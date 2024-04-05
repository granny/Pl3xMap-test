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

import cloud.commandframework.Command;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.internal.CommandRegistrationHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A registration handler for Minecraft Forge.
 *
 * <p>Subtypes exist for client and server commands.</p>
 *
 * @param <C> command sender type
 */
abstract class ForgeCommandRegistrationHandler<C> implements CommandRegistrationHandler {

    private @MonotonicNonNull ForgeCommandManager<C> commandManager;

    /**
     * Returns a literal node that redirects its execution to
     * the given destination node.
     *
     * <p>This method is taken from MIT licensed code in the Velocity project, see
     * <a href="https://github.com/VelocityPowered/Velocity/blob/b88c573eb11839a95bea1af947b0c59a5956368b/proxy/src/main/java/com/velocitypowered/proxy/util/BrigadierUtils.java#L33">
     * Velocity's BrigadierUtils class</a></p>
     *
     * @param alias       the command alias
     * @param destination the destination node
     * @param <S>         brig sender type
     * @return the built node
     */
    private static <S> LiteralCommandNode<S> buildRedirect(
            final @NonNull String alias,
            final @NonNull CommandNode<S> destination
    ) {
        // Redirects only work for nodes with children, but break the top argument-less command.
        // Manually adding the root command after setting the redirect doesn't fix it.
        // (See https://github.com/Mojang/brigadier/issues/46) Manually clone the node instead.
        final LiteralArgumentBuilder<S> builder = LiteralArgumentBuilder
                .<S>literal(alias)
                .requires(destination.getRequirement())
                .forward(
                        destination.getRedirect(),
                        destination.getRedirectModifier(),
                        destination.isFork()
                )
                .executes(destination.getCommand());
        for (final CommandNode<S> child : destination.getChildren()) {
            builder.then(child);
        }
        return builder.build();
    }

    void initialize(final ForgeCommandManager<C> manager) {
        this.commandManager = manager;
    }

    ForgeCommandManager<C> commandManager() {
        return this.commandManager;
    }

    @SuppressWarnings("unchecked")
    protected final void registerCommand(final Command<C> command, final CommandDispatcher<CommandSourceStack> dispatcher) {
        final RootCommandNode<CommandSourceStack> rootNode = dispatcher.getRoot();
        final StaticArgument<C> first = ((StaticArgument<C>) command.getArguments().get(0));
        final CommandNode<CommandSourceStack> baseNode = this.commandManager()
                .brigadierManager()
                .createLiteralCommandNode(
                        first.getName(),
                        command,
                        (src, perm) -> this.commandManager().hasPermission(
                                this.commandManager().commandSourceMapper().apply(src),
                                perm
                        ),
                        true,
                        new ForgeExecutor<>(this.commandManager())
                );

        rootNode.addChild(baseNode);

        for (final String alias : first.getAlternativeAliases()) {
            rootNode.addChild(buildRedirect(alias, baseNode));
        }
    }

    static class Client<C> extends ForgeCommandRegistrationHandler<C> {

        private final Set<Command<C>> registeredCommands = ConcurrentHashMap.newKeySet();
        private volatile boolean registerEventFired = false;

        @Override
        void initialize(final ForgeCommandManager<C> manager) {
            super.initialize(manager);
            MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
            MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> this.registerEventFired = false);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean registerCommand(final @NonNull Command<?> command) {
            this.registeredCommands.add((Command<C>) command);
            if (this.registerEventFired) {
                final ClientPacketListener connection = Minecraft.getInstance().getConnection();
                if (connection == null) {
                    throw new IllegalStateException("Expected connection to be present but it wasn't!");
                }
                final CommandDispatcher<CommandSourceStack> dispatcher = ClientCommandHandler.getDispatcher();
                if (dispatcher == null) {
                    throw new IllegalStateException("Expected an active dispatcher!");
                }
                ContextualArgumentTypeProvider.withBuildContext(
                        this.commandManager(),
                        CommandBuildContext.simple(connection.registryAccess(), connection.enabledFeatures()),
                        false,
                        () -> this.registerCommand((Command<C>) command, dispatcher)
                );
            }
            return true;
        }

        public void registerCommands(final RegisterClientCommandsEvent event) {
            this.registerEventFired = true;
            ContextualArgumentTypeProvider.withBuildContext(
                    this.commandManager(),
                    event.getBuildContext(),
                    true,
                    () -> {
                        for (final Command<C> command : this.registeredCommands) {
                            this.registerCommand(command, event.getDispatcher());
                        }
                    }
            );
        }
    }

    static class Server<C> extends ForgeCommandRegistrationHandler<C> {

        private final Set<Command<C>> registeredCommands = ConcurrentHashMap.newKeySet();

        @Override
        void initialize(final ForgeCommandManager<C> manager) {
            super.initialize(manager);
            MinecraftForge.EVENT_BUS.addListener(this::registerAllCommands);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean registerCommand(@NonNull final Command<?> command) {
            return this.registeredCommands.add((Command<C>) command);
        }

        private void registerAllCommands(final RegisterCommandsEvent event) {
            this.commandManager().registrationCalled();
            ContextualArgumentTypeProvider.withBuildContext(
                    this.commandManager(),
                    event.getBuildContext(),
                    true,
                    () -> {
                        for (final Command<C> command : this.registeredCommands) {
                            /* Only register commands in the declared environment */
                            final Commands.CommandSelection env = command.getCommandMeta().getOrDefault(
                                    ForgeServerCommandManager.META_REGISTRATION_ENVIRONMENT,
                                    Commands.CommandSelection.ALL
                            );

                            if ((env == Commands.CommandSelection.INTEGRATED && !event.getCommandSelection().includeIntegrated)
                                    || (env == Commands.CommandSelection.DEDICATED && !event.getCommandSelection().includeDedicated)) {
                                continue;
                            }
                            this.registerCommand(command, event.getDispatcher());
                        }
                    }
            );
        }
    }
}
