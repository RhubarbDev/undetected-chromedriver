package Driver;

import Utils.DriverUtils;
import Utils.UserAgentUtil;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Collections;

public class UndetectedOptions extends ChromeOptions {

    public UndetectedOptions() {
        super();

        // Options to avoid bot detection
        this.addArguments("--disable-blink-features=AutomationControlled");
        this.addArguments("disable-infobars");
        this.addArguments("window-size=1920,1080");
        this.addArguments("user-agent=" + UserAgentUtil.genUserAgent());

        // Experimental Options to avoid bot detection
        this.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        this.setExperimentalOption("useAutomationExtension", null);

        // Set the remote debugging port
        this.addArguments("--remote-debugging-port=" + DriverUtils.getFreePort());











    }
}
