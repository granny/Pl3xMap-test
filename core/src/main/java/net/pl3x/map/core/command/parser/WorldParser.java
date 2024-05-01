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
package net.pl3x.map.core.command.parser;

import java.util.stream.Collectors;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.command.Sender;
import net.pl3x.map.core.command.exception.WorldParseException;
import net.pl3x.map.core.world.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Parser that parses strings into {@link World}s.
 *
 * @param <C> command sender type
 */
public class WorldParser<C> implements ArgumentParser<@NotNull C, @NotNull World>, BlockingSuggestionProvider.Strings<C> {
    public static <C> ParserDescriptor<C, World> parser() {
        return ParserDescriptor.of(new WorldParser<>(), World.class);
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull @NotNull World> parse(@NonNull CommandContext<@NonNull @NotNull C> commandContext, @NonNull CommandInput commandInput) {
        String input = commandInput.peekString();
        if (input == null) {
            return ArgumentParseResult.failure(new WorldParseException(null, WorldParseException.MUST_SPECIFY_WORLD));
        }

        World world = null;
        try {
            world = Pl3xMap.api().getWorldRegistry().get(input);
        } catch (Throwable ignore) {
        }

        if (world == null) {
            return ArgumentParseResult.failure(new WorldParseException(input, WorldParseException.NO_SUCH_WORLD));
        }

        if (!world.isEnabled()) {
            return ArgumentParseResult.failure(new WorldParseException(input, WorldParseException.MAP_NOT_ENABLED));
        }

        commandInput.readString();
        return ArgumentParseResult.success(world);
    }

    public static @NotNull World resolveWorld(@NotNull CommandContext<@NotNull Sender> context, @NotNull String name) {
        Sender sender = context.sender();
        World world = context.getOrDefault(name, null);
        if (world != null) {
            return world;
        }
        if (sender instanceof Sender.Player<?> player) {
            world = player.getWorld();
            if (world == null) {
                throw new WorldParseException("unknown", WorldParseException.NO_SUCH_WORLD);
            }
            if (!world.isEnabled()) {
                throw new WorldParseException(world.getName(), WorldParseException.MAP_NOT_ENABLED);
            } else {
                return world;
            }
        }
        throw new WorldParseException(null, WorldParseException.MUST_SPECIFY_WORLD);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<C> commandContext, @NonNull CommandInput input) {
        return Pl3xMap.api().getWorldRegistry()
                .values().stream()
                .filter(World::isEnabled)
                .map(World::getName)
                .collect(Collectors.toList());
    }
}
