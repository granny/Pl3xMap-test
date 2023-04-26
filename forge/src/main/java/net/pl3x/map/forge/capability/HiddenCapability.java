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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@AutoRegisterCapability
public class HiddenCapability {
    private static final ResourceLocation KEY = new ResourceLocation("pl3xmap", "hidden");
    private static final Capability<@NonNull HiddenCapability> CAPABILITY = CapabilityManager.get(new HiddenCapability.Token());

    public static @NonNull LazyOptional<@NonNull HiddenCapability> get(@NonNull ServerPlayer player) {
        return player.getCapability(CAPABILITY);
    }

    private boolean hidden;

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @SubscribeEvent
    public void onAttachCapabilitiesEvent(@NonNull AttachCapabilitiesEvent<@NonNull Entity> event) {
        if (event.getObject() instanceof ServerPlayer) {
            event.addCapability(HiddenCapability.KEY, new HiddenCapability.Provider());
        }
    }

    @SubscribeEvent
    public void onPlayerCloneEvent(PlayerEvent.@NonNull Clone event) {
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
        private final LazyOptional<@NonNull HiddenCapability> supplier = LazyOptional.of(this::getOrCreate);

        private HiddenCapability capability;

        private @NonNull HiddenCapability getOrCreate() {
            if (this.capability == null) {
                this.capability = new HiddenCapability();
            }
            return this.capability;
        }

        @Override
        public @NonNull <@NonNull T> LazyOptional<@NonNull T> getCapability(@NonNull Capability<@NonNull T> cap, @Nullable Direction facing) {
            return CAPABILITY.orEmpty(cap, this.supplier);
        }

        @Override
        public @NonNull ByteTag serializeNBT() {
            return ByteTag.valueOf(getOrCreate().isHidden());
        }

        @Override
        public void deserializeNBT(@NonNull ByteTag nbt) {
            getOrCreate().setHidden(nbt.getAsByte() == (byte) 1);
        }
    }

    private static class Token extends CapabilityToken<@NonNull HiddenCapability> {
    }
}
