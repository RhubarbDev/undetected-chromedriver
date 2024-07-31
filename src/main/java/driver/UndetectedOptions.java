package driver;

import utils.OSUtils;
import utils.UserAgentUtil;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Collections;

/**
 * The type Undetected options.
 */
public class UndetectedOptions extends ChromeOptions {

    private final boolean headless;

    public boolean isHeadless() {
        return headless;
    }
    
    /**
     * Instantiates a new Undetected options.
     */
    public UndetectedOptions() {
        this(false, false, false, false);
    }

    /**
     * Instantiates a new Undetected options.
     *
     * @param headless add headless driver options
     */
    public UndetectedOptions(
            boolean headless
    ) {
        this(headless, false, false, false);
    }

    /**
     * Instantiates a new Undetected options.
     *
     * @param headless        add headless driver options
     * @param suppressWelcome add suppressWelcome options
     */
    public UndetectedOptions(
            boolean headless,
            boolean suppressWelcome
    ) {
        this(headless, suppressWelcome, false, false);
    }

    /**
     * Instantiates a new Undetected options.
     *
     * @param headless        add headless driver options
     * @param suppressWelcome add suppressWelcome options
     * @param noSandbox       add noSandbox options
     */
    public UndetectedOptions(
            boolean headless,
            boolean suppressWelcome,
            boolean noSandbox
    ) {
        this(headless, suppressWelcome, noSandbox, false);
    }

    /**
     * Instantiates a new Undetected options.
     *
     * @param headless               add headless driver options
     * @param suppressWelcome        add suppressWelcome options
     * @param noSandbox              add noSandbox options
     * @param devToolsActivePortsFix add devToolActivePortsFix options
     */
    public UndetectedOptions(
            boolean headless,
            boolean suppressWelcome,
            boolean noSandbox,
            boolean devToolsActivePortsFix
    ) {
        super();

        this.headless = headless;

        this.addArguments("--disable-blink-features=AutomationControlled");
        this.addArguments("disable-infobars");
        this.addArguments("window-size=192,1080");

        this.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        this.setExperimentalOption("useAutomationExtension", null);

        if (headless) {
            this.addArguments("user-agent=" + UserAgentUtil.genUserAgent());

            try {
                int  version = (Integer) OSUtils.getInstalledChromeVersion(OSUtils.getOS().command()).getPart(0);
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
