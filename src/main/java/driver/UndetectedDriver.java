package driver;

import org.openqa.selenium.chrome.ChromeDriver;
import utils.Downloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static utils.Permissions.makeExecutable;

/**
 * The type Undetected driver.
 */
public class UndetectedDriver extends ChromeDriver {

    private UndetectedDriver(UndetectedOptions options) {
        super(options);
    }

    private static Path unzipFile(File zipFile) {
        Path parentDir = zipFile.toPath().getParent();
        if (parentDir == null) {
            parentDir = new File(".").toPath();
        }

        String fileName = zipFile.getName();
        String folderName = fileName.substring(0, fileName.lastIndexOf('.'));
        Path destDir = parentDir.resolve(folderName);

        try {
            Files.createDirectories(destDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create unzip destination: " + destDir, e);
        }

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String name = entry.getName();

                // Flatten: strip the first folder (chrome-linux64/, chromedriver-linux64/, etc.)
                int slash = name.indexOf('/');
                if (slash != -1) {
                    name = name.substring(slash + 1);
                }

                if (name.isEmpty()) {
                    zipIn.closeEntry();
                    continue;
                }

                Path filePath = destDir.resolve(name);

                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zipIn, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    // Ensure executables are runnable
                    String lower = name.toLowerCase();
                    if (lower.equals("chrome") || lower.equals("chromedriver") || lower.endsWith(".exe")) {
                        filePath.toFile().setExecutable(true, false);
                    }
                }
                zipIn.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to unzip file: " + zipFile, e);
        }

        return destDir;
    }

    public static UndetectedDriver createDriver(UndetectedOptions options) {
        try {
            Downloader downloader = new Downloader();

            // unzip chrome and update options (options.setBinary(file))
            Path chromeFolder = unzipFile(downloader.chromeFile);
            File chromeBin = chromeFolder.resolve("chrome").toFile();

            if (!chromeBin.exists() || !chromeBin.canExecute()) {
                throw new RuntimeException("Chrome binary not found: " + chromeBin);
            }
            options.setBinary(chromeBin);

            // unzip driver and pass location to patcher
            Path driverFolder = unzipFile(downloader.driverFile);
            Patcher patcher = new Patcher(driverFolder);
            File driverBin = patcher.getDriver();

            if (driverBin == null || !driverBin.exists() || !driverBin.canExecute()) {
                throw new RuntimeException("Chromedriver binary not found or not executable: " + driverBin);
            }

            System.setProperty("webdriver.chrome.driver", driverBin.toString());

            makeExecutable(downloader.chromeFile.getParentFile());

            return new UndetectedDriver(options);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("failed to create driver: " + ex.getMessage(), ex);
        }
    }
}
