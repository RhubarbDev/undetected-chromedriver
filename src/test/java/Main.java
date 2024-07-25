import Driver.Patcher;
import LooseVersion.LooseVersion;
import Utils.PatcherUtil;
import com.google.gson.JsonObject;

public class Main {

    public static void main(String[] args) {
        //Patcher patcher = new Patcher();

        LooseVersion version = new LooseVersion("114.0.5711.3");
        String url = PatcherUtil.legacyDownloadUrl(version);

        System.out.println("URL: " + url);

    }
}
