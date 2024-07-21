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
    private static final String prefix = "undetected";

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

    private static boolean IsPosix() {
        assert os != null;
        return (os == OSType.LINUX || os == OSType.MACOS);
    }

    public String[] GeneratePatchData() {
        assert os != null;

        String url = null;
        String zip = null;
        String exe = null;
        String dat = null;

        switch(os) {
            case OSType.WINDOWS:
                zip = zipName.replace("%", "win32");
                exe = exeName.replace("%", ".exe");
                dat = "~/appdata/roaming/undetected_chromedriver";
                break;
            case OSType.LINUX:
                zip = zipName.replace("%", "linux64");
                exe = exeName.replace("%", "");
                dat = "~/.local/share/undetected_chromedriver";
                break;
            case OSType.MACOS:
                zip = zipName.replace("%", "mac64");
                exe = exeName.replace("%", "");
                dat = "~/Library/Application Support/undetected_chromedriver";
                break;
            default:
                dat = "~/.undetected_chromedriver";
                break;
        }

        if (System.getenv().containsKey("LAMBDA_TASK_ROOT")) {
            dat = "/tmp/undetected_chromedriver";
        }

        Path dataPath = Paths.get(dat.replaceFirst("^~", System.getProperty("user.home"))).toAbsolutePath();

        try {
            Files.createDirectories(dataPath);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        Path executablePath = dataPath.resolve(prefix + "_" + exe);

        throw new UnsupportedOperationException("not implemented");
    }
}