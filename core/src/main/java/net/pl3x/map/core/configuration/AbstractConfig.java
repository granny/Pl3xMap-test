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
package net.pl3x.map.core.configuration;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

public abstract class AbstractConfig {
    private YamlFile config;

    public @NotNull YamlFile getConfig() {
        return this.config;
    }

    protected void reload(@NotNull Path path, @NotNull Class<? extends @NotNull AbstractConfig> clazz) {
        // read yaml from file
        this.config = new YamlFile(path.toFile());
        try {
            getConfig().createOrLoadWithComments();
        } catch (InvalidConfigurationException e) {
            Logger.severe("Could not load " + path.getFileName() + ", please correct your syntax errors", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // load data from yaml
        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
            Key key = field.getDeclaredAnnotation(Key.class);
            Comment comment = field.getDeclaredAnnotation(Comment.class);
            if (key == null) {
                return;
            }
            try {
                Object obj = getClassObject();
                Object value = getValue(key.value(), field.get(obj));
                field.set(obj, value instanceof String str ? StringUtils.unescapeJava(str) : value);
                if (comment != null) {
                    setComment(key.value(), comment.value());
                }
            } catch (Throwable e) {
                Logger.warn("Failed to load " + key.value() + " from " + path.getFileName().toString(), e);
            }
        });

        save();
    }

    protected void save() {
        // save yaml to disk
        try {
            getConfig().save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected @Nullable Object getClassObject() {
        return null;
    }

    protected @Nullable Object getValue(@NotNull String path, @Nullable Object def) {
        if (getConfig().get(path) == null) {
            set(path, def);
        }
        return get(path, def);
    }

    protected void setComment(@NotNull String path, @Nullable String comment) {
        getConfig().setComment(path, comment, CommentType.BLOCK);
    }

    protected @Nullable Object get(@NotNull String path, @Nullable Object def) {
        Object val = get(path);
        return val == null ? def : val;
    }

    protected @Nullable Object get(@NotNull String path) {
        Object value = getConfig().get(path);
        if (!(value instanceof ConfigurationSection section)) {
            return value;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            String rawValue = section.getString(key);
            if (rawValue == null) {
                continue;
            }
            map.put(key, addToMap(rawValue));
        }
        return map;
    }

    protected @NotNull Object addToMap(@NotNull String rawValue) {
        return rawValue;
    }

    protected void set(@NotNull String path, @Nullable Object value) {
        getConfig().set(path, value);
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Key {
        @NotNull String value();
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Comment {
        @NotNull String value();
    }
}
