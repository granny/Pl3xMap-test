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
package net.pl3x.map.bukkit.command;

import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.command.Sender;
import net.pl3x.map.core.world.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitSender extends Sender {
    public BukkitSender(@NotNull CommandSender sender) {
        super(sender);
    }

    public static @NotNull Sender create(@NotNull CommandSender sender) {
        if (sender instanceof org.bukkit.entity.Player) {
            return new Player(sender);
        }
        return new BukkitSender(sender);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull CommandSender getSender() {
        return super.getSender();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        BukkitSender other = (BukkitSender) o;
        return getSender() == other.getSender();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSender());
    }

    @Override
    public @NotNull String toString() {
        return "BukkitSender{"
                + "sender=" + getSender().getName()
                + "}";
    }

    public static class Player extends BukkitSender implements Audience, Sender.Player<org.bukkit.entity.Player> {
        public Player(@NotNull CommandSender sender) {
            super(sender);
        }

        @Override
        public org.bukkit.entity.@NotNull Player getPlayer() {
            return (org.bukkit.entity.Player) getSender();
        }

        @Override
        public @NotNull Audience audience() {
            return Pl3xMap.api().adventure().player(getPlayer().getUniqueId());
        }

        @Override
        public @NotNull UUID getUUID() {
            return getPlayer().getUniqueId();
        }

        @Override
        public @Nullable World getWorld() {
            return Pl3xMap.api().getWorldRegistry().get(getPlayer().getWorld().getName());
        }

        @Override
        public @NotNull String toString() {
            return "BukkitSender$Player{"
                    + "player=" + getPlayer().getUniqueId()
                    + "}";
        }
    }
}
