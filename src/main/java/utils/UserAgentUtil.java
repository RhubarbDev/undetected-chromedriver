package utils;

import driver.LooseVersion;

/**
 * The type User agent util.
 */
public final class UserAgentUtil {
    private static final String[] versions = {
            "Windows NT 10.0; Win64; x64",
            "Macintosh; Intel Mac OS X 10_15_7",
            "X11; Linux x86_64"
    };

    private static final String USER_AGENT = "Mozilla/5.0 (%) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/% Mobile Safari/537.36";

    /**
     * Gen user agent string.
     *
     * @return the generated user agent.
     */
    public static String genUserAgent() {
        OSUtils.OSInfo osInfo = OSUtils.getOS();

        String versionName = versions[osInfo.os().ordinal() % versions.length];
        LooseVersion versionNumber = OSUtils.getInstalledChromeVersion(osInfo.command());

        try {
            int majorVersion = (int)versionNumber.getPart(0);
            if (majorVersion >= 101) {
                versionNumber = new LooseVersion(majorVersion + ".0".repeat(versionNumber.getParts() - 1));
            }
        } catch (Exception ignore) { }

        return USER_AGENT.replaceFirst("%", versionName).replaceFirst("%", versionNumber.toString());
    }
}