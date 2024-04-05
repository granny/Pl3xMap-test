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
package net.pl3x.map.forge.command;

import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.forge.ForgeServerAudiences;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.command.Sender;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgeSender extends Sender {
    public ForgeSender(@NotNull CommandSourceStack sender) {
        super(sender);
    }

    public static @NotNull Sender create(@NotNull CommandSourceStack stack) {
        if (stack.source instanceof ServerPlayer) {
            return new Player(stack);
        }
        return new ForgeSender(stack);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull CommandSourceStack getSender() {
        return super.getSender();
    }

    @Override
    public @NotNull Audience audience() {
        return ((ForgeServerAudiences) Pl3xMap.api().adventure()).audience(getSender());
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
        ForgeSender other = (ForgeSender) o;
        return getSender().source == other.getSender().source;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSender().source);
    }

    @Override
    public @NotNull String toString() {
        return "ForgeSender{"
                + "sender=" + getSender().getTextName()
                + "}";
    }

    public static class Player extends ForgeSender implements Audience, Sender.Player<ServerPlayer> {
        public Player(@NotNull CommandSourceStack sender) {
            super(sender);
        }

        @Override
        public @NotNull ServerPlayer getPlayer() {
            return (ServerPlayer) getSender().source;
        }

        @Override
        public @NotNull Audience audience() {
            return Pl3xMap.api().adventure().player(getPlayer().getUUID());
        }

        @Override
        public @NotNull UUID getUUID() {
            return getPlayer().getUUID();
        }

        @Override
        public @Nullable World getWorld() {
            return Pl3xMap.api().getWorldRegistry().get(getPlayer().level().dimension().location().toString());
        }

        @Override
        public @NotNull String toString() {
            return "ForgeSender$Player{"
                    + "player=" + getPlayer().getUUID()
                    + "}";
        }
    }
}
