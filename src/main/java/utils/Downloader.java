package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Downloader {
    private static final String BASE_URL = "https://googlechromelabs.github.io/chrome-for-testing/";
    public final File chromeFile;
    public final File driverFile;

    private static String fetchData(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000); // 5 seconds
        connection.setReadTimeout(5000);    // 5 seconds

        int status = connection.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + status + " from: " + urlString);
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
        } finally {
            connection.disconnect();
        }
        return content.toString();
    }

    private static File downloadUrl(String url, Path saveLocation) {
        try {
            // Ensure parent directory exists
            Files.createDirectories(saveLocation.getParent());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create directories for " + saveLocation, ex);
        }

        File file = saveLocation.toFile();

        // If file already exists, just reuse it
        if (file.exists()) return file;

        try {
            URL downloadUrl = new URL(url);
            FileUtils.copyURLToFile(downloadUrl, file);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to download from " + url + " to " + saveLocation, ex);
        }

        return file;
    }

    public Downloader() throws Exception {
        String version = fetchData(BASE_URL + "LATEST_RELEASE_STABLE").trim();
        String jsonData = fetchData(BASE_URL + version + ".json");

        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject().get("downloads").getAsJsonObject();

        JsonArray chrome = json.get("chrome").getAsJsonArray();
        JsonArray driver = json.get("chromedriver").getAsJsonArray();

        OS os = new OS();

        String path = null;
        switch(os.value) {
            case WINDOWS:
                path = "~/appdata/roaming/undetected_chromedriver";
                break;
            case MACOS:
                path = "~/Library/Application Support/undetected_chromedriver";
                break;
            default:
                path = "~/.local/share/undetected_chromedriver";
                break;
        }

        path = path.replaceFirst("~", System.getProperty("user.home"));

        Path cacheLocation = Paths.get(path);

        String chromeUrl = null;
        String driverUrl = null;

        for (JsonElement element : chrome) {
            JsonObject object = element.getAsJsonObject();

            String platform = object.get("platform").getAsString();

            if (platform.equalsIgnoreCase(os.osName)) {
                chromeUrl = object.get("url").getAsString();
                break;
            }
        }

        for (JsonElement element : driver) {
            JsonObject object = element.getAsJsonObject();

            String platform = object.get("platform").getAsString();

            if (platform.equalsIgnoreCase(os.osName)) {
                driverUrl = object.get("url").getAsString();
                break;
            }
        }

        if (chromeUrl == null || driverUrl == null) {
            throw new Exception("chrome url or driver url not found.");
        }

        this.chromeFile = downloadUrl(chromeUrl, cacheLocation.resolve("chrome_" + version + ".zip"));
        this.driverFile = downloadUrl(driverUrl, cacheLocation.resolve("driver_" + version + ".zip"));
    }
}
