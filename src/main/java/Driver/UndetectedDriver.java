package Driver;

import org.openqa.selenium.chrome.ChromeDriver;

public class UndetectedDriver extends ChromeDriver {

    public UndetectedDriver() {
        this(new UndetectedOptions());
    }

    public UndetectedDriver(UndetectedOptions options) {
        super(options);
    }
}
