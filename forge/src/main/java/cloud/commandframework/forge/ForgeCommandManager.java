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

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.brigadier.BrigadierManagerHolder;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
public abstract class ForgeCommandManager<C>
        extends CommandManager<C> implements BrigadierManagerHolder<C> {
    static final Set<ForgeCommandManager<?>> INSTANCES = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private final Function<CommandSourceStack, C> commandSourceMapper;
    private final Function<C, CommandSourceStack> backwardsCommandSourceMapper;
    private final CloudBrigadierManager<C, CommandSourceStack> brigadierManager;

    protected ForgeCommandManager(
            final Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final Function<CommandSourceStack, C> commandSourceMapper,
            final Function<C, CommandSourceStack> backwardsCommandSourceMapper,
            final ForgeCommandRegistrationHandler<C> registrationHandler,
            final Supplier<CommandSourceStack> dummyCommandSourceProvider
    ) {
        super(commandExecutionCoordinator, registrationHandler);
        INSTANCES.add(this);
        this.commandSourceMapper = commandSourceMapper;
        this.backwardsCommandSourceMapper = backwardsCommandSourceMapper;
        this.brigadierManager = new CloudBrigadierManager<>(this, () -> new CommandContext<>(
                this.commandSourceMapper.apply(dummyCommandSourceProvider.get()),
                this
        ));
        this.brigadierManager.backwardsBrigadierSenderMapper(this.backwardsCommandSourceMapper);
        this.brigadierManager.brigadierSenderMapper(this.commandSourceMapper);
        this.registerCommandPreProcessor(new ForgeCommandPreprocessor<>(this));
        this.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor<>(
                FilteringCommandSuggestionProcessor.Filter.<C>startsWith(true).andTrimBeforeLastSpace()
        ));
        registrationHandler.initialize(this);
    }

    Function<C, CommandSourceStack> backwardsCommandSourceMapper() {
        return this.backwardsCommandSourceMapper;
    }

    Function<CommandSourceStack, C> commandSourceMapper() {
        return this.commandSourceMapper;
    }

    @Override
    public boolean hasPermission(final @NotNull C sender, final @NotNull String permission) {
        return false;
    }

    @Override
    public final CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    @Override
    public CloudBrigadierManager<C, CommandSourceStack> brigadierManager() {
        return this.brigadierManager;
    }

    final void registrationCalled() {
        this.lockRegistration();
    }
}
