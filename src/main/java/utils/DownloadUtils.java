package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import driver.LooseVersion;
import org.apache.commons.io.IOUtils;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class DownloadUtils {
    private static final String JSON_ENDPOINT = "https://googlechromelabs.github.io/chrome-for-testing/known-good-versions-with-downloads.json";
    private static final String LEGACY_ENDPOINT = "https://chromedriver.storage.googleapis.com/";

    private static JsonObject jsonObject = null;

    public static JsonObject getJson(LooseVersion version) {
        if (jsonObject == null) {
            jsonObject = fetchJson(version);
            if (jsonObject == null) {
                throw new RuntimeException("Cannot find data for version: " + version.toString());
            }
        }
        return jsonObject;
    }

    private static JsonObject fetchJson(LooseVersion version) {
        JsonObject[] obj;
        try {
            URL url = new URI(JSON_ENDPOINT).toURL();
            String data = IOUtils.toString(url, StandardCharsets.UTF_8);

            JsonArray array = JsonParser.parseString(data).getAsJsonObject().get("versions").getAsJsonArray();
            obj = new JsonObject[array.size()];
            for (int i = 0; i < array.size(); i++) {
                obj[i] = array.get(i).getAsJsonObject();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getCause());
        }

        if(obj.length < 1) {
            throw new RuntimeException("Array Empty.");
        }

        // Check the latest version, before searching the entire array.
        LooseVersion latest = new LooseVersion(obj[0].get("version").getAsString());

        if (latest.equals(version)) {
            return obj[0];
        }

        int low = 0;
        int high = obj.length - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;

            LooseVersion objVersion = new LooseVersion(obj[mid].get("version").getAsString());

            if (objVersion.equals(version)) {
                return obj[mid];
            }

            if (objVersion.compareTo(version) < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return null;
    }

    private static String legacyUrl(String fileName, LooseVersion version) {
        LooseVersion validVersion;

        try {
            validVersion = new LooseVersion(IOUtils.toString(new URI(LEGACY_ENDPOINT + "LATEST_RELEASE_" + version.getPart(0).toString()).toURL(), StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        return LEGACY_ENDPOINT + validVersion + "/" + fileName;
    }

    public static String getURL(OSUtils.OSInfo osInfo, LooseVersion version) {
        JsonObject downloads = getJson(version).getAsJsonObject("downloads");

        String url = null;

        // versions lower than 115 don't have chromedriver
        if (!downloads.has("chromedriver")) {
            return legacyUrl(osInfo.legacyFile(), version);
        }

        JsonArray versions = downloads.getAsJsonArray("chromedriver");

        for (JsonElement driverVersion : versions) {
            JsonObject obj = driverVersion.getAsJsonObject();
            if (OSUtils.getOS(obj.get("platform").getAsString()).os() == osInfo.os()) {
                url = obj.get("url").getAsString();
                break;
            }
        }

        if (url == null) {
            throw new RuntimeException("Couldn't match OS to download URL.");
        }

        return url;
    }
}
