package com.ecosphere.frontend;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import static org.junit.jupiter.api.Assertions.*;

/** Covers the Buy Tree -> Place order flow (regression test for the 403 Whitelabel error caused by the missing CSRF token). */
class BuyTreeUiTest extends BaseSeleniumTest {
  private void openFirstBuyModal() {
    loginAs("afia@ecosphere.com");
    driver.get(url("/user/buy"));
    driver.findElements(By.cssSelector(".buy-btn:not([disabled])")).get(0).click();
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("buyModal")));
    wait.until(ExpectedConditions.elementToBeClickable(By.name("address")));
  }
  private void submitOrder() { driver.findElement(By.cssSelector("#buyForm .modal-footer .btn-eco")).click(); }

  @Test void buyPageListsTrees() {
    loginAs("afia@ecosphere.com");
    driver.get(url("/user/buy"));
    assertTrue(bodyContains("Buy Tree"));
    assertFalse(driver.findElements(By.cssSelector(".buy-btn")).isEmpty());
  }
  @Test void searchFiltersTrees() {
    loginAs("afia@ecosphere.com");
    driver.get(url("/user/buy"));
    driver.findElement(By.name("q")).sendKeys("Neem");
    driver.findElement(By.cssSelector("form[method='get'] .btn-eco")).click();
    wait.until(ExpectedConditions.urlContains("q=Neem"));
    assertTrue(bodyContains("Neem Tree"));
    assertFalse(bodyContains("Red Oak"));
  }
  @Test void buyFormContainsCsrfToken() {
    openFirstBuyModal();
    assertFalse(driver.findElements(By.cssSelector("#buyForm input[name='_csrf']")).isEmpty(), "CSRF hidden input missing from buy form");
  }
  @Test void placeCodOrderSucceeds() {
    openFirstBuyModal();
    driver.findElement(By.name("address")).sendKeys("House 12, Road 3, Mirpur 10");
    submitOrder();
    wait.until(ExpectedConditions.urlContains("/user/orders"));
    assertFalse(bodyContains("Whitelabel"), "Order must not end in an error page");
    assertTrue(bodyContains("placed"));
    assertTrue(bodyContains("House 12") || bodyContains("Cash on delivery"));
  }
  @Test void totalUpdatesWithQuantity() {
    openFirstBuyModal();
    WebElement qty = driver.findElement(By.id("mQty"));
    String one = driver.findElement(By.id("mTotal")).getText();
    qty.clear(); qty.sendKeys("3");
    assertNotEquals(one, driver.findElement(By.id("mTotal")).getText());
  }
  @Test void bkashFieldsToggle() {
    openFirstBuyModal();
    assertFalse(driver.findElement(By.id("bkBox")).isDisplayed());
    new Select(driver.findElement(By.id("mPay"))).selectByValue("BKASH");
    assertTrue(driver.findElement(By.id("bkBox")).isDisplayed());
  }
  @Test void placeBkashOrderSucceeds() {
    openFirstBuyModal();
    driver.findElement(By.name("address")).sendKeys("Banani, Dhaka");
    new Select(driver.findElement(By.id("mPay"))).selectByValue("BKASH");
    driver.findElement(By.id("bkSender")).sendKeys("01712345678");
    driver.findElement(By.id("bkTrx")).sendKeys("UI" + (System.nanoTime() % 1000000000L));
    submitOrder();
    wait.until(ExpectedConditions.urlContains("/user/orders"));
    assertTrue(bodyContains("bKash"));
  }
  @Test void pendingOrderCanBeCancelled() {
    openFirstBuyModal();
    driver.findElement(By.name("address")).sendKeys("Uttara, Dhaka");
    submitOrder();
    wait.until(ExpectedConditions.urlContains("/user/orders"));
    driver.findElement(By.cssSelector("form[action*='/cancel'] button")).click();
    wait.until(ExpectedConditions.alertIsPresent()).accept();
    wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Order cancelled"));
  }
}
