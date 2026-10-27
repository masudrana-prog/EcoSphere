package com.ecosphere.frontend;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

class RegisterUiTest extends BaseSeleniumTest {
  private void fill(String name, String email, String pw, String confirm, boolean terms) {
    driver.get(url("/register"));
    driver.findElement(By.name("name")).sendKeys(name);
    driver.findElement(By.name("email")).sendKeys(email);
    driver.findElement(By.name("phone")).sendKeys("+8801711111111");
    driver.findElement(By.name("password")).sendKeys(pw);
    driver.findElement(By.name("confirm")).sendKeys(confirm);
    if (terms) driver.findElement(By.id("t")).click();
    driver.findElement(By.cssSelector("form button.btn-eco")).click();
  }

  @Test void registerThenLoginAsNewUser() {
    String email = "ui" + System.nanoTime() + "@test.com";
    fill("UI Tester", email, "password123", "password123", true);
    wait.until(ExpectedConditions.urlContains("registered"));
    assertTrue(bodyContains("Account created"));
    loginAs(email);
    wait.until(ExpectedConditions.urlContains("/user"));
  }
  @Test void passwordMismatchShowsError() {
    fill("UI Tester", "mm" + System.nanoTime() + "@test.com", "password123", "different123", true);
    assertTrue(bodyContains("Passwords do not match"));
  }
  @Test void termsMustBeAccepted() {
    fill("UI Tester", "nt" + System.nanoTime() + "@test.com", "password123", "password123", false);
    assertTrue(bodyContains("Please accept the Terms"));
    assertTrue(driver.getCurrentUrl().contains("/register"));
  }
  @Test void orgFieldsAppearForNurseryRole() {
    driver.get(url("/register"));
    assertFalse(driver.findElements(By.cssSelector(".org-field")).get(0).isDisplayed());
    driver.findElement(By.cssSelector(".role-tab[data-r='NURSERY']")).click();
    assertTrue(driver.findElements(By.cssSelector(".org-field")).get(0).isDisplayed());
  }
}
