package Utils;

import LooseVersion.LooseVersion;
import com.google.gson.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    private static final String jsonEndpoint = "https://googlechromelabs.github.io/chrome-for-testing/known-good-versions-with-downloads.json";

    public static JsonObject getJson() {
        if (jsonObject == null) {
            jsonObject = fetchJson();
            if (jsonObject == null) {
                throw new RuntimeException("Cannot find data for version: " + getInstalledChromeVersion());
            }
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

    private static JsonObject fetchJson() {
        JsonObject[] obj;
        try {
            URL url = new URI(jsonEndpoint).toURL();
            String data = IOUtils.toString(url, StandardCharsets.UTF_8);

            JsonArray array = JsonParser.parseString(data).getAsJsonObject().get("versions").getAsJsonArray();
            obj = new JsonObject[array.size()];
            for (int i = 0; i < array.size(); i++) {
                obj[i] = array.get(i).getAsJsonObject();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getCause());
        }

        LooseVersion chromeVersion = getInstalledChromeVersion();

        int low = 0, high = obj.length - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;

            LooseVersion objVersion = new LooseVersion(obj[mid].get("version").getAsString());

            if (objVersion.equals(chromeVersion)) {
                return obj[mid];
            }

            if (objVersion.compareTo(chromeVersion) < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return null;
    }

    public static String getURL() {
        JsonArray downloads = getJson().getAsJsonObject("downloads").getAsJsonArray("chromedriver");
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

    public static LooseVersion getInstalledChromeVersion() {
        LooseVersion version = null;
        String[] command;
        int index = -1;


        // TODO: This needs testing on windows & MacOS
        switch (determineOS()) {
            case OSType.WINDOWS:
                command = new String[] { "cmd", "/c", "reg query \"HKEY_CURRENT_USER\\Software\\Google\\Chrome\\BLBeacon\" /v version" };
                index = 1;
                break;
            case OSType.MACOS:
                command = new String[] { "/Application/Google Chrome.app/Contents/MacOS/Google Chrome", "--version" };
                index = 2;
                break;
            case OSType.LINUX:
                command = new String[] { "/opt/google/chrome/chrome", "--version" };
                index = 2;
                break;
            default:
                throw new RuntimeException("Couldn't determine OS");
        }

        String ln = null;

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(new File(System.getProperty("user.home")));
            Process proc = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Google Chrome") || line.contains("version")) {
                    ln = line;
                }
            }
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (ln == null) {
            throw new RuntimeException("Couldn't find version.");
        }

        String[] items = ln.split(" ");
        return new LooseVersion(items[index]);
    }

    public static String generateCDC() {
        StringBuilder cdc = new StringBuilder();

        for(int i = 0; i < 27; i++) {
            cdc.append(asciiLower[rand.nextInt(asciiLower.length)]);
        }

        return cdc.toString();
    }
}