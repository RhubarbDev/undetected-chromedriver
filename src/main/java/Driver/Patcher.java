package Driver;

import LooseVersion.LooseVersion;
import Utils.PatcherUtil;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Patcher {

    private static final String exeName = "undetected_chromedriver";

    private final PatcherUtil.OSType os;
    private final LooseVersion driverVersion;
    private final Path saveLocation;

    public Path unzipChromedriver(Path zipFile) {
        String fileBaseName = FilenameUtils.getBaseName(zipFile.getFileName().toString());
        Path destination = Paths.get(zipFile.getParent().toString(), fileBaseName);

        try (ZipFile file = new ZipFile(zipFile.toFile(), ZipFile.OPEN_READ, Charset.defaultCharset())) {
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path entryPath = destination.resolve(entry.getName());
                if (entryPath.normalize().startsWith(destination.normalize())) {
                    if (entry.isDirectory()) {
                        Files.createDirectories(entryPath);
                    } else {
                        Files.createDirectories(entryPath.getParent());
                        try (InputStream inputStream = file.getInputStream(entry)) {
                            try (OutputStream outputStream = new FileOutputStream(entryPath.toFile())) {
                                IOUtils.copy(inputStream, outputStream);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }

        String name = "chromedriver" + (os == PatcherUtil.OSType.WINDOWS ? ".exe" : "");

        Iterator<File> files = FileUtils.iterateFiles(destination.toFile(), null, true);

        while (files.hasNext()) {
            File file = files.next();
            if (file.getName().equalsIgnoreCase(name)) {
                return file.toPath();
            }
        }

        return null;
    }



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
        String name = driverVersion + ".zip";

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

    private boolean isPatched() {
        File patchedExe = new File(saveLocation.toFile(), (driverVersion + "_" + exeName + (this.os == PatcherUtil.OSType.WINDOWS ? ".exe" : "")));

        if (patchedExe.exists()) {
            return true;
        }

        return false;
    }

    public Patcher() {
        this.os = PatcherUtil.determineOS();
        this.driverVersion = PatcherUtil.fetchReleaseNumber();
        this.saveLocation = PatcherUtil.generatePath();
        System.out.println("Checking if Executable is patched...");
        File patchedExe = new File(saveLocation.toFile(), (driverVersion + "_" + exeName));

        // this doesn't work.
        if (patchedExe.exists()) {
            System.out.println("Executable patched: " + patchedExe.getPath());
            return;
        }

        System.out.println("Downloading Chromedriver...");
        Path zipPath = downloadChromedriver();
        System.out.println("Archive downloaded.");

        System.out.println("Unzipping Archive.");
        Path driverPath = unzipChromedriver(zipPath);
        System.out.println("File Unzipped, driver location: " + driverPath);
    }
}
