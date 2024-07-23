import Driver.Patcher;
import LooseVersion.LooseVersion;
import Utils.PatcherUtil;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        Path downloadPath = PatcherUtil.DownloadChromeDriver();
        System.out.println(downloadPath);

        PatcherUtil.CleanupFolder();

    }
}
