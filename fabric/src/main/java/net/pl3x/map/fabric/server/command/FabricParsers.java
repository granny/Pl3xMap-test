package net.pl3x.map.fabric.server.command;

import net.minecraft.world.phys.Vec3;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.command.Sender;
import net.pl3x.map.core.command.parser.PlatformParsers;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.player.Player;
import net.pl3x.map.fabric.server.FabricPlayer;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.modded.data.Coordinates;
import org.incendo.cloud.minecraft.modded.data.SinglePlayerSelector;
import org.incendo.cloud.minecraft.modded.parser.VanillaArgumentParsers;
import org.incendo.cloud.parser.ParserDescriptor;
import org.jetbrains.annotations.Nullable;

public class FabricParsers implements PlatformParsers {
    @Override
    public ParserDescriptor<Sender, ?> columnPosParser() {
        return VanillaArgumentParsers.columnPosParser();
    }

    @Override
    public Point resolvePointFromColumnPos(String name, CommandContext<Sender> context) {
        Coordinates.ColumnCoordinates columnCoordinates = context.<Coordinates.ColumnCoordinates>getOrDefault(name, null);
        if (columnCoordinates == null) {
            return Point.ZERO;
        }
        Vec3 position = columnCoordinates.position();
        return Point.of(position.x, position.z);
    }

    @Override
    public ParserDescriptor<Sender, ?> playerSelectorParser() {
        return VanillaArgumentParsers.singlePlayerSelectorParser();
    }

    @Override
    public Player resolvePlayerFromPlayerSelector(String name, CommandContext<Sender> context) {
        Sender sender = context.sender();
        SinglePlayerSelector playerSelector = context.<SinglePlayerSelector>getOrDefault(name, null);
        if (playerSelector == null) {
            if (sender instanceof Sender.Player<?> senderPlayer) {
                Player player = Pl3xMap.api().getPlayerRegistry().get(senderPlayer.getUUID());
                if (player != null) {
                    return player;
                }
            }
            return null;
        }
        return new FabricPlayer(playerSelector.single());
    }
}
