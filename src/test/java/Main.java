import Driver.Patcher;
import Driver.UndetectedOptions;
import org.openqa.selenium.chrome.ChromeDriver;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        UndetectedOptions opts = new UndetectedOptions();
        Patcher patcher = new Patcher();

        System.setProperty("webdriver.chrome.driver", patcher.getDriver().toString());

        ChromeDriver driver;
        try {
            driver = new ChromeDriver(opts);
        } catch (Exception ignore){
            try {
                opts.addArguments("--no-sandbox");
                opts.addArguments("--disable-dev-shm-usage");
                driver = new ChromeDriver(opts);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
        }

        driver.get("https://bot.sannysoft.com");

        System.out.println(driver.getTitle());

        Thread.sleep(50000);

        driver.close();
        driver.quit();
    }
}