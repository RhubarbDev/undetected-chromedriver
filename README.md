# Undetected Chromedriver for Java.

I based this project on  https://github.com/ultrafunkamsterdam/undetected-chromedriver 
<br>
However I haven't implemented all the featues found in that project.

## Capabilities.

- Downloads Chromedriver compatible with installed Chrome version and patches it.
- Adds options to the driver that should minimize bot detection.

## Usage

```java
UndetectedOptions options = new UndetectedOptions();

UndetectedDriver driver = new UndetectedDriver(options);

driver.get("https://google.com");

System.out.println(driver.getTitle());

driver.close();
driver.quit();
```

See [Main](src/test/java/Main.java) for example.
