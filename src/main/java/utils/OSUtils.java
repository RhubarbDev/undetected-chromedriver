package utils;

import com.google.gson.Gson;
import driver.LooseVersion;
import org.openqa.selenium.json.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public final class OSUtils {

    public enum OS {
        WINDOWS,
        MACOS,
        LINUX
    }

    public record OSInfo(OS os, String path, String legacyFile, String[] command) {}
    private static LooseVersion version = null;
    private static OSInfo info = null;

    public static OSInfo getOS() {
        return getOS(System.getProperty("os.name"));
    }

    public static OSInfo getOS(String name) {
        if (info != null) {
            return info;
        }

        OS os;
        if (name.contains("win")) {
            os = OS.WINDOWS;
        } else if (name.contains("nix") || name.contains("nux") || name.contains("aix")) {
            os = OS.LINUX;
        } else if (name.contains("mac")) {
            os = OS.MACOS;
        } else {
            throw new RuntimeException("Couldn't determine Operating System.");
        }

        Gson gson = new Gson();
        List<OSInfo> osInfoList = null;

        try (InputStream input = OSUtils.class.getResourceAsStream("/os.json")) {
            if (input != null) {
                Type osInfoListType = new TypeToken<List<OSInfo>>(){}.getType();
                osInfoList = gson.fromJson(new InputStreamReader(input), osInfoListType);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        if (osInfoList == null || osInfoList.size() < OS.values().length) {
            throw new RuntimeException("Failed to parse JSON");
        }

        for(OSInfo osInfo : osInfoList) {
            if (Objects.equals(osInfo.os, os)) {
                info = osInfo;
                return info;
            }
        }

        throw new RuntimeException("Failed to find osInfo");
    }

    public static LooseVersion getInstalledChromeVersion(String[] command) {
        if (version != null) {
            return version;
        }

        Process proc;
        try {
            proc = new ProcessBuilder(command).start();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        String line = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String read;
            while ((read = reader.readLine()) != null) {
                if (read.contains("Google Chrome") || read.contains("version")) {
                    line = read;
                }
            }
            if (line == null) {
                throw new RuntimeException("Couldn't find version.");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        String[] items = line.split(" ");
        for (String item : items) {
            try {
                version = new LooseVersion(item);
            } catch (Exception ignored) {
                // do nothing.
            }
        }

        if (version == null) {
            throw new RuntimeException("Couldn't determine Chrome version.");
        }

        return version;
    }
}
