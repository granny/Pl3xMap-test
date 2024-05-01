/*
 * MIT License
 *
 * Copyright (c) 2020-2023 William Blake Galbreath
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
package net.pl3x.map.core.command;

import java.util.List;
import java.util.function.UnaryOperator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.command.commands.ConfirmCommand;
import net.pl3x.map.core.command.commands.FullRenderCommand;
import net.pl3x.map.core.command.commands.HelpCommand;
import net.pl3x.map.core.command.commands.HideCommand;
import net.pl3x.map.core.command.commands.PauseCommand;
import net.pl3x.map.core.command.commands.ResumeCommand;
import net.pl3x.map.core.command.commands.RadiusRenderCommand;
import net.pl3x.map.core.command.commands.ReloadCommand;
import net.pl3x.map.core.command.commands.ResetMapCommand;
import net.pl3x.map.core.command.commands.ShowCommand;
import net.pl3x.map.core.command.commands.StatusCommand;
import net.pl3x.map.core.command.commands.StitchCommand;
import net.pl3x.map.core.command.commands.VersionCommand;
import net.pl3x.map.core.command.parser.PlatformParsers;
import net.pl3x.map.core.configuration.Config;
import net.pl3x.map.core.configuration.Lang;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.description.CommandDescription;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.minecraft.extras.AudienceProvider;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the command handler.
 */
public interface CommandHandler {
    /**
     * Get the command manager.
     *
     * @return command manager
     */
    @NotNull CommandManager<@NotNull Sender> getManager();

    /**
     * Get the platform parsers.
     *
     * @return platform parsers
     */
    @NotNull PlatformParsers getPlatformParsers();

    /**
     * Get the root command.
     *
     * @return the root command
     */
    Command.@NotNull Builder<@NotNull Sender> getRoot();

    default void setupExceptionHandlers() {
        MinecraftExceptionHandler.<Sender>createNative()
                .defaultHandlers()
                .decorator(component -> Component.text()
                        .append(Lang.parse(Lang.PREFIX_COMMAND)
                                .hoverEvent(Lang.parse(Lang.CLICK_FOR_HELP))
                                .clickEvent(ClickEvent.runCommand("/map help")))
                        .append(component)
                        .build())
                .registerTo(getManager());
    }

    /**
     * Register a new subcommand.
     *
     * @param builder command builder
     */
    default void registerSubcommand(@NotNull UnaryOperator<Command.@NotNull Builder<@NotNull Sender>> builder) {
        this.getManager().command(builder.apply(getRoot()));
    }

    default Command.@NotNull Builder<@NotNull Sender> buildRoot() {
        return getManager().commandBuilder("map", "pl3xmap")
                .permission("pl3xmap.command.map")
                .commandDescription(CommandDescription.commandDescription("Pl3xMap command. '/map help'"))
                .handler(context -> {
                    context.sender().sendMessage(Lang.COMMAND_BASE
                            // minimessage doesn't seem to handle placeholders inside
                            // placeholders, so we have to replace this one manually
                            .replace("<web-address>", Config.WEB_ADDRESS));
                });
    }

    default void registerSubcommands() {
        List.of(
                new ConfirmCommand(this),
                new FullRenderCommand(this),
                new HelpCommand(this),
                new HideCommand(this),
                new PauseCommand(this),
                new ResumeCommand(this),
                new RadiusRenderCommand(this),
                new ReloadCommand(this),
                new ResetMapCommand(this),
                new ShowCommand(this),
                new StatusCommand(this),
                new StitchCommand(this),
                new VersionCommand(this)
        ).forEach(Pl3xMapCommand::register);
    }
}
