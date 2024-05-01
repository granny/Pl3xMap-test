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

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.command.CommandHandler;
import net.pl3x.map.core.command.Pl3xMapCommand;
import net.pl3x.map.core.command.Sender;
import net.pl3x.map.core.command.parser.WorldParser;
import net.pl3x.map.core.configuration.Config;
import net.pl3x.map.core.configuration.Lang;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.world.World;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.jetbrains.annotations.NotNull;

public class FullRenderCommand extends Pl3xMapCommand {
    public FullRenderCommand(@NotNull CommandHandler handler) {
        super(handler);
    }

    @Override
    public void register() {
        getHandler().registerSubcommand(builder -> builder.literal("fullrender")
                .required("world", WorldParser.parser(), description(Lang.COMMAND_ARGUMENT_REQUIRED_WORLD_DESCRIPTION))
                .commandDescription(RichDescription.of(Lang.parse(Lang.COMMAND_FULLRENDER_DESCRIPTION)))
                .permission("pl3xmap.command.fullrender")
                .handler(this::execute));
    }

    public void execute(@NotNull CommandContext<@NotNull Sender> context) {
        CompletableFuture.runAsync(() -> executeAsync(context));
    }

    private void executeAsync(@NotNull CommandContext<@NotNull Sender> context) {
        Sender sender = context.sender();
        World world = context.get("world");

        Collection<Point> regions = world.listRegions(true);

        if (Config.DEBUG_MODE) {
            regions.forEach(region -> Logger.debug("Adding region: " + region));
        }

        Pl3xMap.api().getRegionProcessor().addRegions(world, regions);

        sender.sendMessage(Lang.COMMAND_FULLRENDER_STARTING);
    }
}
