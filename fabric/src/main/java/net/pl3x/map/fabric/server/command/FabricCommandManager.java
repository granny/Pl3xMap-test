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
package net.pl3x.map.fabric.server.command;

import io.leangen.geantyref.TypeToken;
import net.minecraft.commands.arguments.DimensionArgument;
import net.pl3x.map.core.command.CommandHandler;
import net.pl3x.map.core.command.Sender;
import net.pl3x.map.core.command.parser.PlatformParsers;
import net.pl3x.map.core.command.parser.WorldParser;
import org.incendo.cloud.Command;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;
import org.jetbrains.annotations.NotNull;

public class FabricCommandManager implements CommandHandler {
    private final FabricServerCommandManager<@NotNull Sender> manager;
    private final Command.Builder<@NotNull Sender> root;

    public FabricCommandManager() {
        this.manager = new FabricServerCommandManager<Sender>(
                ExecutionCoordinator.simpleCoordinator(),
                SenderMapper.create(FabricSender::create, Sender::getSender)
        );

        CloudBrigadierManager<@NotNull Sender, ?> brigadier = getManager().brigadierManager();
        brigadier.setNativeNumberSuggestions(false);
        brigadier.registerMapping(new TypeToken<WorldParser<Sender>>() {
        }, builder -> builder.toConstant(DimensionArgument.dimension()).cloudSuggestions());

        setupExceptionHandlers();

        this.root = buildRoot();
        getManager().command(getRoot());
        registerSubcommands();
    }

    @Override
    public @NotNull FabricServerCommandManager<@NotNull Sender> getManager() {
        return this.manager;
    }

    @Override
    public @NotNull PlatformParsers getPlatformParsers() {
        return new FabricParsers();
    }

    @Override
    public Command.@NotNull Builder<@NotNull Sender> getRoot() {
        return this.root;
    }
}
