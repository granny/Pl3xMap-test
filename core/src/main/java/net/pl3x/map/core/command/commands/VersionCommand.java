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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.command.CommandHandler;
import net.pl3x.map.core.command.Pl3xMapCommand;
import net.pl3x.map.core.command.Sender;
import net.pl3x.map.core.configuration.Lang;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.jetbrains.annotations.NotNull;

public class VersionCommand extends Pl3xMapCommand {
    private static final String URL = "https://api.modrinth.com/v2/project/pl3xmap/version?featured=true&game_versions=[%%22%s%%22]&loaders=[%%22%s%%22]";
    private static final String MODRINTH = "<click:open_url:https://modrinth.com/mod/pl3xmap>https://modrinth.com/mod/pl3xmap</click>";

    private String version;
    private long lastChecked;

    public VersionCommand(@NotNull CommandHandler handler) {
        super(handler);
    }

    @Override
    public void register() {
        getHandler().registerSubcommand(builder -> builder.literal("version")
                .commandDescription(RichDescription.of(Lang.parse(Lang.COMMAND_VERSION_DESCRIPTION)))
                .permission("pl3xmap.command.version")
                .handler(this::execute));
    }

    public void execute(@NotNull CommandContext<@NotNull Sender> context) {
        Sender sender = context.sender();

        long now = System.currentTimeMillis();
        if (this.lastChecked + TimeUnit.SECONDS.toMillis(15) > now) {
            showVersion(sender);
            return;
        }
        this.version = "-1";
        this.lastChecked = now;

        sender.sendMessage(Lang.COMMAND_VERSION_PLEASE_WAIT);

        String url = String.format(URL,
                Pl3xMap.api().getVersion().split("-")[0],
                Pl3xMap.api().getPlatform()
        );

        HttpClient.newHttpClient()
                .sendAsync(
                        HttpRequest.newBuilder().uri(URI.create(url)).build(),
                        HttpResponse.BodyHandlers.ofString()
                )
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    JsonElement elem = JsonParser.parseString(json);
                    if (elem.isJsonArray()) {
                        JsonArray arr = elem.getAsJsonArray();
                        if (!arr.isEmpty()) {
                            JsonElement elem1 = arr.get(0);
                            if (elem1.isJsonObject()) {
                                JsonObject obj = elem1.getAsJsonObject();
                                JsonElement ver = obj.get("version_number");
                                if (ver.isJsonPrimitive()) {
                                    this.version = ver.getAsString();
                                } else {
                                    this.version = "-5";
                                    this.lastChecked = 0;
                                }
                            } else {
                                this.version = "-4";
                                this.lastChecked = 0;
                            }
                        } else {
                            this.version = "-3";
                            this.lastChecked = 0;
                        }
                    } else {
                        this.version = "-2";
                        this.lastChecked = 0;
                    }
                    showVersion(sender);
                });
    }

    private void showVersion(Sender sender) {
        sender.sendMessage(Lang.COMMAND_VERSION_SUCCESS,
                Placeholder.unparsed("version", Pl3xMap.api().getVersion()),
                Placeholder.unparsed("platform", Pl3xMap.api().getPlatform()),
                Placeholder.unparsed("commit", Pl3xMap.api().getVersionCommit())
        );

        if (this.version.startsWith("-")) {
            sender.sendMessage(switch (this.version) {
                case "-1" -> Lang.COMMAND_VERSION_STILL_CHECKING;
                case "-2" -> Lang.COMMAND_VERSION_ERROR_NOT_ARRAY;
                case "-3", "-4" -> Lang.COMMAND_VERSION_ERROR_CORRUPT_JSON;
                default -> Lang.COMMAND_VERSION_ERROR_UNKNOWN_VERSION;
            });
            return;
        }

        int cur_build;
        int new_build;

        try {
            new_build = Integer.parseInt(this.version.split("-")[1]);
        } catch (Throwable t) {
            sender.sendMessage(Lang.COMMAND_VERSION_ERROR_UNABLE_TO_DETERMINE);
            this.lastChecked = 0;
            return;
        }

        try {
            cur_build = Integer.parseInt(Pl3xMap.api().getVersion().split("-")[1]);
        } catch (Throwable e) {
            sender.sendMessage(Lang.COMMAND_VERSION_SNAPSHOT);
            sender.sendMessage(Lang.COMMAND_VERSION_LATEST_BUILD_IS,
                    Placeholder.unparsed("build", String.valueOf(new_build)));
            return;
        }

        if (new_build == cur_build) {
            sender.sendMessage(Lang.COMMAND_VERSION_RUNNING_LATEST_BUILD);
            return;
        }

        sender.sendMessage(Lang.COMMAND_VERSION_BUILDS_BEHIND,
                Placeholder.unparsed("number", String.valueOf(new_build - cur_build)));

        if (cur_build > new_build) {
            sender.sendMessage(Lang.COMMAND_VERSION_TIME_TRAVELER);
        } else {
            sender.sendMessage(Lang.COMMAND_VERSION_DOWNLOAD,
                    Placeholder.parsed("link", MODRINTH));
        }
    }
}
