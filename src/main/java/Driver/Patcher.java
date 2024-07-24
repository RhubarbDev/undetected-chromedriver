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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Patcher {

    private static final String zipName = "undetected_chromedriver.zip";
    private static final String exeName = "undetected_chromedriver";

    private final PatcherUtil.OSType os;
    private final LooseVersion driverVersion;
    private final Path saveLocation;

    public void unzipChromedriver(Path zipFile) {
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
    }


    public Path extractChromedriver(Path dir) {
        File destination = dir.toFile();
        File zipFile = new File(destination, (driverVersion + "_" + zipName));
        File exeFile = new File(destination, (driverVersion + "_" + exeName));

        // make this more descriptive at some point
        if (!destination.exists() ) {
            throw new RuntimeException("Couldn't find files.");
        }

        try {
            ZipInputStream input = new ZipInputStream(new FileInputStream(zipFile));

            ZipEntry entry = input.getNextEntry();
            while (entry != null) {
                if (entry.getName().equalsIgnoreCase("chromedriver")) {
                    BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(exeFile));
                    byte[] bytesIn = new byte[4096];

                    int read = 0;
                    while ((read = input.read(bytesIn)) != -1) {
                        output.write(bytesIn, 0, read);
                    }

                    output.close();
                    break;
                }
            }

            input.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return exeFile.toPath();
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
        unzipChromedriver(zipPath);
        System.out.println("File Unzipped.");
    }
}
