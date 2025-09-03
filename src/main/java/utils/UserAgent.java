package utils;

/**
 * The type User agent util.
 */
public final class UserAgent {
    private static final String[] versions = {
            "Windows NT 10.0; Win64; x64",
            "Macintosh; Intel Mac OS X 10_15_7",
            "X11; Linux x86_64"
    };

    private static final String USER_AGENT = "Mozilla/5.0 (%) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/% Mobile Safari/537.36";

    // temp
    public static String genUserAgent() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36";
    }
}