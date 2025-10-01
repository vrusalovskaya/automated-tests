package task1;

import helpers.PassengerFormHelper;
import io.qameta.allure.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;

@Epic("Passenger Form Tests")
@Feature("UI Form Interaction")
class NameDisplayInHeaderTest {
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
        driver.get("https://rail.ninja/");
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @Description("Check that the passenger's name updates in the header after typing")
    @Severity(SeverityLevel.CRITICAL)
    void testNameDisplayInHeader() throws InterruptedException {
        PassengerFormHelper helper = new PassengerFormHelper(driver, wait);
        helper.openPassengerForm("Mecca", "Medina",
                LocalDate.of(2025, 11, 5));

        By fullNameLocator = By.id("checkout-passengers-form_passengersCategories_adult_0_full_name");
        WebElement fullNameInput = wait.until(ExpectedConditions.presenceOfElementLocated(fullNameLocator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", fullNameInput);

        String expectedName = "John Doe";
        fullNameInput.clear();
        fullNameInput.sendKeys(expectedName);

        WebElement headerElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("div.sc-cf59c1f-0.ixREea span.sc-cf59c1f-1.dfrRB"))
        );

        String actualName = headerElement.getText();
        Assertions.assertEquals(expectedName, actualName, "Header did not update with entered passenger name");
    }
}
