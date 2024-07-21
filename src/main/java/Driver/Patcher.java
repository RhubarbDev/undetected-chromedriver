package Driver;

import Utils.PatcherUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Patcher {

    private static final String prefix = "undetected";
    private final boolean isPosix;

    private Path dataPath;
    private Path executablePath;
    private Path zipPath;

    /**
     * I've not included executable_path, force and version_main. I'll do it at some point.
     */
    public Patcher() {
        this.isPosix = PatcherUtil.IsPosix();
        String[] data = PatcherUtil.GeneratePatchData();

        this.dataPath = Paths.get(data[2].replaceFirst("^~", System.getProperty("user.home"))).toAbsolutePath();

        try {
            Files.createDirectories(this.dataPath);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create directory: " + ex.getMessage());
        }

        this.executablePath = this.dataPath.resolve(prefix + "_" + data[1]).toAbsolutePath();
        this.zipPath = this.dataPath.resolve(prefix);

        if (!this.isPosix && !this.executablePath.toString().endsWith(".exe")) {
            this.executablePath = Paths.get(this.executablePath.toString() + ".exe");
        }
    }
}
