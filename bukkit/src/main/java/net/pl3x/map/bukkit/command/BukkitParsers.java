package net.pl3x.map.bukkit.command;

import net.minecraft.world.phys.Vec3;
import net.pl3x.map.bukkit.BukkitPlayer;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.command.Sender;
import net.pl3x.map.core.command.parser.PlatformParsers;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.player.Player;
import org.incendo.cloud.bukkit.data.SinglePlayerSelector;
import org.incendo.cloud.bukkit.parser.location.Location2D;
import org.incendo.cloud.bukkit.parser.location.Location2DParser;
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.ParserDescriptor;

public class BukkitParsers implements PlatformParsers {
    @Override
    public ParserDescriptor<Sender, ?> columnPosParser() {
        return Location2DParser.location2DParser();
    }

    @Override
    public Point resolvePointFromColumnPos(String name, CommandContext<Sender> context) {
        Location2D location2D = context.<Location2D>getOrDefault(name, null);
        if (location2D == null) {
            return Point.ZERO;
        }
        return Point.of(location2D.blockX(), location2D.blockZ());
    }

    @Override
    public ParserDescriptor<Sender, ?> playerSelectorParser() {
        return SinglePlayerSelectorParser.singlePlayerSelectorParser();
    }

    @Override
    public Player resolvePlayerFromPlayerSelector(String name, CommandContext<Sender> context) {
        Sender sender = context.sender();
        SinglePlayerSelector playerSelector = context.getOrDefault(name, null);
        if (playerSelector == null) {
            if (sender instanceof Sender.Player<?> senderPlayer) {
                Player player = Pl3xMap.api().getPlayerRegistry().get(senderPlayer.getUUID());
                if (player != null) {
                    return player;
                }
            }
            return null;
        }
        return new BukkitPlayer(playerSelector.single());
    }
}
