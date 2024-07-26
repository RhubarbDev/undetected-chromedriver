import Driver.Patcher;
import Driver.UndetectedDriver;
import Driver.UndetectedOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        UndetectedOptions options = new UndetectedOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");

        UndetectedDriver driver = UndetectedDriver.createDriver(options, false, true, true);

        driver.get("https://bot.sannysoft.com");

        Thread.sleep(5000);

        driver.close();
        driver.quit();
    }
}