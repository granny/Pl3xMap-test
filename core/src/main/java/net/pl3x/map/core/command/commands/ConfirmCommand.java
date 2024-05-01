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
package net.pl3x.map.core.command.commands;

import com.google.common.cache.CacheBuilder;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.pl3x.map.core.command.CommandHandler;
import net.pl3x.map.core.command.Pl3xMapCommand;
import net.pl3x.map.core.command.Sender;
import net.pl3x.map.core.configuration.Lang;
import org.incendo.cloud.description.CommandDescription;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.processors.cache.CloudCache;
import org.incendo.cloud.processors.cache.GuavaCache;
import org.incendo.cloud.processors.confirmation.ConfirmationConfiguration;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.jetbrains.annotations.NotNull;

public class ConfirmCommand extends Pl3xMapCommand {
    private final ConfirmationManager<@NotNull Sender> confirmationManager = ConfirmationManager.of(
            ConfirmationConfiguration.<Sender>builder()
                    .cache(GuavaCache.of(CacheBuilder.newBuilder().build()))
                    .noPendingCommandNotifier(sender -> sender.sendMessage(Lang.COMMAND_CONFIRM_NO_PENDING_MESSAGE))
                    .confirmationRequiredNotifier((sender, senderConfirmationContext) -> sender.sendMessage(
                            Component.text().append(Lang.parse(Lang.COMMAND_CONFIRM_CONFIRMATION_REQUIRED_MESSAGE))
                                    .hoverEvent(Lang.parse(Lang.CLICK_TO_CONFIRM))
                                    .clickEvent(ClickEvent.runCommand("/map confirm"))
                    ))
                    .expiration(Duration.ofSeconds(15L))
                    .build()
    );

    public ConfirmCommand(@NotNull CommandHandler handler) {
        super(handler);
    }

    @Override
    public void register() {
        getHandler().getManager().registerCommandPostProcessor(this.confirmationManager.createPostprocessor());

        getHandler().registerSubcommand(builder -> builder.literal("confirm")
                .commandDescription(RichDescription.of(Lang.parse(Lang.COMMAND_CONFIRM_DESCRIPTION)))
                .permission("pl3xmap.command.confirm")
                .handler(this.confirmationManager.createExecutionHandler()));
    }
}
