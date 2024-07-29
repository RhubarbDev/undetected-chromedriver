# Undetected Chromedriver for Java.

I based this project on  https://github.com/ultrafunkamsterdam/undetected-chromedriver 
<br>
However I haven't implemented all the featues found in that project.

## Capabilities.

- Downloads Chromedriver compatible with installed Chrome version and patches it.
- Adds options to the driver that should minimize bot detection.

## Usage

```java
import driver.UndetectedOptions;
import driver.UndetectedDriver;

UndetectedOptions options = new UndetectedOptions();

UndetectedDriver driver = UndetectedDriver.createDriver(options);

driver.get("https://google.com");

System.out.println(driver.getTitle());

driver.quit();
```

See [Main](src/test/java/Main.java) for example.
