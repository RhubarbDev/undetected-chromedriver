import Driver.Patcher;
import LooseVersion.LooseVersion;
import Utils.PatcherUtil;
import com.google.gson.JsonObject;
import org.checkerframework.checker.units.qual.C;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.JavascriptExecutor;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Patcher patcher = new Patcher();
        Path path = patcher.getDriver();

        if (!path.toFile().setExecutable(true)) {
            throw new RuntimeException("Couldn't set executable.");
        }

        System.setProperty("webdriver.chrome.driver", path.toString());

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        ChromeDriver driver = new ChromeDriver(options);

        driver.get("https://google.com");

        driver.close();
        driver.quit();
    }
}
