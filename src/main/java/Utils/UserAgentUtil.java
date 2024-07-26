package Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import LooseVersion.LooseVersion;

public final class UserAgentUtil {
    private static final String[] versions = {
            "Windows NT 10.0; Win64; x64",
            "Macintosh; Intel Mac OS X 10_15_7",
            "X11; Linux x86_64"
    };

    private static final String userAgent = "Mozilla/5.0 (%) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/% Mobile Safari/537.36";

    public static String genUserAgent() {
        String versionName = versions[PatcherUtil.determineOS().ordinal() % versions.length];
        LooseVersion versionNumber = PatcherUtil.getInstalledChromeVersion();
        String[] parts = versionNumber.toString().split("\\.");

        if (parts.length > 0 && Integer.parseInt(parts[0]) >= 101) {
            versionNumber = new LooseVersion(parts[0] + ".0".repeat(parts.length - 1));
        }

        return userAgent.replaceFirst("%", versionName).replaceFirst("%", versionNumber.toString());
    }

    public static String fixUserAgent(String userAgent) {
        try {
            userAgent = userAgent.replaceFirst("Headless", "");
            final Pattern pattern = Pattern.compile("Chrome/((?:\\d+\\.?)+)");
            final Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                StringBuilder builder = new StringBuilder();
                String[] parts = matcher.group(1).split("\\.");

                if (parts.length > 0) {
                    builder.append(parts[0]);
                    builder.append(".0".repeat(parts.length - 1));
                    userAgent = userAgent.replace(matcher.group(1), builder.toString());
                }
            }
        } catch (Exception ex) {
            return null;
        }
        return userAgent;
    }
}