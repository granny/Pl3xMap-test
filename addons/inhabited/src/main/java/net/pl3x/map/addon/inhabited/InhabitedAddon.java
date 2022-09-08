package net.pl3x.map.addon.inhabited;

import net.pl3x.map.Pl3xMap;
import net.pl3x.map.addon.Addon;
import net.pl3x.map.addon.inhabited.renderer.InhabitedRenderer;
import net.pl3x.map.render.RendererRegistry;

public class InhabitedAddon extends Addon {
    @Override
    public void onEnable() {
        // register our custom renderer with Pl3xMap
        RendererRegistry registry = Pl3xMap.api().getRendererRegistry();
        registry.register("inhabited", InhabitedRenderer.class);
    }

    @Override
    public void onDisable() {
        // unregister our custom renderer from Pl3xMap
        RendererRegistry registry = Pl3xMap.api().getRendererRegistry();
        registry.unregister("inhabited");
    }
}