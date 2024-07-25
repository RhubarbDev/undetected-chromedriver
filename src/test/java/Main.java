import Driver.Patcher;
import Utils.UserAgentUtil;
import com.google.common.base.StandardSystemProperty;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        Patcher patcher = new Patcher();
        Path path = patcher.getDriver();

        if (!path.toFile().setExecutable(true)) {
            throw new RuntimeException("Couldn't set executable.");
        }

        System.setProperty("webdriver.chrome.driver", path.toString());

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless");

        ChromeDriver driver = new ChromeDriver(options);
        String userAgent = (String) ((JavascriptExecutor) driver).executeScript("return navigator.userAgent;");
        System.out.println(userAgent);
        options.addArguments("user-agent=" + UserAgentUtil.fixUserAgent(userAgent));
        driver.quit();
        driver = new ChromeDriver(options);

        userAgent = (String) ((JavascriptExecutor) driver).executeScript("return navigator.userAgent;");
        System.out.println(userAgent);


        driver.get("https://google.com");
        System.out.println(driver.getTitle());

        driver.close();
        driver.quit();
    }
}