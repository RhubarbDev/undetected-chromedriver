import Driver.Patcher;
import LooseVersion.LooseVersion;
import Utils.PatcherUtil;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        //System.out.println("Test.");
        //Patcher patcher = new Patcher();

        Path path = PatcherUtil.GeneratePath();

        PatcherUtil.DownloadChromeDriver(path);

    }
}
