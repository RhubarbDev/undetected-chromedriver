package Driver;

import Utils.PatcherUtil;
import Utils.UserAgentUtil;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * The type Undetected driver.
 */
public class UndetectedDriver extends ChromeDriver {

    /**
     * Instantiates a new Undetected driver.
     *
     * @param options ChromeOptions passed to the driver.
     */
    private UndetectedDriver(UndetectedOptions options) {
        super(options);
    }

    /**
     * Create driver undetected driver.
     *
     * @param options         the options
     */
    public static UndetectedDriver createDriver(UndetectedOptions options) {

        Patcher patcher = new Patcher();

        System.setProperty("webdriver.chrome.driver", patcher.getDriver().toString());

        return new UndetectedDriver(options);
    }
}