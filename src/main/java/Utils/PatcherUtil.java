package Utils;

import LooseVersion.LooseVersion;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.management.RuntimeErrorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.Buffer;
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

    private static final OSType os = DetermineOS();
    private static JsonObject jsonObject = null;

    // I might be able to combine these, need to check
    private static final String urlRepo = "https://googlechromelabs.github.io/chrome-for-testing";
    private static final String verPath = "/last-known-good-versions-with-downloads.json";

    private static final String zipName = "chromedriver_%";
    private static final String exeName = "chromedriver%";

    private static OSType DetermineOS(String name) {
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

    public static OSType DetermineOS() {
        return os == null ? DetermineOS(System.getProperty("os.name")) : os;
    }

    public static boolean IsPosix() {
        assert os != null;
        return (os == OSType.LINUX || os == OSType.MACOS);
    }

    public static String[] GeneratePatchData() {
        assert os != null;

        /*
         * 0 = zip
         * 1 = exe
         * 2 = dat
         */

        String[] info = new String[3];

        switch(os) {
            case OSType.WINDOWS:
                info[0] = zipName.replace("%", "win32");
                info[1] = exeName.replace("%", ".exe");
                info[2] = "~/appdata/roaming/undetected_chromedriver";
                break;
            case OSType.LINUX:
                info[0] = zipName.replace("%", "linux64");
                info[1] = exeName.replace("%", "");
                info[2] = "~/.local/share/undetected_chromedriver";
                break;
            case OSType.MACOS:
                info[0] = zipName.replace("%", "mac64");
                info[1] = exeName.replace("%", "");
                info[2] = "~/Library/Application Support/undetected_chromedriver";
                break;
            default:
                info[2] = "~/.undetected_chromedriver";
                break;
        }

        if (System.getenv().containsKey("LAMBDA_TASK_ROOT")) {
            info[2] = "/tmp/undetected_chromedriver";
        }

        return info;
    }

    private static JsonObject FetchDriverData() {
        StringBuilder builder = new StringBuilder();

        try {
            URL url = new URL(urlRepo + verPath);
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

    // returns true if download succeeded
    public static boolean DownloadChromeDriver() {
        if (jsonObject == null) {
            jsonObject = FetchDriverData();
        }

        JsonArray downloads = jsonObject.getAsJsonObject("channels").getAsJsonObject("Stable").getAsJsonObject("downloads").getAsJsonArray("chrome");
        String url = null;

        for (JsonElement download : downloads) {
            JsonObject obj = download.getAsJsonObject();
            if (DetermineOS(obj.get("platform").getAsString()) == DetermineOS()) {
                url = obj.get("url").getAsString();
                break;
            }
        }

        if (url == null) {
            throw new RuntimeException("Couldn't match OS to download URL.");
        }

        System.out.println("URL: " + url);







        // it would make sense to unzip chromedriver here as well as download, as I can get the zip name easily using regex

        return false;
    }


    // pretty sure this isn't needed anymore, see PatcherUtil.DownloadChromeDriver
    public static LooseVersion FetchReleaseNumber() {
        if (jsonObject == null) {
            jsonObject = FetchDriverData();
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

    public static String GenerateCDC() {
        StringBuilder cdc = new StringBuilder();

        for(int i = 0; i < 27; i++) {
            cdc.append(asciiLower[rand.nextInt(asciiLower.length)]);
        }

        return cdc.toString();
    }

    public static boolean ExecutablePatched(Path executable) {
        File file = new File(String.valueOf(executable)); // why can't I use path :(
        return file.exists() && file.isFile();
    }




}