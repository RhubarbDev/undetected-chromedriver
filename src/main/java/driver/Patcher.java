package driver;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import utils.DownloadUtils;
import utils.OSUtils;

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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Patcher {
    private static final String EXECUTABLE = "undetected_%_chromedriver";

    private static final OSUtils.OSInfo osInfo = OSUtils.getOS();
    private LooseVersion version;
    private Path inputPath;
    private final Path outputPath;

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

        String name = "chromedriver" + (osInfo.os() == OSUtils.OS.WINDOWS ? ".exe" : "");

        Iterator<File> files = FileUtils.iterateFiles(destination.toFile(), null, true);

        while (files.hasNext()) {
            File file = files.next();
            if (file.getName().equalsIgnoreCase(name)) {
                return file.toPath();
            }
        }

        return null;
    }

    private Path downloadChromeDriver(String downloadUrl) {

        Path saveLocation = Paths.get(osInfo.path());

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
        String name = this.version + ".zip";

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
    
    public Patcher() {
        this(DownloadUtils.getURL(osInfo, OSUtils.getInstalledChromeVersion(osInfo.command())));
    }

    public Patcher(String downloadUrl) {
        String line = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(osInfo.command());
            builder.directory(new File(System.getProperty("user.home")));
            Process proc = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String read;
            while ((read = reader.readLine()) != null) {
                if (read.contains("Google Chrome") || Objects.requireNonNull(line).contains("version")) {
                    line = read;
                }
            }
            reader.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        if (line == null) {
            throw new RuntimeException("Couldn't find version.");
        }

        this.version = OSUtils.getInstalledChromeVersion(osInfo.command());

        this.outputPath = Paths.get(
                osInfo.path(),
                FileSystems.getDefault().getSeparator(),
                EXECUTABLE.replaceFirst("%", this.version.toString()),
                (osInfo.os() == OSUtils.OS.WINDOWS ? ".exe" : "")
        );

        if (outputPath.toFile().exists()) {
            outputPath.toFile().setExecutable(true);
            return;
        }

        this.inputPath = unzipChromedriver(downloadChromeDriver(downloadUrl));

        if (this.inputPath == null) {
            throw new RuntimeException("Couldn't find Chromedriver.");
        }

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
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to patch: " + ex.getMessage());
        }

        outputPath.toFile().setExecutable(true);

        File[] files = new File(osInfo.path()).listFiles();

        if (files == null) {
            return;
        }

        String patchedName = EXECUTABLE.replace("%", this.version.toString());

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
            } catch (IOException ex) {}
        }
    }
}
