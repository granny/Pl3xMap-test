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

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.pl3x.map.core.command.CommandHandler;
import net.pl3x.map.core.command.Pl3xMapCommand;
import net.pl3x.map.core.command.Sender;
import net.pl3x.map.core.configuration.Lang;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.minecraft.extras.AudienceProvider;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.jetbrains.annotations.NotNull;

public class HelpCommand extends Pl3xMapCommand {
    private final MinecraftHelp<@NotNull Sender> minecraftHelp;

    public HelpCommand(@NotNull CommandHandler handler) {
        super(handler);
        this.minecraftHelp = MinecraftHelp.<Sender>builder()
                .commandManager(handler.getManager())
                .audienceProvider(AudienceProvider.nativeAudience())
                .commandPrefix("/map help")
                .colors(MinecraftHelp.helpColors(
                        TextColor.color(0x5B00FF),
                        NamedTextColor.WHITE,
                        TextColor.color(0xC028FF),
                        NamedTextColor.GRAY,
                        NamedTextColor.DARK_GRAY
                ))
                .messages(MinecraftHelp.MESSAGE_HELP_TITLE, "Pl3xMap Help")
                .build();
    }

    @Override
    public void register() {
        getHandler().registerSubcommand(builder -> builder.literal("help")
                .commandDescription(RichDescription.of(Lang.parse(Lang.COMMAND_HELP_DESCRIPTION)))
                .optional(CommandComponent.<Sender, String>ofType(String.class, "query")
                        .parser(StringParser.greedyStringParser())
                        .suggestionProvider(SuggestionProvider.blockingStrings((context, input) -> getHandler().getManager()
                                .createHelpHandler().queryRootIndex(context.sender())
                                .entries().stream().map(CommandEntry::syntax).toList())))
                .permission("pl3xmap.command.help")
                .handler(ctx -> {
                    String query = ctx.<String>optional("query").orElse("");
                    this.minecraftHelp.queryCommands(query, ctx.sender());
                }));
    }
}
