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
package net.pl3x.map.fabric.client.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.image.BufferedImage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.pl3x.map.core.util.Colors;
import net.pl3x.map.fabric.client.Pl3xMapFabricClient;
import net.pl3x.map.fabric.client.duck.MapInstance;
import net.pl3x.map.fabric.common.network.ServerboundMapPayload;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.MapInstance.class)
public abstract class MapInstanceMixin implements MapInstance {
    @Final
    @Shadow
    DynamicTexture texture;
    @Shadow
    MapItemSavedData data;
    @Shadow
    boolean requiresUpload;

    private final BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);

    private Pl3xMapFabricClient mod;

    private int id;
    private byte scale;
    private int centerX;
    private int centerZ;
    private String world;

    private boolean isReady;
    private boolean skip;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ctor(@NotNull MapRenderer renderer, int id, @NotNull MapItemSavedData data, @NotNull CallbackInfo ci) {
        this.mod = Pl3xMapFabricClient.getInstance(); // there's got to be a better way :3
        this.id = id;
    }

    @Inject(method = "updateTexture()V", at = @At("HEAD"), cancellable = true)
    private void updateTexture(@NotNull CallbackInfo ci) {
        if (!this.mod.isEnabled()) {
            // custom map renderers are disabled; show vanilla map
            return;
        }

        if (!this.mod.isOnServer()) {
            // player is not on a server; show vanilla map
            return;
        }

        if (this.mod.getServerUrl() == null) {
            // server does not have pl3xmap installed; show vanilla map
            return;
        }

        if (this.skip) {
            // skip this map; show vanilla map
            return;
        }

        // ask for data from server if we haven't already
        if (!this.isReady) {
            ClientPlayNetworking.send(new ServerboundMapPayload(this.id));
            this.skip = true;
            return;
        }

        // try to draw our own map from pl3xmap tiles
        if (updateMapTexture()) {
            // succeeded; cancel drawing vanilla map
            ci.cancel();
        }
    }

    @Override
    public void skip() {
        // we're done; we're skipping this map
        this.isReady = true;
        this.skip = true;
    }

    @Override
    public void setData(byte scale, int centerX, int centerZ, @NotNull String world) {
        // received data from server
        this.scale = scale;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.world = world;

        // we're ready now; do not skip
        this.isReady = true;
        this.skip = false;

        // update image with the new data
        updateImage();
    }

    @Override
    public void updateImage() {
        if (RenderSystem.isOnRenderThread()) {
            this.mod.getExecutor().submit(this::updateImage);
            return;
        }

        if (!this.isReady) {
            // we're not ready yet
            return;
        }

        // pre-calculations
        int mod = 1 << this.scale;
        int startX = (this.centerX / mod - 64) * mod;
        int startZ = (this.centerZ / mod - 64) * mod;

        // store the pixels from pl3xmap tiles onto our own temp image
        BufferedImage img;
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                int blockX = startX + (x * mod) + this.scale;
                int blockZ = startZ + (z * mod) + this.scale;

                // get actual tile from pl3xmap website
                img = this.mod.getTileManager().get(this.world, blockX >> 9, blockZ >> 9);

                // get pixel color from tile
                this.image.setRGB(x, z, Colors.rgb2bgr(img.getRGB(blockX & 511, blockZ & 511)));
            }
        }

        // mark dirty
        this.requiresUpload = true;
    }

    private boolean updateMapTexture() {
        NativeImage pixels = this.texture.getPixels();
        if (pixels == null) {
            // vanilla texture not ready, or already discarded; ignore
            return false;
        }

        int color;
        int pl3xColor;

        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                // get vanilla color
                color = this.data.colors[x + z * 128] & 255;

                // check if vanilla color exists
                if (color >> 2 == 0) {
                    // vanilla color missing (fog of war); draw transparent pixel
                    pixels.setPixelRGBA(x, z, 0);
                    continue;
                }

                // vanilla color exists; grab color from pl3xmap tile
                pl3xColor = this.image.getRGB(x, z);
                if (pl3xColor == 0) {
                    // pl3xmap color is missing; fallback to vanilla color
                    pixels.setPixelRGBA(x, z, MapColor.getColorFromPackedId(color));
                } else {
                    // draw pl3xmap tile pixel
                    pixels.setPixelRGBA(x, z, pl3xColor);
                }
            }
        }

        // finalize the texture
        this.texture.upload();
        return true;
    }
}
