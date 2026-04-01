package com.onlinestore;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.time.Duration;
import java.util.Properties;

public class CartTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private Properties config;

    @BeforeClass
    public void setup() {
        // InitConfig
        config = new Properties();
        try (java.io.FileInputStream input = new java.io.FileInputStream("src/test/resources/config.properties")) {
            config.load(input);
        } catch (Exception ex) {
            System.out.println("Property error");
            ex.printStackTrace();
        }

        // InitializeDriver
        System.out.println("Initializing Driver...");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("start-maximized");
        options.addArguments("--disable-notifications");
        
        // ExtractProfileSetting
        String useProfile = config.getProperty("periplus.useLocalProfile");
        
        // ValidateProfileSetting
        if (useProfile == null || useProfile.trim().isEmpty()) {
            Assert.fail("CRITICAL ERROR: 'periplus.useLocalProfile' (Y/N) missing from config.properties!");
        }
        
        if (useProfile.equalsIgnoreCase("Y")) {
            System.out.println("Local Profile ON");
            String profilePath = System.getProperty("user.dir") + File.separator + "local-chrome-profile";
            options.addArguments("user-data-dir=" + profilePath);
        } else {
            System.out.println("Local Profile OFF");
        }
        
        // StartDriver
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @Test
    public void testAddBookToCart() {
        // LoadCredentials
        String email = config.getProperty("periplus.email");
        String password = config.getProperty("periplus.password");
        
        // LoadBookTitle
        String bookTitle = config.getProperty("periplus.bookTitle");
        String baseUrl = config.getProperty("periplus.baseUrl");
        
        // ValidateTestParams
        if (bookTitle == null || bookTitle.trim().isEmpty()) {
            Assert.fail("CRITICAL ERROR: 'periplus.bookTitle' missing from config.properties!");
        }
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            Assert.fail("CRITICAL ERROR: 'periplus.baseUrl' missing from config.properties!");
        }

        try {
            // NavigateLogin
            System.out.println("Navigating Login...");
            driver.get(baseUrl + "/index.php?route=account/login");

            // SubmitCredentials
            System.out.println("Login...");
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='email' or @id='input-email']")));
            emailInput.sendKeys(email);

            WebElement passwordInput = driver.findElement(By.xpath("//input[@name='password' or @id='input-password']"));
            passwordInput.sendKeys(password);

            WebElement loginButton = driver.findElement(By.xpath("//input[@value='Login' or @type='submit']"));
            loginButton.click();

            // VerifyLogin
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("login")));
            System.out.println("Login Success");

            // NavigateHome
            System.out.println("Searching...");
            driver.get(baseUrl + "/");
            
            // SearchProduct
            WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.name("filter_name")));
            searchBox.sendKeys(bookTitle);
            searchBox.sendKeys(Keys.ENTER);

            // LocateProduct
            System.out.println("Locating product...");
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("preloader")));
            
            // FormatSearchTerm
            String shortSearchTerm = bookTitle.contains(":") ? bookTitle.split(":")[0].toLowerCase().trim() : (bookTitle.length() > 12 ? bookTitle.substring(0, 12).toLowerCase() : bookTitle.toLowerCase());
            String xpathQuery = "//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + shortSearchTerm + "')]";
            
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpathQuery)));
            
            // NavigateProduct
            System.out.println("Navigating product...");
            WebElement bookLink = driver.findElement(By.xpath("(" + xpathQuery + ")[1]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", bookLink);
            
            // AddCart
            System.out.println("Adding cart...");
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("preloader")));
            WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='button-cart' or @id='btn-add-to-cart' or contains(@class, 'btn-add-to-cart') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'add to cart')] | //a[contains(@class, 'btn-add-to-cart') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'add to cart') or contains(@class, 'add-to-cart')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addToCartBtn);

            // HandleModal
            System.out.println("Handling Modal...");
            try { 
                Thread.sleep(2000); 
                driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE); 
                ((JavascriptExecutor) driver).executeScript("var elems = document.querySelectorAll('.modal, .modal-backdrop, .overlay, .ui-widget-overlay, .mfp-bg, .mfp-wrap'); for(var i=0; i<elems.length; i++){elems[i].style.display='none';}");
            } catch (Exception x) {}
            System.out.println("Added to Cart");

            // ViewCart
            System.out.println("Clicking Shopping Bag...");
            try { Thread.sleep(1500); } catch (Exception x) {}
            
            WebElement cartBag = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(@class, 'ti-bag') or contains(@id, 'cart')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cartBag);

            // VerifyNavigation
            System.out.println("Navigating Cart...");
            driver.get(baseUrl + "/checkout/cart");
            wait.until(ExpectedConditions.urlContains("cart"));

            // VerifyCart
            System.out.println("Verifying Cart...");
            WebElement body = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
            
            String cartText = body.getText().toLowerCase();
            boolean isBookInCart = cartText.contains(shortSearchTerm); 

            if (isBookInCart) {
                System.out.println("Verified");
            } else {
                Assert.fail("Not in cart");
            }
            
            Assert.assertTrue(isBookInCart, "Item in cart");

        } catch (Exception e) {
            System.out.println("Test Failed");
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            System.out.println("Complete");
        }
    }
}
