package task2;

import helpers.PassengerFormHelper;
import io.qameta.allure.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@Epic("Passenger Form Tests")
@Feature("UI Form Interaction")
public class PassengerFormTests {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeEach
    @Step("Open passenger form before each test")
    public void setUp() throws InterruptedException {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
        driver.get("https://rail.ninja/");

        PassengerFormHelper helper = new PassengerFormHelper(driver, wait);
        helper.openPassengerForm("Mecca", "Medina", LocalDate.of(2025, 11, 5));
    }

    @AfterEach
    @Step("Close browser after each test")
    public void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {}
        }
    }

    @Test
    @Description("Verify that the Clear button resets all fields")
    @Severity(SeverityLevel.CRITICAL)
    public void testClearButtonResetsForm() {
        fillPassengerForm();

        WebElement clearButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[.//span[text()='Clear']]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", clearButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", clearButton);

        By nameLocator = By.id("checkout-passengers-form_passengersCategories_adult_0_full_name");
        WebElement nameInput = wait.until(ExpectedConditions.presenceOfElementLocated(nameLocator));
        WebElement maleRadio = driver.findElement(By.xpath("//input[@type='radio' and @value='male']"));
        WebElement citizenshipInput = driver.findElement(By.id("checkout-passengers-form_passengersCategories_adult_0_citizenship"));
        WebElement passportInput = driver.findElement(By.id("checkout-passengers-form_passengersCategories_adult_0_id_number"));
        List<WebElement> dobInputs = driver.findElement(By.id("checkout-passengers-form_passengersCategories_adult_0_dob"))
                .findElements(By.cssSelector("input.ant-select-selection-search-input"));

        Assertions.assertEquals("", nameInput.getAttribute("value"));
        Assertions.assertFalse(maleRadio.isSelected(), "Male radio button should be unselected after clear");
        Assertions.assertEquals("", citizenshipInput.getAttribute("value"));
        Assertions.assertEquals("", passportInput.getAttribute("value"));
        for (WebElement dobInput : dobInputs) {
            Assertions.assertEquals("", dobInput.getAttribute("value"));
        }
    }

    @Test
    @Description("Check validation errors when required fields are empty")
    @Severity(SeverityLevel.NORMAL)
    public void testValidationOfRequiredFields() {
        clickContinueButton();
        List<WebElement> errors = wait.until(ExpectedConditions
                .visibilityOfAllElementsLocatedBy(By.cssSelector(".ant-form-item-explain-error")));
        assertThat("Validation messages should appear", errors.size(), greaterThan(0));
    }

    @Test
    @Description("Confirm Email field appears after typing email")
    @Severity(SeverityLevel.MINOR)
    public void testConfirmEmailFieldAppears() {
        By emailLocator = By.id("checkout-passengers-form_clientDetails_user_email");
        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(emailLocator));
        emailInput.sendKeys("test@example.com");

        By confirmEmailLocator = By.id("checkout-passengers-form_clientDetails_confirm_user_email");
        WebElement confirmEmailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(confirmEmailLocator));
        assertThat("Confirm Email field should be displayed after typing in email", confirmEmailInput.isDisplayed());
    }

    @Test
    @Description("Verify that the passenger form can be successfully submitted with valid data")
    @Severity(SeverityLevel.CRITICAL)
    void testSuccessfulSubmission() {
        fillPassengerForm();

        By emailLocator = By.id("checkout-passengers-form_clientDetails_user_email");
        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(emailLocator));
        String uniqueEmail = "test+" + System.currentTimeMillis() + "@example.com";
        emailInput.sendKeys(uniqueEmail);

        By confirmEmailLocator = By.id("checkout-passengers-form_clientDetails_confirm_user_email");
        WebElement confirmEmailInput = wait.until(ExpectedConditions.presenceOfElementLocated(confirmEmailLocator));
        confirmEmailInput.sendKeys(uniqueEmail);

        clickContinueButton();
    }

    @Test
    @Description("Verify that the 'Confirm Email' field shows an error when it does not match the 'Email' field")
    @Severity(SeverityLevel.NORMAL)
    public void testConfirmEmailFieldValidation() {
        By emailLocator = By.id("checkout-passengers-form_clientDetails_user_email");
        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(emailLocator));
        emailInput.sendKeys("test@example.com");

        By confirmEmailLocator = By.id("checkout-passengers-form_clientDetails_confirm_user_email");
        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmEmailLocator));

        clickContinueButton();

        By confirmEmailErrorLocator = By.id("checkout-passengers-form_clientDetails_confirm_user_email_help");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(confirmEmailErrorLocator));
        Assertions.assertEquals("Please repeat your email", error.getText().trim());
    }

    @Step("Fill passenger form with default data")
    private void fillPassengerForm() {

        By nameLocator = By.id("checkout-passengers-form_passengersCategories_adult_0_full_name");
        WebElement nameInput = wait.until(ExpectedConditions.presenceOfElementLocated(nameLocator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", nameInput);
        nameInput.clear();
        nameInput.sendKeys("John Doe");

        WebElement genderRadio = driver.findElement(By.xpath("//input[@type='radio' and @value='" + "male" + "']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", genderRadio);

        WebElement citizenshipInput = driver.findElement(By.id("checkout-passengers-form_passengersCategories_adult_0_citizenship"));
        citizenshipInput.sendKeys("Saudi Arabia");
        citizenshipInput.sendKeys(Keys.ARROW_DOWN, Keys.ENTER);

        WebElement passportInput = driver.findElement(By.id("checkout-passengers-form_passengersCategories_adult_0_id_number"));
        passportInput.clear();
        passportInput.sendKeys("A1234567");

        WebElement dobContainer = driver.findElement(By.id("checkout-passengers-form_passengersCategories_adult_0_dob"));
        List<WebElement> dobSelects = dobContainer.findElements(By.cssSelector("input.ant-select-selection-search-input"));
        dobSelects.get(0).sendKeys("1", Keys.ENTER);
        dobSelects.get(1).sendKeys("January", Keys.ENTER);
        dobSelects.get(2).sendKeys("1990", Keys.ENTER);
    }

    @Step("Click the Continue button on the passenger form")
    private void clickContinueButton() {
        By continueBtnLocator = By.xpath("//form[@id='checkout-passengers-form']//button[.//span[text()='Continue']]");
        WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(continueBtnLocator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", continueButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", continueButton);
    }
}
