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
package net.pl3x.map.core.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.pl3x.map.core.log.Logger;
import org.jetbrains.annotations.NotNull;

public class EventRegistry {
    public void callEvent(@NotNull Event event) {
        for (RegisteredHandler handler : event.getHandlers()) {
            try {
                Logger.debug("Executing Event " + handler);
                handler.execute(event);
            } catch (Throwable t) {
                Logger.severe("Failed to execute Event " + handler, t);
            }
        }
    }

    public void register(@NotNull EventListener listener) {
        for (Method method : listener.getClass().getMethods()) {
            if (method.getDeclaredAnnotation(EventHandler.class) == null) {
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 0) {
                Logger.warn(String.format("Annotated EventListener does not have an Event param %s@%s", listener.getClass(), method.getName()));
                continue;
            }
            Class<?> event = params[0];
            if (!Event.class.isAssignableFrom(event)) {
                Logger.warn(String.format("%s is not an event", event.getName()));
                continue;
            }
            try {
                Field handlers = event.getDeclaredField("handlers");
                handlers.setAccessible(true);

                @SuppressWarnings("unchecked")
                List<RegisteredHandler> list = (List<RegisteredHandler>) handlers.get(event);

                RegisteredHandler handler = new RegisteredHandler(listener, method);
                if (list.remove(handler)) {
                    Logger.debug("Remove previously registered event " + handler);
                }
                list.add(handler);
                Logger.debug("Registered new addon event " + handler);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
