package Driver;

import Utils.PatcherUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Patcher {

    private static final String exeName = "undetected_%_chromedriver";

    private Path inputPath;
    private final Path outputPath;

    private final PatcherUtil.OSType os;
    private final LooseVersion version;

    public Path getDriver() {
        return outputPath;
    }

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
    public Path downloadChromedriver(String downloadUrl) {

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

        File file;
        String name = version + ".zip";

        if (downloadUrl == null) {
            downloadUrl = PatcherUtil.getURL();
        }

        try {
            URL url = new URI(downloadUrl).toURL();
            file = new File(saveLocation.toString(), name);
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

        String patchedName = exeName.replace("%", this.version.toString());

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

    public boolean attemptPatch() {
        assert !outputPath.toFile().exists();

        try (RandomAccessFile inputFile = new RandomAccessFile(this.inputPath.toFile(), "r");
             RandomAccessFile outputFile = new RandomAccessFile(this.outputPath.toFile(), "rw")) {

            byte[] buffer = new byte[1024];
            StringBuilder builder = new StringBuilder();
            int bytesRead;
            while ((bytesRead = inputFile.read(buffer)) != -1) {
                builder.append(new String(buffer, 0, bytesRead, StandardCharsets.ISO_8859_1));
            }

            String content = builder.toString();
            Pattern pattern = Pattern.compile("\\{window\\.cdc.*?;}");
            Matcher matcher = pattern.matcher(content);

            if (matcher.find()) {
                String foundString = matcher.group();
                String replacementString = "{console.log(\"undetected chromedriver 1337!\")}";
                StringBuilder target = new StringBuilder(replacementString);
                int paddingLength = foundString.length() - replacementString.length();
                target.append(" ".repeat(Math.max(0, paddingLength)));
                String newContent = content.replace(foundString, target.toString());
                outputFile.setLength(0);
                outputFile.write(newContent.getBytes(StandardCharsets.ISO_8859_1));
                return true;
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to patch: " + ex.getMessage());
        }
        return false;
    }

    public Patcher() {
        this(null);
    }

    private void checkExecutable(File file) {
        if (file.setExecutable(true)) {
            System.out.println("Made {" + file + "} executable.");
        }
    }

    public Patcher(String downloadUrl) {
        this.os = PatcherUtil.determineOS();
        this.version = PatcherUtil.getInstalledChromeVersion();
        Path saveLocation = PatcherUtil.generatePath();
        this.outputPath = Paths.get(saveLocation +
                FileSystems.getDefault().getSeparator() +
                exeName.replaceFirst("%", version.toString()) +
                (os == PatcherUtil.OSType.WINDOWS ? ".exe" : "")
        );

        System.out.println("Checking if Executable is patched...");

        if (outputPath.toFile().exists()) {
            System.out.println("Patched File exists..");
            checkExecutable(outputPath.toFile());
            return;
        }

        System.out.println("Downloading Chromedriver...");
        Path zipPath = downloadChromedriver(downloadUrl);
        System.out.println("Archive downloaded.\nUnzipping Archive.");

        this.inputPath = unzipChromedriver(zipPath);

        if (this.inputPath == null) {
            throw new RuntimeException("Couldn't find Chromedriver.");
        }

        System.out.println("File unzipped.\nAttempting to patch executable...");

        if (attemptPatch()) {
            System.out.println("Driver patched.");
        }

        checkExecutable(outputPath.toFile());

        System.out.println("Cleaning up.");
        cleanupFolder();
    }
}