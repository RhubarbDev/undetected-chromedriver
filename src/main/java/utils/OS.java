package utils;

public class OS {

    public enum OSValue {
        WINDOWS,
        MACOS,
        LINUX,
        UNKNOWN
    }

    private static OS os = null;
    public final OSValue value;
    public final String osName;

    private static OSValue getOSValue() {
        OSValue value = OSValue.UNKNOWN;

        String name = System.getProperty("os.name");

        if (name.contains("win")) {
            value = OSValue.WINDOWS;
        } else if (name.contains("nix") || name.contains("nux") || name.contains("aix")) {
            value = OSValue.LINUX;
        } else if (name.contains("mac")) {
            value = OSValue.MACOS;
        }

        return value;
    }

    public OS() {
        this(getOSValue());
    }

    // don't use this directly unless
    public OS(OSValue value) {
        this.value = value;

        String arch = System.getProperty("os.arch");

        switch (this.value) {
            case WINDOWS:
                this.osName = arch.contains("64") ? "win64" : "win32";
                break;
            case LINUX:
                this.osName = "linux64";
                break;
            case MACOS:
                this.osName = arch.contains("aarch64") ? "mac-arm64" : "mac-x64";
                break;
            default:
                this.osName = "unknown";
        }
    }
}