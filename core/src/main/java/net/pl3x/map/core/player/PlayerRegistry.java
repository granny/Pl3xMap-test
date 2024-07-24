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
package net.pl3x.map.core.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.configuration.PlayersLayerConfig;
import net.pl3x.map.core.registry.Registry;
import net.pl3x.map.core.util.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages player specific data
 */
public class PlayerRegistry extends Registry<@NotNull Player> {
    public @NotNull Player getOrDefault(@NotNull UUID uuid, @NotNull Supplier<@NotNull Player> supplier) {
        Player player = get(uuid);
        if (player == null) {
            player = supplier.get();
            register(player.getUUID(), player);
        }
        return player;
    }

    public @NotNull Player register(@NotNull UUID uuid, @NotNull Player player) {
        Preconditions.checkNotNull(uuid, "UUID cannot be null");
        Preconditions.checkNotNull(player, "Player cannot be null");
        return super.register(uuid.toString(), player);
    }

    public @Nullable Player unregister(@NotNull UUID uuid) {
        return super.unregister(uuid.toString());
    }

    /**
     * Get the registered player by uuid.
     * <p>
     * Will return null if no player registered.
     *
     * @param uuid player uuid
     * @return registered player or null
     */
    public @Nullable Player get(@NotNull UUID uuid) {
        return super.get(uuid.toString());
    }

    /**
     * Get the registered player by name.
     * <p>
     * Will return null if no player registered.
     *
     * @param name player name
     * @return registered player or null
     */
    public @Nullable Player get(@NotNull String name) {
        String lowercaseName = name.toLowerCase(Locale.ROOT);
        for (Player player : values()) {
            if (player.getName().toLowerCase(Locale.ROOT).equals(lowercaseName)) {
                return player;
            }
        }
        return null;
    }

    public @NotNull Optional<Player> optional(@NotNull UUID uuid) {
        Player player = get(uuid);
        return player == null ? Optional.empty() : Optional.of(player);
    }

    public @NotNull List<@NotNull Object> parsePlayers() {
        if (!PlayersLayerConfig.ENABLED) {
            return Collections.emptyList();
        }
        List<Object> players = new ArrayList<>();
        forEach(player -> {
            // do not expose hidden players in the json
            if (player.isHidden() || player.isNPC()) {
                return;
            }
            if (PlayersLayerConfig.HIDE_SPECTATORS && player.isSpectator()) {
                return;
            }
            if (PlayersLayerConfig.HIDE_INVISIBLE && player.isInvisible()) {
                return;
            }

            Map<String, Object> playerEntry = new LinkedHashMap<>();

            playerEntry.put("name", player.getDecoratedName());
            playerEntry.put("uuid", player.getUUID().toString());
            playerEntry.put("displayName", player.getDecoratedName());
            playerEntry.put("world", player.getWorld().getName());
            playerEntry.put("position", player.getPosition());

            players.add(playerEntry);
        });
        return players;
    }
}
