import driver.UndetectedDriver;
import driver.UndetectedOptions;

import java.io.BufferedWriter;
import java.io.FileWriter;

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
    public static void main(String[] args) {
        UndetectedOptions options = new UndetectedOptions(
                true,
                true,
                true,
                true
        );

        UndetectedDriver driver = UndetectedDriver.createDriver(options);

        try {
            driver.get("https://bot.sannysoft.com");
            String source = driver.getPageSource();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("page.html"))) {
                writer.write(source);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            driver.quit();
        }
    }
}