package me.romix.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.romix.Main;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class UpdateChecker implements Runnable {

    private static final String API_ENDPOINT =
            "https://api.modrinth.com/v2/project/%s/version";

    private static final int TIMEOUT_MS = 6_000;

    private final Main   plugin;
    private final Logger logger;

    public UpdateChecker(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @Override
    public void run() {
        if (Main.MODRINTH_ID.equals("idek")) {
            return;
        }

        try {
            URL url = new URL(String.format(API_ENDPOINT, Main.MODRINTH_ID));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent",
                    "DirtyDetect/" + Main.VERSION + " (by " + Main.AUTHOR + ")");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setDoInput(true);

            int status = connection.getResponseCode();
            if (status != 200) {
                return;
            }

            JsonArray versions = JsonParser
                    .parseReader(new InputStreamReader(connection.getInputStream()))
                    .getAsJsonArray();

            if (versions == null || versions.isEmpty()) {
                return;
            }

            JsonElement first = versions.get(0);
            if (!first.isJsonObject()) return;

            String latestVersion = first.getAsJsonObject()
                    .get("version_number")
                    .getAsString();

            plugin.setLatestVersion(latestVersion);

            if (plugin.isUpdateAvailable()) {
                logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                logger.info("  Update available: v" + latestVersion
                        + "  (running v" + Main.VERSION + ")");
                logger.info("  Download: " + Main.MODRINTH_URL);
                logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }

        } catch (Exception e) {
            return;
        }
    }
}