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
package net.pl3x.map.core.command.commands;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.pl3x.map.core.command.CommandHandler;
import net.pl3x.map.core.command.Pl3xMapCommand;
import net.pl3x.map.core.command.Sender;
import net.pl3x.map.core.configuration.Lang;
import net.pl3x.map.core.player.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.jetbrains.annotations.NotNull;

public class ShowCommand extends Pl3xMapCommand {
    public ShowCommand(@NotNull CommandHandler handler) {
        super(handler);
    }

    @Override
    public void register() {
        getHandler().registerSubcommand(builder -> builder.literal("show")
                .commandDescription(RichDescription.of(Lang.parse(Lang.COMMAND_SHOW_DESCRIPTION)))
                .permission("pl3xmap.command.show")
                .handler(this::execute));
        getHandler().registerSubcommand(builder -> builder.literal("show")
                .optional("player", getHandler().getPlatformParsers().playerSelectorParser(), description(Lang.COMMAND_ARGUMENT_OPTIONAL_PLAYER_DESCRIPTION))
                .commandDescription(RichDescription.of(Lang.parse(Lang.COMMAND_SHOW_DESCRIPTION)))
                .permission("pl3xmap.command.show.others")
                .handler(this::execute));
    }

    private void execute(@NotNull CommandContext<@NotNull Sender> context) {
        Sender sender = context.sender();
        Player target = getHandler().getPlatformParsers().resolvePlayerFromPlayerSelector("player", context);

        if (target == null) {
            sender.sendMessage(Lang.ERROR_MUST_SPECIFY_PLAYER);
            return;
        }

        if (!target.isHidden()) {
            sender.sendMessage(Lang.COMMAND_SHOW_NOT_HIDDEN,
                    Placeholder.unparsed("player", target.getName()));
            return;
        }

        target.setHidden(false, true);

        sender.sendMessage(Lang.COMMAND_SHOW_SUCCESS,
                Placeholder.unparsed("player", target.getName()));
    }
}
