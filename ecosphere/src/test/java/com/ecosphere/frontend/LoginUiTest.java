package com.ecosphere.frontend;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

class LoginUiTest extends BaseSeleniumTest {
  @Test void loginPageLoads() {
    driver.get(url("/login"));
    assertTrue(bodyContains("Portal Access"));
  }
  @Test void userLoginGoesToUserDashboard() { loginAs("afia@ecosphere.com"); wait.until(ExpectedConditions.urlContains("/user")); assertTrue(driver.getCurrentUrl().endsWith("/user")); }
  @Test void adminLoginGoesToAdminDashboard() { loginAs("admin@ecosphere.com"); wait.until(ExpectedConditions.urlContains("/admin")); }
  @Test void ngoLoginGoesToNgoDashboard() { loginAs("ngo@ecosphere.com"); wait.until(ExpectedConditions.urlContains("/ngo")); }
  @Test void nurseryLoginGoesToNurseryDashboard() { loginAs("nursery@ecosphere.com"); wait.until(ExpectedConditions.urlContains("/nursery")); }

  @Test void wrongPasswordShowsError() {
    login("afia@ecosphere.com", "totally-wrong");
    wait.until(ExpectedConditions.urlContains("error"));
    assertTrue(bodyContains("Invalid credentials"));
  }
  @Test void protectedPageRedirectsToLogin() {
    driver.get(url("/user/buy"));
    wait.until(ExpectedConditions.urlContains("/login"));
  }
  @Test void userCannotOpenAdminPage() {
    loginAs("afia@ecosphere.com");
    wait.until(ExpectedConditions.urlContains("/user"));
    driver.get(url("/admin"));
    assertTrue(bodyContains("403") || bodyContains("Forbidden"));
  }
  @Test void roleTabsChangeHint() {
    driver.get(url("/login"));
    driver.findElements(By.cssSelector(".role-tab")).get(2).click();
    assertTrue(driver.findElement(By.id("hint")).getText().startsWith("Nursery"));
  }
}
