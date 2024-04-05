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
package net.pl3x.map.forge.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.player.PlayerRegistry;
import net.pl3x.map.forge.ForgePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoRegisterCapability
public class HiddenCapability {
    private static final ResourceLocation KEY = new ResourceLocation("pl3xmap", "hidden");
    private static final Capability<@NotNull HiddenCapability> CAPABILITY = CapabilityManager.get(new HiddenCapability.Token());
    private boolean hidden;

    public static @NotNull LazyOptional<@NotNull HiddenCapability> get(@NotNull ServerPlayer player) {
        return player.getCapability(CAPABILITY);
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @SubscribeEvent
    public void onAttachCapabilitiesEvent(@NotNull AttachCapabilitiesEvent<@NotNull Entity> event) {
        if (event.getObject() instanceof ServerPlayer) {
            event.addCapability(HiddenCapability.KEY, new HiddenCapability.Provider());
        }
    }

    @SubscribeEvent
    public void onPlayerCloneEvent(PlayerEvent.@NotNull Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) {
            return;
        }
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) {
            return;
        }

        PlayerRegistry registry = Pl3xMap.api().getPlayerRegistry();
        registry.unregister(oldPlayer.getUUID());

        oldPlayer.reviveCaps();
        get(oldPlayer).ifPresent(oldCap ->
                get(newPlayer).ifPresent(newCap ->
                        newCap.setHidden(oldCap.isHidden())
                )
        );
        oldPlayer.invalidateCaps();

        registry.getOrDefault(newPlayer.getUUID(), () -> new ForgePlayer(newPlayer));
    }

    public static class Provider implements ICapabilityProvider, INBTSerializable<ByteTag> {
        private HiddenCapability capability;
        private final LazyOptional<@NotNull HiddenCapability> supplier = LazyOptional.of(this::getOrCreate);

        private @NotNull HiddenCapability getOrCreate() {
            if (this.capability == null) {
                this.capability = new HiddenCapability();
            }
            return this.capability;
        }

        @Override
        public @NotNull <T> LazyOptional<@NotNull T> getCapability(@NotNull Capability<@NotNull T> cap, @Nullable Direction facing) {
            return CAPABILITY.orEmpty(cap, this.supplier);
        }

        @Override
        public @NotNull ByteTag serializeNBT() {
            return ByteTag.valueOf(getOrCreate().isHidden());
        }

        @Override
        public void deserializeNBT(@NotNull ByteTag nbt) {
            getOrCreate().setHidden(nbt.getAsByte() != (byte) 0);
        }
    }

    private static class Token extends CapabilityToken<@NotNull HiddenCapability> {
    }
}
