import driver.UndetectedDriver;
import driver.UndetectedOptions;

/**
 * The type Main.
 */
public class Main {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws InterruptedException the interrupted exception
     */
    public static void main(String[] args) throws InterruptedException {
        UndetectedOptions options = new UndetectedOptions(
                false,
                true,
                true,
                true
        );

        UndetectedDriver driver = UndetectedDriver.createDriver(options);

        driver.get("https://bot.sannysoft.com");

        Thread.sleep(5000);

        driver.close();
        driver.quit();
    }
}