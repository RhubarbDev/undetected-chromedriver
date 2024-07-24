package Utils;

import LooseVersion.LooseVersion;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public final class PatcherUtil {

    private static final char[] asciiLower = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    private static final Random rand = new Random();

    public enum OSType {
        WINDOWS,
        MACOS,
        LINUX,
        OTHER
    }

    // only fetch the jsonObject once.
    private static JsonObject jsonObject = null;

    // if something stops working, this might have changed.
    private static final String urlRepo = "https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions-with-downloads.json";

    public static JsonObject getJson() {
        if (jsonObject == null) {
            jsonObject = fetchDriverData();
        }
        return jsonObject;
    }

    private static OSType determineOS(String name) {
        OSType type = OSType.OTHER;

        if (name.contains("win")) {
            type = OSType.WINDOWS;
        } else if (name.contains("nix") || name.contains("nux") || name.contains("aix")) {
            type = OSType.LINUX;
        } else if (name.contains("mac")) {
            type = OSType.MACOS;
        }

        return type;
    }

    public static OSType determineOS() {
        return determineOS(System.getProperty("os.name"));
    }

    public static Path generatePath() {
        String path = null;

        switch (determineOS()) {
            case OSType.WINDOWS:
                path = "~/appdata/roaming/undetected_chromedriver";
                break;
            case OSType.LINUX:
                path = "~/.local/share/undetected_chromedriver";
                break;
            case OSType.MACOS:
                path = "~/Library/Application Support/undetected_chromedriver";
                break;
            default:
                path =  "~/.undetected_chromedriver";
                break;
        }

        if (System.getenv().containsKey("LAMBDA_TASK_ROOT")) {
            path = "/tmp/undetected_chromedriver";
        }

        return Paths.get(path.replaceFirst("^~", System.getProperty("user.home"))).toAbsolutePath();
    }

    private static JsonObject fetchDriverData() {
        StringBuilder builder = new StringBuilder();

        try {
            URL url = new URL(urlRepo);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        return new Gson().fromJson(builder.toString(), JsonObject.class);
    }

    public static String getURL() {
        JsonArray downloads = jsonObject.getAsJsonObject("channels").getAsJsonObject("Stable").getAsJsonObject("downloads").getAsJsonArray("chromedriver");
        String url = null;

        for (JsonElement download : downloads) {
            JsonObject obj = download.getAsJsonObject();
            if (determineOS(obj.get("platform").getAsString()) == determineOS()) {
                url = obj.get("url").getAsString();
                break;
            }
        }

        if (url == null) {
            throw new RuntimeException("Couldn't match OS to download URL.");
        }

        return url;
    }

    // pretty sure this isn't needed anymore, see PatcherUtil.DownloadChromeDriver
    public static LooseVersion fetchReleaseNumber() {
        if (jsonObject == null) {
            jsonObject = fetchDriverData();
        }

        String ver = null;
        try {
            ver = jsonObject.getAsJsonObject("channels").getAsJsonObject("Stable").get("version").getAsString();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        // I'm pretty sure this isn't needed, but whatever
        if (ver == null) {
            throw new RuntimeException("Couldn't parse version.");
        }

        return new LooseVersion(ver);
    }

    public static String generateCDC() {
        StringBuilder cdc = new StringBuilder();

        for(int i = 0; i < 27; i++) {
            cdc.append(asciiLower[rand.nextInt(asciiLower.length)]);
        }

        return cdc.toString();
    }
}