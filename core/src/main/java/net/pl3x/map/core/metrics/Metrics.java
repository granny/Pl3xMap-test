/*
 * MIT License
 *
 * Copyright (c) 2021 Bastian Oppermann
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
package net.pl3x.map.core.metrics;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.configuration.Config;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.file.YamlConfiguration;

public class Metrics {
    private final Pl3xMap pl3xmap;
    private final MetricsBase metricsBase;

    /**
     * Creates a new Metrics instance.
     *
     * @param pl3xmap The Pl3xMap API instance this metrics started with.
     */
    public Metrics(@NotNull Pl3xMap pl3xmap) throws IOException {
        this.pl3xmap = pl3xmap;
        // Get the config file
        Path bStatsFolder = this.pl3xmap.getMainDir().resolve("../bStats");
        File configFile = FileUtil.mkDirs(bStatsFolder.resolve("config.yml")).toFile();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (!config.isSet("serverUuid")) {
            config.addDefault("enabled", true);
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            config.addDefault("logFailedRequests", false);
            config.addDefault("logSentData", false);
            config.addDefault("logResponseStatusText", false);
            // Inform the server owners about bStats
            config.options().header("""
                            bStats (https://bStats.org) collects some basic information for plugin authors, like how
                            many people use their plugin and their total player count. It's recommended to keep bStats
                            enabled, but if you're not comfortable with this, you can turn this setting off. There is no
                            performance penalty associated with having metrics enabled, and data sent to bStats is fully
                            anonymous.""")
                    .copyDefaults(true);
            try {
                config.save(configFile);
            } catch (IOException ignored) {
            }
        }
        // Load the data
        boolean enabled = config.getBoolean("enabled", true);
        String serverUUID = config.getString("serverUuid");
        boolean logErrors = config.getBoolean("logFailedRequests", false);
        boolean logSentData = config.getBoolean("logSentData", false);
        boolean logResponseStatusText = config.getBoolean("logResponseStatusText", false);
        this.metricsBase = new MetricsBase(
                "bukkit", // report all data to the bukkit page
                serverUUID,
                enabled,
                this::appendPlatformData,
                this::appendServiceData,
                submitDataTask -> Pl3xMap.api().getScheduler().addTask(0, false, submitDataTask),
                this.pl3xmap::isEnabled,
                Logger::warn,
                Logger::info,
                logErrors,
                logSentData,
                logResponseStatusText);

        addCustomChart(new Metrics.SimplePie("unfiltered_server_software", this.pl3xmap::getPlatform));
        addCustomChart(new Metrics.SimplePie("language_used", () ->
                Config.LANGUAGE_FILE.replace("lang-", "").replace(".yml", "")
        ));
        addCustomChart(new Metrics.SimplePie("internal_web_server", () ->
                Config.HTTPD_ENABLED ? "true" : "false"
        ));
        addCustomChart(new Metrics.AdvancedPie("renderers_used", () -> {
            Map<String, Integer> map = new HashMap<>();
            this.pl3xmap.getWorldRegistry().forEach(world ->
                    world.getRenderers().forEach((id, builder) -> {
                        int count = map.getOrDefault(builder.getName(), 0);
                        map.put(builder.getName(), count + 1);
                    })
            );
            return map;
        }));
        addCustomChart(new Metrics.DrilldownPie("plugin_version", () -> {
            String[] version = this.pl3xmap.getVersion().split("-");
            return Map.of(version[0], Map.of(version[1], 1));
        }));
    }

    /**
     * Shuts down the underlying scheduler service.
     */
    public void shutdown() {
        this.metricsBase.shutdown();
    }

    /**
     * Adds a custom chart.
     *
     * @param chart The chart to add.
     */
    public void addCustomChart(@NotNull CustomChart chart) {
        this.metricsBase.addCustomChart(chart);
    }

    private void appendPlatformData(@NotNull JsonObjectBuilder builder) {
        builder.appendField("playerAmount", this.pl3xmap.getPlayerRegistry().size());
        builder.appendField("onlineMode", this.pl3xmap.getOnlineMode() ? 1 : 0);
        builder.appendField("bukkitVersion", this.pl3xmap.getServerVersion());
        builder.appendField("bukkitName", this.pl3xmap.getPlatform());
        builder.appendField("javaVersion", System.getProperty("java.version"));
        builder.appendField("osName", System.getProperty("os.name"));
        builder.appendField("osArch", System.getProperty("os.arch"));
        builder.appendField("osVersion", System.getProperty("os.version"));
        builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
    }

    private void appendServiceData(@NotNull JsonObjectBuilder builder) {
        builder.appendField("pluginVersion", this.pl3xmap.getVersion());
    }

    public static class MetricsBase {
        public static final String METRICS_VERSION = "3.0.2";

        private static final String REPORT_URL = "https://bStats.org/api/v2/data/%s";

        private final ScheduledExecutorService scheduler;
        private final String platform;
        private final String serverUuid;
        private final Consumer<JsonObjectBuilder> appendPlatformDataConsumer;
        private final Consumer<JsonObjectBuilder> appendServiceDataConsumer;
        private final Consumer<Runnable> submitTaskConsumer;
        private final Supplier<Boolean> checkServiceEnabledSupplier;
        private final BiConsumer<String, Throwable> errorLogger;
        private final Consumer<String> infoLogger;
        private final boolean logErrors;
        private final boolean logSentData;
        private final boolean logResponseStatusText;
        private final Set<CustomChart> customCharts = new HashSet<>();
        private final boolean enabled;

        /**
         * Creates a new MetricsBase class instance.
         *
         * @param platform                    The platform of the service.
         * @param serverUuid                  The server uuid.
         * @param enabled                     Whether data sending is enabled.
         * @param appendPlatformDataConsumer  A consumer that receives a {@code JsonObjectBuilder} and
         *                                    appends all platform-specific data.
         * @param appendServiceDataConsumer   A consumer that receives a {@code JsonObjectBuilder} and
         *                                    appends all service-specific data.
         * @param submitTaskConsumer          A consumer that takes a runnable with the submit task. This can be
         *                                    used to delegate the data collection to another thread to prevent errors caused by
         *                                    concurrency. Can be {@code null}.
         * @param checkServiceEnabledSupplier A supplier to check if the service is still enabled.
         * @param errorLogger                 A consumer that accepts log message and an error.
         * @param infoLogger                  A consumer that accepts info log messages.
         * @param logErrors                   Whether errors should be logged.
         * @param logSentData                 Whether the data sent should be logged.
         * @param logResponseStatusText       Whether the response status text should be logged.
         */
        public MetricsBase(
                @NotNull String platform,
                @NotNull String serverUuid,
                boolean enabled,
                @NotNull Consumer<@NotNull JsonObjectBuilder> appendPlatformDataConsumer,
                @NotNull Consumer<@NotNull JsonObjectBuilder> appendServiceDataConsumer,
                @NotNull Consumer<@NotNull Runnable> submitTaskConsumer,
                @NotNull Supplier<@NotNull Boolean> checkServiceEnabledSupplier,
                @NotNull BiConsumer<@NotNull String, @NotNull Throwable> errorLogger,
                @NotNull Consumer<@NotNull String> infoLogger,
                boolean logErrors,
                boolean logSentData,
                boolean logResponseStatusText) {
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, task -> new Thread(task, "bStats-Metrics"));
            // We want delayed tasks (non-periodic) that will execute in the future to be
            // cancelled when the scheduler is shutdown.
            // Otherwise, we risk preventing the server from shutting down even when
            // MetricsBase#shutdown() is called
            scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            this.scheduler = scheduler;
            this.platform = platform;
            this.serverUuid = serverUuid;
            this.enabled = enabled;
            this.appendPlatformDataConsumer = appendPlatformDataConsumer;
            this.appendServiceDataConsumer = appendServiceDataConsumer;
            this.submitTaskConsumer = submitTaskConsumer;
            this.checkServiceEnabledSupplier = checkServiceEnabledSupplier;
            this.errorLogger = errorLogger;
            this.infoLogger = infoLogger;
            this.logErrors = logErrors;
            this.logSentData = logSentData;
            this.logResponseStatusText = logResponseStatusText;
            if (enabled) {
                // WARNING: Removing the option to opt-out will get your plugin banned from bStats
                startSubmitting();
            }
        }

        /**
         * Gzips the given string.
         *
         * @param str The string to gzip.
         * @return The gzipped string.
         */
        private static byte[] compress(@Nullable String str) throws IOException {
            if (str == null) {
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
                gzip.write(str.getBytes(StandardCharsets.UTF_8));
            }
            return outputStream.toByteArray();
        }

        public void addCustomChart(@NotNull CustomChart chart) {
            this.customCharts.add(chart);
        }

        public void shutdown() {
            this.scheduler.shutdown();
        }

        private void startSubmitting() {
            final Runnable submitTask =
                    () -> {
                        if (!this.enabled || !this.checkServiceEnabledSupplier.get()) {
                            // Submitting data or service is disabled
                            this.scheduler.shutdown();
                            return;
                        }
                        if (this.submitTaskConsumer != null) {
                            this.submitTaskConsumer.accept(this::submitData);
                        } else {
                            this.submitData();
                        }
                    };
            // Many servers tend to restart at a fixed time at xx:00 which causes an uneven distribution of requests on the
            // bStats backend. To circumvent this problem, we introduce some randomness into the initial and second delay.
            // WARNING: You must not modify and part of this Metrics class, including the submit delay or frequency!
            // WARNING: Modifying this code will get your plugin banned on bStats. Just don't do it!
            long initialDelay = (long) (1000 * 60 * (3 + Math.random() * 3));
            long secondDelay = (long) (1000 * 60 * (Math.random() * 30));
            this.scheduler.schedule(submitTask, initialDelay, TimeUnit.MILLISECONDS);
            this.scheduler.scheduleAtFixedRate(submitTask, initialDelay + secondDelay, 1000 * 60 * 30, TimeUnit.MILLISECONDS);
        }

        private void submitData() {
            final JsonObjectBuilder builder = new JsonObjectBuilder();
            this.appendPlatformDataConsumer.accept(builder);
            JsonObjectBuilder.JsonObject[] chartData = this.customCharts.stream()
                    .map(customChart -> customChart.getRequestJsonObject(this.errorLogger, this.logErrors))
                    .filter(Objects::nonNull)
                    .toArray(JsonObjectBuilder.JsonObject[]::new);
            final JsonObjectBuilder serviceJsonBuilder = new JsonObjectBuilder()
                    .appendField("id", 10133)
                    .appendField("customCharts", chartData);
            this.appendServiceDataConsumer.accept(serviceJsonBuilder);
            builder.appendField("service", serviceJsonBuilder.build());
            builder.appendField("serverUUID", this.serverUuid);
            builder.appendField("metricsVersion", METRICS_VERSION);
            JsonObjectBuilder.JsonObject data = builder.build();
            this.scheduler.execute(() -> {
                try {
                    // Send the data
                    sendData(data);
                } catch (Exception e) {
                    // Something went wrong! :(
                    if (this.logErrors) {
                        this.errorLogger.accept("Could not submit bStats metrics data", e);
                    }
                }
            });
        }

        private void sendData(JsonObjectBuilder.@NotNull JsonObject data) throws Exception {
            if (this.logSentData) {
                this.infoLogger.accept("Sent bStats metrics data: " + data);
            }
            String url = String.format(REPORT_URL, this.platform);
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            // Compress the data to save bandwidth
            byte[] compressedData = compress(data.toString());
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("Connection", "close");
            connection.addRequestProperty("Content-Encoding", "gzip");
            connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Metrics-Service/1");
            connection.setDoOutput(true);
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.write(compressedData);
            }
            StringBuilder builder = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
            }
            if (this.logResponseStatusText) {
                this.infoLogger.accept("Sent data to bStats and received response: " + builder);
            }
        }
    }

    public static class SimplePie extends CustomChart {
        private final Callable<@Nullable String> callable;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public SimplePie(@NotNull String chartId, @NotNull Callable<@Nullable String> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObjectBuilder.@Nullable JsonObject getChartData() throws Exception {
            String value = this.callable.call();
            if (value == null || value.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            return new JsonObjectBuilder().appendField("value", value).build();
        }
    }

    public static class AdvancedPie extends CustomChart {
        private final Callable<@Nullable Map<@NotNull String, @NotNull Integer>> callable;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public AdvancedPie(@NotNull String chartId, @NotNull Callable<@Nullable Map<@NotNull String, @NotNull Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObjectBuilder.@Nullable JsonObject getChartData() throws Exception {
            JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
            Map<@NotNull String, @NotNull Integer> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            boolean allSkipped = true;
            for (Map.Entry<@NotNull String, @NotNull Integer> entry : map.entrySet()) {
                if (entry.getValue() == 0) {
                    // Skip this invalid
                    continue;
                }
                allSkipped = false;
                valuesBuilder.appendField(entry.getKey(), entry.getValue());
            }
            if (allSkipped) {
                // Null = skip the chart
                return null;
            }
            return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
        }
    }

    public static class DrilldownPie extends CustomChart {
        private final Callable<@Nullable Map<@NotNull String, @NotNull Map<@NotNull String, @NotNull Integer>>> callable;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public DrilldownPie(@NotNull String chartId, @NotNull Callable<@Nullable Map<@NotNull String, @NotNull Map<@NotNull String, @NotNull Integer>>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        public JsonObjectBuilder.JsonObject getChartData() throws Exception {
            JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
            Map<@NotNull String, @NotNull Map<@NotNull String, @NotNull Integer>> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            boolean reallyAllSkipped = true;
            for (Map.Entry<@NotNull String, @NotNull Map<@NotNull String, @NotNull Integer>> entryValues : map.entrySet()) {
                JsonObjectBuilder valueBuilder = new JsonObjectBuilder();
                boolean allSkipped = true;
                for (Map.Entry<@NotNull String, @NotNull Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
                    valueBuilder.appendField(valueEntry.getKey(), valueEntry.getValue());
                    allSkipped = false;
                }
                if (!allSkipped) {
                    reallyAllSkipped = false;
                    valuesBuilder.appendField(entryValues.getKey(), valueBuilder.build());
                }
            }
            if (reallyAllSkipped) {
                // Null = skip the chart
                return null;
            }
            return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
        }
    }

    public abstract static class CustomChart {
        private final String chartId;

        protected CustomChart(@Nullable String chartId) {
            if (chartId == null) {
                throw new IllegalArgumentException("chartId must not be null");
            }
            this.chartId = chartId;
        }

        public JsonObjectBuilder.@Nullable JsonObject getRequestJsonObject(@NotNull BiConsumer<@NotNull String, @NotNull Throwable> errorLogger, boolean logErrors) {
            JsonObjectBuilder builder = new JsonObjectBuilder();
            builder.appendField("chartId", this.chartId);
            try {
                JsonObjectBuilder.JsonObject data = getChartData();
                if (data == null) {
                    // If the data is null we don't send the chart.
                    return null;
                }
                builder.appendField("data", data);
            } catch (Throwable t) {
                if (logErrors) {
                    errorLogger.accept("Failed to get data for custom chart with id " + this.chartId, t);
                }
                return null;
            }
            return builder.build();
        }

        protected abstract JsonObjectBuilder.@Nullable JsonObject getChartData() throws Exception;
    }

    /**
     * An extremely simple JSON builder.
     *
     * <p>While this class is neither feature-rich nor the most performant one, it's sufficient for its use-case.
     */
    public static class JsonObjectBuilder {
        private StringBuilder builder = new StringBuilder();
        private boolean hasAtLeastOneField = false;

        public JsonObjectBuilder() {
            this.builder.append("{");
        }

        /**
         * Escapes the given string like stated in <a href="https://www.ietf.org/rfc/rfc4627.txt">rfc4627</a>.
         *
         * <p>This method escapes only the necessary characters '"', '\'. and '\u0000' - '\u001F'.
         * Compact escapes are not used (e.g., '\n' is escaped as "\u000a" and not as "\n").
         *
         * @param value The value to escape.
         * @return The escaped value.
         */
        @SuppressWarnings("UnnecessaryUnicodeEscape")
        private static @NotNull String escape(@NotNull String value) {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (c == '"') {
                    builder.append("\\\"");
                } else if (c == '\\') {
                    builder.append("\\\\");
                } else if (c <= '\u000F') {
                    builder.append("\\u000").append(Integer.toHexString(c));
                } else if (c <= '\u001F') {
                    builder.append("\\u00").append(Integer.toHexString(c));
                } else {
                    builder.append(c);
                }
            }
            return builder.toString();
        }

        /**
         * Appends a string field to the JSON.
         *
         * @param key   The key of the field.
         * @param value The value of the field.
         * @return A reference to this object.
         */
        public @NotNull JsonObjectBuilder appendField(@NotNull String key, @Nullable String value) {
            if (value == null) {
                throw new IllegalArgumentException("JSON value must not be null");
            }
            appendFieldUnescaped(key, "\"" + escape(value) + "\"");
            return this;
        }

        /**
         * Appends an integer field to the JSON.
         *
         * @param key   The key of the field.
         * @param value The value of the field.
         * @return A reference to this object.
         */
        public @NotNull JsonObjectBuilder appendField(@NotNull String key, int value) {
            appendFieldUnescaped(key, String.valueOf(value));
            return this;
        }

        /**
         * Appends an object to the JSON.
         *
         * @param key    The key of the field.
         * @param object The object.
         * @return A reference to this object.
         */
        public @NotNull JsonObjectBuilder appendField(@NotNull String key, @Nullable JsonObject object) {
            if (object == null) {
                throw new IllegalArgumentException("JSON object must not be null");
            }
            appendFieldUnescaped(key, object.toString());
            return this;
        }

        /**
         * Appends an object array to the JSON.
         *
         * @param key    The key of the field.
         * @param values The integer array.
         * @return A reference to this object.
         */
        public @NotNull JsonObjectBuilder appendField(@NotNull String key, @NotNull JsonObject[] values) {
            if (values == null) {
                throw new IllegalArgumentException("JSON values must not be null");
            }
            String escapedValues = Arrays.stream(values)
                    .map(JsonObject::toString)
                    .collect(Collectors.joining(","));
            appendFieldUnescaped(key, "[" + escapedValues + "]");
            return this;
        }

        /**
         * Appends a field to the object.
         *
         * @param key          The key of the field.
         * @param escapedValue The escaped value of the field.
         */
        private void appendFieldUnescaped(@Nullable String key, @Nullable String escapedValue) {
            if (this.builder == null) {
                throw new IllegalStateException("JSON has already been built");
            }
            if (key == null) {
                throw new IllegalArgumentException("JSON key must not be null");
            }
            if (this.hasAtLeastOneField) {
                this.builder.append(",");
            }
            this.builder.append("\"").append(escape(key)).append("\":").append(escapedValue);
            this.hasAtLeastOneField = true;
        }

        /**
         * Builds the JSON string and invalidates this builder.
         *
         * @return The built JSON string.
         */
        public @NotNull JsonObject build() {
            if (this.builder == null) {
                throw new IllegalStateException("JSON has already been built");
            }
            JsonObject object = new JsonObject(this.builder.append("}").toString());
            this.builder = null;
            return object;
        }

        /**
         * A super simple representation of a JSON object.
         *
         * <p>This class only exists to make methods of the {@link JsonObjectBuilder} type-safe and not
         * allow a raw string inputs for methods like {@link JsonObjectBuilder#appendField(String,
         * JsonObject)}.
         */
        public static class JsonObject {
            private final String value;

            private JsonObject(@NotNull String value) {
                this.value = value;
            }

            @Override
            public @NotNull String toString() {
                return value;
            }
        }
    }
}
