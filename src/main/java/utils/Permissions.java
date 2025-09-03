package utils;

import java.io.File;
import java.util.Objects;

public class Permissions {
    private static final int MAX_DEPTH = 15;

    public static void makeExecutable(File dir) {
        makeExecutable(dir, 0);
    }

    public static void makeExecutable(File dir, int depth)
    {
        if (depth == MAX_DEPTH) return;

        if (dir.isDirectory()) {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                makeExecutable(file);
            }
        }

        if (!dir.setExecutable(true, true)) {
            System.err.println("failed to set +x on: " + dir.getAbsolutePath());
        }
    }

}