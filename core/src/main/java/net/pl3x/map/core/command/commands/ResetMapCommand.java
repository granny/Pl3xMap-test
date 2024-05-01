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

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.command.CommandHandler;
import net.pl3x.map.core.command.Pl3xMapCommand;
import net.pl3x.map.core.command.Sender;
import net.pl3x.map.core.command.parser.WorldParser;
import net.pl3x.map.core.configuration.Lang;
import net.pl3x.map.core.util.FileUtil;
import net.pl3x.map.core.world.World;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.jetbrains.annotations.NotNull;

public class ResetMapCommand extends Pl3xMapCommand {
    public ResetMapCommand(@NotNull CommandHandler handler) {
        super(handler);
    }

    @Override
    public void register() {
        getHandler().registerSubcommand(builder -> builder.literal("resetmap")
                .required("world", WorldParser.parser(), description(Lang.COMMAND_ARGUMENT_REQUIRED_WORLD_DESCRIPTION))
                .commandDescription(RichDescription.of(Lang.parse(Lang.COMMAND_RESETMAP_DESCRIPTION)))
                .meta(ConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .permission("pl3xmap.command.resetmap")
                .handler(this::execute));
    }

    private void execute(@NotNull CommandContext<@NotNull Sender> context) {
        CompletableFuture.runAsync(() -> executeAsync(context));
    }

    private void executeAsync(@NotNull CommandContext<@NotNull Sender> context) {
        Sender sender = context.sender();
        World world = context.get("world");

        TagResolver.Single worldPlaceholder = Placeholder.unparsed("world", world.getName());
        sender.sendMessage(Lang.COMMAND_RESETMAP_BEGIN, worldPlaceholder);

        CompletableFuture.runAsync(() -> {
            // unregister the world
            Pl3xMap.api().getWorldRegistry().unregister(world.getName());

            // delete world files
            String result;
            try {
                FileUtil.deleteDirectory(world.getTilesDirectory());
                result = Lang.COMMAND_RESETMAP_SUCCESS;
            } catch (IOException e) {
                result = Lang.COMMAND_RESETMAP_FAILED;
            }

            // create a new world and register it
            Pl3xMap.api().getWorldRegistry().register(Pl3xMap.api().cloneWorld(world));

            sender.sendMessage(result, worldPlaceholder);
        });
    }
}
