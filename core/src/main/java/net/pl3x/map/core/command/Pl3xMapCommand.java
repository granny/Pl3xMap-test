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
package net.pl3x.map.core.command;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.pl3x.map.core.configuration.Lang;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Pl3xMap command.
 */
public abstract class Pl3xMapCommand {
    private final CommandHandler handler;

    protected Pl3xMapCommand(@NotNull CommandHandler handler) {
        this.handler = handler;
    }

    /**
     * Get the command handler.
     *
     * @return command handler
     */
    public @NotNull CommandHandler getHandler() {
        return this.handler;
    }

    /**
     * Register subcommand.
     */
    public abstract void register();

    /**
     * Create a command description.
     *
     * @param description  description of command
     * @param placeholders placeholders
     * @return rich description
     */
    protected static @NotNull RichDescription description(@NotNull String description, @NotNull TagResolver.@NotNull Single... placeholders) {
        return RichDescription.of(Lang.parse(description, placeholders));
    }
}
