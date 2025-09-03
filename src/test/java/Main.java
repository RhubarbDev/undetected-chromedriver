import driver.UndetectedDriver;
import driver.UndetectedOptions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        UndetectedOptions options = new UndetectedOptions(
                false,
                true,
                true
        );

        UndetectedDriver driver = UndetectedDriver.createDriver(options);

        try {
            driver.get("https://bot.sannysoft.com");
            String source = driver.getPageSource();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("page.html"))) {
                writer.write(Objects.requireNonNullElse(source, ":("));
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        } finally {
            driver.quit();
        }
    }
}