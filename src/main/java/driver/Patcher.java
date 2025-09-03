package driver;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import utils.*;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Patcher {
    private File chromedriver = null;

    public File getDriver() {
        return this.chromedriver;
    }

    private void patch() {
        if (this.chromedriver == null) return;

        Path original = this.chromedriver.toPath();
        Path backup = original.resolveSibling(original.getFileName() + ".bak");

        try {
            // Backup original
            Files.copy(original, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            byte[] bytes = Files.readAllBytes(original);
            String content = new String(bytes, StandardCharsets.ISO_8859_1);

            // More robust regex for cdc variable
            Pattern pattern = Pattern.compile("\\{window\\.cdc_[^=]*?=.+?\\}");
            Matcher matcher = pattern.matcher(content);

            String newContent;
            if (matcher.find()) {
                String foundString = matcher.group();
                String replacementString = "{console.log(\"undetected chromedriver 1337!\")}";
                StringBuilder target = new StringBuilder(replacementString);

                int paddingLength = foundString.length() - replacementString.length();
                if (paddingLength > 0) {
                    target.append(" ".repeat(paddingLength));
                }

                newContent = content.replace(foundString, target.toString());
            } else {
                newContent = content;
            }

            Files.write(original, newContent.getBytes(StandardCharsets.ISO_8859_1));

        } catch (IOException ex) {
            throw new RuntimeException("Failed to patch chromedriver: " + ex.getMessage(), ex);
        }
    }

    public Patcher(Path driverFolder) {
        this.chromedriver = driverFolder.resolve("chromedriver").toFile();
        patch();
    }
}