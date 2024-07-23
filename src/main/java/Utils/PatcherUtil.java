package Utils;

import LooseVersion.LooseVersion;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // if something stops working, this might have changed.
    private static final String urlRepo = "https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions-with-downloads.json";
    private static final String zipName = "undetected_chromedriver.zip";
    private static final String exeName = "undetected_chromedriver";

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

    public static Path GeneratePath() {
        assert os != null;

        String path = null;

        switch (os) {
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

    private static JsonObject FetchDriverData() {
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

    private static String GetURL() {
        JsonArray downloads = jsonObject.getAsJsonObject("channels").getAsJsonObject("Stable").getAsJsonObject("downloads").getAsJsonArray("chromedriver");
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

        return url;
    }


    // returns path of downloaded file.
    public static Path DownloadChromeDriver() {
        if (jsonObject == null) {
            jsonObject = FetchDriverData();
        }

        Path saveLocation = GeneratePath();

        // Check saveLocation exists, if not create it.
        try {
            Files.createDirectories(saveLocation);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        if (!saveLocation.toFile().exists() || !saveLocation.toFile().isDirectory()) {
            throw new RuntimeException(saveLocation.toString() + " is not a directory.");
        }


        File file = null;
        String name = FetchReleaseNumber() + "_" + zipName;

        try {
            URL url = new URL(GetURL());
            file = new File(saveLocation.toString(), name);

            /*
             * If a file of the same version already exists, don't download it again.
             * when the unzip function has been written, make it check for the executable, as the zip file will be deleted
             */
            if (!file.exists()) {
                FileUtils.copyURLToFile(url, file);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        return file.toPath();
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

    // this is a bit of a naive approach, it doesn't know if its patched, only if it exists.
    public static boolean ExecutablePatched(Path executable) {
        File file = executable.toFile();
        return file.exists() && file.isFile();
    }

    public static void CleanupFolder() {
        File[] files = GeneratePath().toFile().listFiles();

        if (files == null) {
            System.out.println("Nothing to cleanup (probably).");
            return;
        }

        for (File file : files) {
            System.out.println(file.getName());

            if (file.getName().equalsIgnoreCase(exeName)) continue;

            try {
                if (file.isDirectory()) {
                    FileUtils.cleanDirectory(file);
                    FileUtils.deleteDirectory(file);
                } else {
                    FileUtils.delete(file);
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}