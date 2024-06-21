/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.pl3x.map.core.markers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.pl3x.map.core.markers.option.Option;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a wrapped {@link JsonObject} with extra '<code>addProperty</code>' methods,
 * because Google made it a final class for some weird reason.
 * <p>
 * Null values cannot be added to this JsonObject, they will simply be ignored when added.
 */
@SuppressWarnings("unused")
public class JsonObjectWrapper {
    private final JsonObject wrapped;

    /**
     * Creates an empty JsonObjectWrapper.
     */
    public JsonObjectWrapper() {
        this.wrapped = new JsonObject();
    }

    /**
     * Get the underlying JsonObject.
     *
     * @return JsonObject
     */
    public @NotNull JsonObject getJsonObject() {
        return this.wrapped;
    }

    // Let's add some stuff

    /**
     * Convenience method to add a primitive member. The specified value is converted to a
     * JsonPrimitive of String.
     *
     * @param property name of the member.
     * @param value    the enum value associated with the member.
     */
    public void addProperty(@NotNull String property, @Nullable Enum<?> value) {
        if (value == null) {
            return;
        }
        getJsonObject().addProperty(property, value.ordinal());
    }

    /**
     * Convenience method to add a primitive member. The specified value is converted to a
     * JsonPrimitive of String.
     *
     * @param property name of the member.
     * @param value    the json serializable value associated with the member.
     */
    public void addProperty(@NotNull String property, @Nullable JsonSerializable value) {
        if (value == null) {
            return;
        }
        getJsonObject().add(property, value.toJson());
    }

    /**
     * Convenience method to add a primitive member. The specified value is converted to a
     * JsonPrimitive of String.
     *
     * @param property name of the member.
     * @param value    the option value associated with the member.
     */
    public void addProperty(@NotNull String property, @Nullable Option<?> value) {
        if (value == null) {
            return;
        }
        getJsonObject().add(property, value.toJson());
    }

    /**
     * Convenience method to add a primitive member. The specified value is converted to a
     * JsonPrimitive of String.
     *
     * @param property name of the member.
     * @param value    the value associated with the member.
     */
    public void addProperty(@NotNull String property, @Nullable List<? extends @NotNull JsonSerializable> value) {
        if (value == null) {
            return;
        }
        JsonArray arr = new JsonArray();
        value.forEach(serializable -> {
            if (serializable == null) {
                return;
            }
            arr.add(serializable.toJson());
        });
        getJsonObject().add(property, arr);
    }

    // Now let's finish with the existing stuff from JsonObject

    /**
     * Convenience method to add a primitive member. The specified value is converted to a
     * JsonPrimitive of String.
     *
     * @param property name of the member.
     * @param value    the string value associated with the member.
     */
    public void addProperty(String property, String value) {
        if (value == null) {
            return;
        }
        getJsonObject().addProperty(property, value);
    }

    /**
     * Convenience method to add a primitive member. The specified value is converted to a
     * JsonPrimitive of Number.
     *
     * @param property name of the member.
     * @param value    the number value associated with the member.
     */
    public void addProperty(String property, Number value) {
        if (value == null) {
            return;
        }
        getJsonObject().addProperty(property, value);
    }

    /**
     * Convenience method to add a boolean member. The specified value is converted to a
     * JsonPrimitive of Boolean.
     *
     * @param property name of the member.
     * @param value    the boolean value associated with the member.
     */
    public void addProperty(String property, Boolean value) {
        if (value == null) {
            return;
        }
        getJsonObject().addProperty(property, value);
    }

    /**
     * Convenience method to add a char member. The specified value is converted to a
     * JsonPrimitive of Character.
     *
     * @param property name of the member.
     * @param value    the number value associated with the member.
     */
    public void addProperty(String property, Character value) {
        if (value == null) {
            return;
        }
        getJsonObject().addProperty(property, value);
    }

    /**
     * Adds a member, which is a name-value pair, to self. The name must be a String, but the value
     * can be an arbitrary JsonElement, thereby allowing you to build a full tree of JsonElements
     * rooted at this node.
     *
     * @param property name of the member.
     * @param value    the member object.
     */
    public void add(String property, JsonElement value) {
        if (value == null) {
            return;
        }
        getJsonObject().add(property, value);
    }

    /**
     * Removes the {@code property} from this {@link JsonObject}.
     *
     * @param property name of the member that should be removed.
     * @return the {@link JsonElement} object that is being removed.
     * @since 1.3
     */
    public JsonElement remove(String property) {
        return getJsonObject().remove(property);
    }

    /**
     * Returns a set of members of this object. The set is ordered, and the order is in which the
     * elements were added.
     *
     * @return a set of members of this object.
     */
    public Set<Map.Entry<String, JsonElement>> entrySet() {
        return getJsonObject().entrySet();
    }

    /**
     * Returns a set of members key values.
     *
     * @return a set of member keys as Strings
     * @since 2.8.1
     */
    public Set<String> keySet() {
        return getJsonObject().keySet();
    }

    /**
     * Returns the number of key/value pairs in the object.
     *
     * @return the number of key/value pairs in the object.
     */
    public int size() {
        return getJsonObject().size();
    }

    /**
     * Convenience method to check if a member with the specified name is present in this object.
     *
     * @param memberName name of the member that is being checked for presence.
     * @return true if there is a member with the specified name, false otherwise.
     */
    public boolean has(String memberName) {
        return getJsonObject().has(memberName);
    }

    /**
     * Returns the member with the specified name.
     *
     * @param memberName name of the member that is being requested.
     * @return the member matching the name. Null if no such member exists.
     */
    public JsonElement get(String memberName) {
        return getJsonObject().get(memberName);
    }

    /**
     * Convenience method to get the specified member as a JsonPrimitive element.
     *
     * @param memberName name of the member being requested.
     * @return the JsonPrimitive corresponding to the specified member.
     */
    public JsonPrimitive getAsJsonPrimitive(String memberName) {
        return getJsonObject().getAsJsonPrimitive(memberName);
    }

    /**
     * Convenience method to get the specified member as a JsonArray.
     *
     * @param memberName name of the member being requested.
     * @return the JsonArray corresponding to the specified member.
     */
    public JsonArray getAsJsonArray(String memberName) {
        return getJsonObject().getAsJsonArray(memberName);
    }

    /**
     * Convenience method to get the specified member as a JsonObject.
     *
     * @param memberName name of the member being requested.
     * @return the JsonObject corresponding to the specified member.
     */
    public JsonObject getAsJsonObject(String memberName) {
        return getJsonObject().getAsJsonObject(memberName);
    }

    /**
     * Creates a deep copy of this element and all its children
     *
     * @since 2.8.2
     */
    public JsonObject deepCopy() {
        return getJsonObject().deepCopy();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        JsonObject other;
        if (this.getClass() == o.getClass()) {
            other = ((JsonObjectWrapper) o).getJsonObject();
        } else if (JsonObject.class == o.getClass()) {
            other = (JsonObject) o;
        } else {
            return false;
        }
        return getJsonObject().equals(other);
    }

    @Override
    public int hashCode() {
        return getJsonObject().hashCode();
    }
}
