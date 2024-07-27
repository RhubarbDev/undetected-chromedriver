package Driver;

import Utils.PatcherUtil;
import org.openqa.selenium.chrome.ChromeDriver;

public class UndetectedDriver extends ChromeDriver {

    public UndetectedDriver(UndetectedOptions options) {
        super(options);
    }

    public static UndetectedDriver createDriver(UndetectedOptions options) {
        return createDriver(options, false, false, false);
    }

    public static UndetectedDriver createDriver(
            UndetectedOptions options,
            boolean headless,
            boolean suppressWelcome,
            boolean noSandbox
    ) {
        // Not fully supported.
        if (headless) {
            try {
                int version = Integer.parseInt(PatcherUtil.getInstalledChromeVersion().toString().split("\\.")[0]);
                if (version < 108) {
                    options.addArguments("--headless=chrome");
                } else {
                    options.addArguments("--headless=new");
                }
            } catch (Exception ignored) {
                options.addArguments("--headless=new");
            }
        }

        if (noSandbox) {
            options.addArguments("--no-sandbox", "--test-type");
        }

        if (suppressWelcome) {
            options.addArguments("--no-default-browser-check", "--no-first-run");
        }

        Patcher patcher = new Patcher();

        System.setProperty("webdriver.chrome.driver", patcher.getDriver().toString());

        return new UndetectedDriver(options);
    }




}
