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
package net.pl3x.map.bukkit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.player.Player;
import net.pl3x.map.core.world.World;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitPlayer extends Player {
    private final NamespacedKey hiddenKey = new NamespacedKey(Pl3xMapBukkit.getProvidingPlugin(Pl3xMapBukkit.class), "hidden");

    public BukkitPlayer(org.bukkit.entity.@NotNull Player player) {
        super(player.getName(), player);
    }

    @Override
    @SuppressWarnings("unchecked")
    public org.bukkit.entity.@NotNull Player getPlayer() {
        return super.getPlayer();
    }

    @Override
    public @NotNull String getName() {
        return getPlayer().getName();
    }

    @Override
    public @NotNull UUID getUUID() {
        return getPlayer().getUniqueId();
    }

    @Override
    public @NotNull World getWorld() {
        org.bukkit.World world = getPlayer().getWorld();
        return Pl3xMap.api().getWorldRegistry().getOrDefault(world.getName(),
                () -> new BukkitWorld(((CraftWorld) world).getHandle(), world.getName()));
    }

    @Override
    public @NotNull Point getPosition() {
        Location loc = getPlayer().getLocation();
        return Point.of(loc.getBlockX(), loc.getBlockZ());
    }

    @Override
    public float getYaw() {
        return getPlayer().getLocation().getYaw();
    }

    @Override
    public int getHealth() {
        return (int) Math.round(getPlayer().getHealth());
    }

    @Override
    public int getArmorPoints() {
        AttributeInstance attr = getPlayer().getAttribute(Attribute.GENERIC_ARMOR);
        return attr == null ? 0 : (int) Math.round(attr.getValue());
    }

    @Override
    public @Nullable URL getSkin() {
        try {
            ServerPlayer player = ((CraftPlayer) getPlayer()).getHandle();
            Property property = player.getGameProfile().getProperties().get("textures").stream().findFirst().orElse(null);
            if (property == null) {
                return null;
            }
            String json = new String(Base64.getDecoder().decode(property.value()), StandardCharsets.UTF_8);
            JsonElement jsonElement = JsonParser.parseString(json);
            if (!jsonElement.isJsonObject()) {
                return null;
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject().getAsJsonObject("textures");
            if (jsonObject == null) {
                return null;
            }
            JsonObject skin = jsonObject.get("SKIN").getAsJsonObject();
            if (skin == null) {
                return null;
            }
            String url = skin.get("url").getAsString();
            if (url == null) {
                return null;
            }
            return new URI(url).toURL();
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public boolean isInvisible() {
        return getPlayer().isInvisible();
    }

    @Override
    public boolean isNPC() {
        return getPlayer().hasMetadata("NPC");
    }

    @Override
    public boolean isSpectator() {
        return getPlayer().getGameMode() == GameMode.SPECTATOR;
    }

    @Override
    public boolean isPersistentlyHidden() {
        return getPlayer().getPersistentDataContainer().getOrDefault(hiddenKey, PersistentDataType.BYTE, (byte) 0) != 0;
    }

    @Override
    public void setPersistentlyHidden(boolean hidden) {
        getPlayer().getPersistentDataContainer().set(hiddenKey, PersistentDataType.BYTE, (byte) (hidden ? 1 : 0));
    }

    @Override
    public @NotNull String toString() {
        return "BukkitPlayer{"
                + "player=" + getPlayer().getUniqueId()
                + "}";
    }
}
