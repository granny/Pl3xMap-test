/*
 * MIT License
 *
 * Copyright (c) 2023 Cloud Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.commandframework.forge;

import com.mojang.brigadier.arguments.ArgumentType;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;

//@ApiStatus.Internal
public final class ContextualArgumentTypeProvider<V> implements Supplier<ArgumentType<V>> {

    private static final ThreadLocal<ThreadLocalContext> CONTEXT = new ThreadLocal<>();
    private static final Map<ForgeCommandManager<?>, Set<ContextualArgumentTypeProvider<?>>> INSTANCES = new WeakHashMap<>();

    private final Function<CommandBuildContext, ArgumentType<V>> provider;
    private volatile ArgumentType<V> provided;

    ContextualArgumentTypeProvider(final Function<CommandBuildContext, ArgumentType<V>> provider) {
        this.provider = provider;
    }

    /**
     * Temporarily expose a command build context to providers called from this thread.
     *
     * @param ctx            the context
     * @param commandManager command manager to use
     * @param resetExisting  whether to clear cached state from existing provider instances for this command type
     * @param action         an action to perform while the context is exposed
     */
    public static void withBuildContext(
            final ForgeCommandManager<?> commandManager,
            final CommandBuildContext ctx,
            final boolean resetExisting,
            final Runnable action
    ) {
        final ThreadLocalContext context = new ThreadLocalContext(commandManager, ctx);
        CONTEXT.set(context);

        try {
            if (resetExisting) {
                synchronized (INSTANCES) {
                    for (final ContextualArgumentTypeProvider<?> contextualArgumentTypeProvider : context.instances()) {
                        contextualArgumentTypeProvider.provided = null;
                    }
                }
            }

            action.run();
        } finally {
            CONTEXT.remove();
        }
    }

    @Override
    public ArgumentType<V> get() {
        final ThreadLocalContext ctx = CONTEXT.get();

        if (ctx != null) {
            synchronized (INSTANCES) {
                ctx.instances().add(this);
            }
        }

        ArgumentType<V> provided = this.provided;
        if (provided == null) {
            synchronized (this) {
                if (this.provided == null) {
                    if (ctx == null) {
                        throw new IllegalStateException(
                                "No build context was available while trying to compute an argument type");
                    }
                    provided = this.provider.apply(ctx.commandBuildContext);
                    this.provided = provided;
                }
            }
        }
        return provided;
    }

    private record ThreadLocalContext(
            ForgeCommandManager<?> commandManager,
            CommandBuildContext commandBuildContext
    ) {
        private Set<ContextualArgumentTypeProvider<?>> instances() {
            return INSTANCES.computeIfAbsent(this.commandManager, $ -> Collections.newSetFromMap(new WeakHashMap<>()));
        }
    }
}
