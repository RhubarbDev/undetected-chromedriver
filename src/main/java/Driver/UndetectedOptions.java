package Driver;

import Utils.PatcherUtil;
import Utils.UserAgentUtil;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Collections;

public class UndetectedOptions extends ChromeOptions {
    public UndetectedOptions() {
        this(false, false, false, false);
    }

    public UndetectedOptions(
            boolean headless
    ) {
        this(headless, false, false, false);
    }

    public UndetectedOptions(
            boolean headless,
            boolean suppressWelcome
    ) {
        this(headless, suppressWelcome, false, false);
    }

    public UndetectedOptions(
            boolean headless,
            boolean suppressWelcome,
            boolean noSandbox
    ) {
        this(headless, suppressWelcome, noSandbox, false);
    }

    public UndetectedOptions(
            boolean headless,
            boolean suppressWelcome,
            boolean noSandbox,
            boolean devToolsActivePortsFix
    ) {
        super();

        this.addArguments("--disable-blink-features=AutomationControlled");
        this.addArguments("disable-infobars");
        this.addArguments("window-size=192,1080");

        this.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        this.setExperimentalOption("useAutomationExtension", null);

        if (headless) {
            this.addArguments("user-agent=" + UserAgentUtil.genUserAgent());

            try {
                int version = (Integer)PatcherUtil.getInstalledChromeVersion().getPart(0);
                if (version < 108) {
                    this.addArguments("--headless=chrome");
                } else {
                    this.addArguments("--headless=new");
                }
            } catch (Exception ignored) {
                this.addArguments("--headless=new");
            }
        }

        if (suppressWelcome) {
            this.addArguments("--no-default-browser-check", "--no-first-run");
        }

        if (noSandbox) {
            this.addArguments("--no-sandbox", "--test-type");
        }

        if (devToolsActivePortsFix) {
            this.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        }
    }
}
