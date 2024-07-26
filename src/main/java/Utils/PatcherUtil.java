package Utils;

import Driver.LooseVersion;
import com.google.gson.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class PatcherUtil {

    public enum OSType {
        WINDOWS,
        MACOS,
        LINUX,
        OTHER
    }

    private static JsonObject jsonObject = null;

    /* jsonEndpoint - json endpoint contains download links for versions of chromedriver > 115
     * legacyDriverVersions - url used to find downloads for versions of chromedriver < 115
     */
    private static final String jsonEndpoint = "https://googlechromelabs.github.io/chrome-for-testing/known-good-versions-with-downloads.json";
    private static final String legacyDriverVersions = "https://chromedriver.storage.googleapis.com/";

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
        String path = switch (determineOS()) {
            case OSType.WINDOWS -> "~/appdata/roaming/undetected_chromedriver";
            case OSType.LINUX -> "~/.local/share/undetected_chromedriver";
            case OSType.MACOS -> "~/Library/Application Support/undetected_chromedriver";
            default -> "~/.undetected_chromedriver";
        };

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

        if(obj.length < 1) {
            throw new RuntimeException("Array Empty.");
        }

        LooseVersion chromeVersion = getInstalledChromeVersion();

        // Check the latest version, before searching the entire array.
        LooseVersion latest = new LooseVersion(obj[0].get("version").getAsString());

        if (latest.equals(chromeVersion)) {
            return obj[0];
        }

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

    // this might not work perfectly.
    public static String legacyDownloadUrl(LooseVersion version) {
        LooseVersion validVersion;

        try {
            String majorVersion = version.toString().split("\\.")[0];
            URL url = new URI(legacyDriverVersions + "LATEST_RELEASE_" + majorVersion).toURL();
            validVersion = new LooseVersion(IOUtils.toString(url, StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        String fileName = switch (PatcherUtil.determineOS()) {
            case OSType.WINDOWS -> "chromedriver_win32.zip";
            case OSType.MACOS -> "chromedriver_mac64.zip";
            case OSType.LINUX -> "chromedriver_linux64.zip";
            case OSType.OTHER -> throw new RuntimeException("Couldn't determine OS");
        };

        return legacyDriverVersions + validVersion + "/" + fileName;
    }

    // This function could easily break, me thinks.
    public static String getURL() {
        JsonObject downloads = getJson().getAsJsonObject("downloads");

        String url = null;

        // versions lower than 115 don't have chromedriver
        if (!downloads.has("chromedriver")) {
            return legacyDownloadUrl(getInstalledChromeVersion());
        }

        JsonArray versions = downloads.getAsJsonArray("chromedriver");

        for (JsonElement version : versions) {
            JsonObject obj = version.getAsJsonObject();
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
        String[] command;
        int index = switch (determineOS()) {
            case OSType.WINDOWS -> {
                command = new String[]{"cmd", "/c", "reg query \"HKEY_CURRENT_USER\\Software\\Google\\Chrome\\BLBeacon\" /v version"};
                yield 1;
            }
            case OSType.MACOS -> {
                command = new String[]{"/Application/Google Chrome.app/Contents/MacOS/Google Chrome", "--version"};
                yield 2;
            }
            case OSType.LINUX -> {
                command = new String[]{"/opt/google/chrome/chrome", "--version"};
                yield 2;
            }
            default -> throw new RuntimeException("Couldn't determine OS");
        };

        String line = null;

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(new File(System.getProperty("user.home")));
            Process proc = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String read;
            while ((read = reader.readLine()) != null) {
                if (read.contains("Google Chrome") || Objects.requireNonNull(line).contains("version")) {
                    line = read;
                }
            }
            reader.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        if (line == null) {
            throw new RuntimeException("Couldn't find version.");
        }

        String[] items = line.split(" ");
        return new LooseVersion(items[index]);
    }
}