package LooseVersion;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LooseVersion implements Comparable<LooseVersion> {
    private static final Pattern COMPONENT_RE = Pattern.compile("(\\d+|[a-z]+|\\.)");
    private final String versionString;
    private final List<Object> version;

    public LooseVersion(String vString) {
        assert vString != null;
        this.versionString = vString;

        Matcher matcher = COMPONENT_RE.matcher(versionString);
        version = new ArrayList<>();

        while (matcher.find()) {
            String part = matcher.group().trim();
            if (!part.equals(".")) {
                try {
                    version.add(Integer.parseInt(part));
                } catch (NumberFormatException ex) {
                    version.add(part);
                }
            }
        }
    }

    @Override
    public String toString() {
        return this.versionString;
    }

    @Override
    public int compareTo(LooseVersion other) {
        for (int i = 0; i < Math.max(this.version.size(), other.version.size()); i++) {
            Object thisPart = i < this.version.size() ? this.version.get(i) : null;
            Object otherPart = i < other.version.size() ? other.version.get(i) : null;

            int result = CompareParts(thisPart, otherPart);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private int CompareParts(Object thisPart, Object otherPart) {
        if (thisPart == null) {
            return otherPart == null ? 0 : -1;
        }

        if (otherPart == null) {
            return 1;
        }

        if (thisPart instanceof Integer && otherPart instanceof  Integer) {
            return ((Integer) thisPart).compareTo((Integer) otherPart);
        } else if (thisPart instanceof String && otherPart instanceof String) {
            return ((String) thisPart).compareTo((String) otherPart);
        } else if (thisPart instanceof Integer) {
            return -1; // Integers are less than strings
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        LooseVersion other = (LooseVersion) obj;
        return this.compareTo(other) == 0;
    }

    @Override
    public int hashCode() {
        return versionString != null ? versionString.hashCode() : 0;
    }
}