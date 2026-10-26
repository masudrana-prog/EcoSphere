package com.ecosphere.frontend;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

/**
 * Base class for Selenium UI tests. Boots the whole app on a random port (H2 + demo seed data) and drives headless Chrome.
 * Optional system properties:  -Dchrome.binary=/path/to/chrome   -Dwebdriver.chrome.driver=/path/to/chromedriver   -Dheadless=false
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class BaseSeleniumTest {
  @LocalServerPort int port;
  WebDriver driver;
  WebDriverWait wait;

  @BeforeEach void openBrowser() {
    ChromeOptions o = new ChromeOptions();
    if (!"false".equals(System.getProperty("headless"))) o.addArguments("--headless=new");
    o.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1400,1000", "--disable-gpu");
    String bin = System.getProperty("chrome.binary");
    if (bin != null && !bin.isBlank()) o.setBinary(bin);
    driver = new ChromeDriver(o);
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
  }
  @AfterEach void closeBrowser() { if (driver != null) driver.quit(); }

  String url(String path) { return "http://localhost:" + port + path; }

  void login(String email, String password) {
    driver.get(url("/login"));
    driver.findElement(By.name("email")).sendKeys(email);
    driver.findElement(By.name("password")).sendKeys(password);
    driver.findElement(By.cssSelector("form[action$='/login'] button")).click();
  }
  void loginAs(String email) { login(email, "password123"); }
  boolean bodyContains(String text) { return driver.findElement(By.tagName("body")).getText().contains(text); }
}
