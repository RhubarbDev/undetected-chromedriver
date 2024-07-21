package Utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PatcherUtil {

    public enum OSType {
        WINDOWS,
        MACOS,
        LINUX,
        OTHER
    }

    private static OSType os = DetermineOS();

    // I'm pretty sure I'll need to change these
    private static final String urlRepo = "https://chromedriver.storage.googleapis.com";
    private static final String zipName = "chromedriver_%";
    private static final String exeName = "chromedriver%";

    public static OSType DetermineOS() {
        if (os == null) {
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("win")) {
                os = OSType.WINDOWS;
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                os = OSType.LINUX;
            } else if (osName.contains("mac")) {
                os = OSType.MACOS;
            } else {
                os = OSType.OTHER;
            }
        }

        return os;
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
}