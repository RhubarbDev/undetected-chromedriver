package driver;

import org.openqa.selenium.chrome.ChromeOptions;
import utils.UserAgent;

import java.util.Collections;

/**
 * The type Undetected options.
 */
public class UndetectedOptions extends ChromeOptions {
    public UndetectedOptions(boolean headless, boolean disableSandbox, boolean devToolsActivePortsFix) {
        super();

        if (headless) {
            this.addArguments("--headless=new");
            this.addArguments("--disabled-gpu");
        }

        this.addArguments("--window-size=1920,1080");
        this.addArguments("--disable-blink-features=AutomationControlled");
        this.addArguments("disable-infobars");

        this.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        this.setExperimentalOption("useAutomationExtension", null);

        this.addArguments("user-agent=" + UserAgent.genUserAgent());

        this.addArguments("--no-default-browser-check", "--no-first-run");

        if (disableSandbox) {
            this.addArguments("--no-sandbox", "--test-type");
        }

        if (devToolsActivePortsFix) {
            this.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        }
    }
}