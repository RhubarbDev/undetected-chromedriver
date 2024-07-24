package Driver;

import LooseVersion.LooseVersion;
import Utils.PatcherUtil;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class Patcher {

    private static final String zipName = "undetected_chromedriver.zip";
    private static final String exeName = "undetected_chromedriver";

    private final PatcherUtil.OSType os;
    private final LooseVersion driverVersion;

    // returns path of downloaded file.
    public Path downloadChromedriver() {

        JsonObject object = PatcherUtil.getJson();
        Path saveLocation = PatcherUtil.generatePath();

        // Check saveLocation exists, if not create it.
        try {
            Files.createDirectories(saveLocation);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        if (!saveLocation.toFile().exists() || !saveLocation.toFile().isDirectory()) {
            throw new RuntimeException(saveLocation + " is not a directory.");
        }


        File file = null;
        String name = driverVersion + "_" + zipName;

        try {
            URL url = new URL(PatcherUtil.getURL());
            file = new File(saveLocation.toString(), name);

            /*
             * If a file of the same version already exists, don't download it again.
             * when the unzip function has been written, make it check for the executable, as the zip file will be deleted
             */
            if (!file.exists()) {
                FileUtils.copyURLToFile(url, file);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        return file.toPath();
    }

    public void cleanupFolder() {
        File[] files = PatcherUtil.generatePath().toFile().listFiles();

        if (files == null) {
            System.out.println("Nothing to cleanup (probably).");
            return;
        }

        String patchedName = driverVersion + "_" + exeName;

        for (File file : files) {
            try {
                if (file.isDirectory()) {
                    FileUtils.cleanDirectory(file);
                    FileUtils.deleteDirectory(file);
                } else {
                    // ignore the most recent patched executable
                    if (file.getName().equalsIgnoreCase(patchedName)) {
                        continue;
                    }
                    FileUtils.delete(file);
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public Patcher() {
        os = PatcherUtil.determineOS();
        driverVersion = PatcherUtil.fetchReleaseNumber();
    }
}
