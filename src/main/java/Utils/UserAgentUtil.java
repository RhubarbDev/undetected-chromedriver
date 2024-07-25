package Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UserAgentUtil {
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