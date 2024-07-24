import Driver.Patcher;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        Patcher patcher = new Patcher();

        Path downloadedFile = patcher.downloadChromedriver();
        System.out.println(downloadedFile);

    }
}
