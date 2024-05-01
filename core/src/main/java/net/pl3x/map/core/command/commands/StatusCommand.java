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

import java.util.Set;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.command.CommandHandler;
import net.pl3x.map.core.command.Pl3xMapCommand;
import net.pl3x.map.core.command.Sender;
import net.pl3x.map.core.configuration.Lang;
import net.pl3x.map.core.renderer.progress.Progress;
import net.pl3x.map.core.renderer.task.RegionProcessor;
import net.pl3x.map.core.world.World;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.jetbrains.annotations.NotNull;

public class StatusCommand extends Pl3xMapCommand {
    public StatusCommand(@NotNull CommandHandler handler) {
        super(handler);
    }

    @Override
    public void register() {
        getHandler().registerSubcommand(builder -> builder.literal("status")
                .commandDescription(RichDescription.of(Lang.parse(Lang.COMMAND_STATUS_DESCRIPTION)))
                .permission("pl3xmap.command.status")
                .handler(this::execute));
    }

    public void execute(@NotNull CommandContext<@NotNull Sender> context) {
        Sender sender = context.sender();

        String lineNext = "├─";
        String lineLast = "└─";

        String header = "<color:#5b00ff><strikethrough>---------------</strikethrough> <white>Pl3xMap Status</white> <strikethrough>---------------</strikethrough></color>";
        String footer = "<color:#5b00ff><strikethrough>----------------------------------------------</strikethrough></color>";

        String active = """
                <gray>Actively running renderers:</gray>
                <dark_gray><linelast></dark_gray> <color:#5b00ff>World:</color> <white><world></white>
                   <dark_gray><linenext></dark_gray> <white>chk:</white> <gray><processed_chunks>/<total_chunks></gray>
                   <dark_gray><linenext></dark_gray> <white>pct:</white> <gray><percent>%</gray>
                   <dark_gray><linenext></dark_gray> <white>cps:</white> <gray><cps></gray>
                   <dark_gray><linelast></dark_gray> <white>eta:</white> <gray><eta></gray>""";

        String queuedHeader = "<gray>Queued up renderers:</gray>";
        String queuedEntry = "<dark_gray><line></dark_gray> <color:#5b00ff>World:</color> <white><world></white>";

        String paused = "<gray>Renderers are </gray><white>paused</white>";
        String idle = "<gray>Renderers are </gray><white>idle</white>";

        RegionProcessor processor = Pl3xMap.api().getRegionProcessor();
        Progress progress = processor.getProgress();

        StringBuilder sb = new StringBuilder();
        sb.append(header).append("\n");

        boolean isPaused = processor.isPaused();
        boolean isIdle = progress.getWorld() == null;

        if (isPaused) {
            sb.append(paused);
        } else if (isIdle) {
            sb.append(idle);
        } else {
            sb.append(active);
        }
        sb.append("\n");

        Set<World> worlds = processor.getQueuedWorlds();
        if (!worlds.isEmpty()) {
            sb.append(queuedHeader).append("\n");
            int i = 0;
            for (World world : worlds) {
                sb.append(queuedEntry
                        .replace("<world>", world.getName())
                        .replace("<line>", i++ < worlds.size() ? lineNext : lineLast));
                sb.append("\n");
            }
        }
        sb.append(footer);

        if (isPaused || isIdle) {
            sender.sendMessage(sb.toString(), false);
            return;
        }

        sender.sendMessage(sb.toString(), false,
                Placeholder.unparsed("world", progress.getWorld().getName()),
                Placeholder.unparsed("processed_chunks", Long.toString(progress.getProcessedChunks().get())),
                Placeholder.unparsed("total_chunks", Long.toString(progress.getTotalChunks())),
                Placeholder.unparsed("processed_regions", Long.toString(progress.getProcessedRegions().get())),
                Placeholder.unparsed("total_regions", Long.toString(progress.getTotalRegions())),
                Placeholder.unparsed("percent", String.format("%.2f", progress.getPercent())),
                Placeholder.unparsed("cps", String.format("%.2f", progress.getCPS())),
                Placeholder.unparsed("eta", progress.getETA()),
                Placeholder.unparsed("linenext", lineNext),
                Placeholder.unparsed("linelast", lineLast)
        );
    }
}
