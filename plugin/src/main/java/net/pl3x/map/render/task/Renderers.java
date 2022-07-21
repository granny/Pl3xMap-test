package net.pl3x.map.render.task;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import net.pl3x.map.render.task.builtin.BasicRenderer;
import net.pl3x.map.render.task.builtin.BiomeRenderer;

public class Renderers {
    public static final Renderers INSTANCE = new Renderers();

    public static final Class<? extends Renderer> BASIC = INSTANCE.register("basic", BasicRenderer.class);
    public static final Class<? extends Renderer> BIOMES = INSTANCE.register("biomes", BiomeRenderer.class);

    private final Map<String, Class<? extends Renderer>> renderers = new HashMap<>();

    private Renderers() {
    }

    public Class<? extends Renderer> register(String name, Class<? extends Renderer> scanner) {
        if (this.renderers.containsKey(name)) {
            throw new IllegalStateException(String.format("Renderer already registered with name %s", name));
        }
        this.renderers.put(name, scanner);
        return scanner;
    }

    public Class<? extends Renderer> unregister(String name) {
        if (!this.renderers.containsKey(name)) {
            throw new IllegalStateException(String.format("No renderer registered with name %s", name));
        }
        return this.renderers.remove(name);
    }

    public Renderer createRenderer(String name, ScanTask scanTask) {
        Class<? extends Renderer> clazz = this.renderers.get(name);
        if (clazz == null) {
            throw new IllegalStateException(String.format("No renderer registered with name %s", name));
        }

        try {
            Constructor<? extends Renderer> ctor = clazz.getConstructor(String.class, ScanTask.class);
            return ctor.newInstance(name, scanTask);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
