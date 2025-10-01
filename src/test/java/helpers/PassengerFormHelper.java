package helpers;

import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class PassengerFormHelper {
    private final WebDriver DRIVER;
    private final WebDriverWait WAIT;

    public PassengerFormHelper(WebDriver driver, WebDriverWait wait) {
        this.DRIVER = driver;
        this.WAIT = wait;
    }

    @Step("Open passenger form: from {departureStation} to {arrivalStation} on {date}")
    public void openPassengerForm(String departureStation, String arrivalStation, LocalDate date) throws InterruptedException {
        acceptCookiesIfPresent(2);
        enterStation("departure_station", departureStation);
        enterStation("arrival_station", arrivalStation);
        pickDate(date);

        WebElement searchButton = WAIT.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Search') or @type='submit']")));
        ((JavascriptExecutor) DRIVER).executeScript("arguments[0].scrollIntoView({block:'center'});", searchButton);
        searchButton.click();

        String originalWindow = DRIVER.getWindowHandle();

        new WebDriverWait(DRIVER, Duration.ofSeconds(5)).until(d -> {
            Set<String> handles = DRIVER.getWindowHandles();
            if (handles.size() > 1) {
                for (String handle : handles) {
                    if (!handle.equals(originalWindow)) {
                        DRIVER.switchTo().window(handle);
                        return true;
                    }
                }
            }

            return Objects.requireNonNull(DRIVER.getCurrentUrl()).contains("/timetable");
        });


        new WebDriverWait(DRIVER, Duration.ofSeconds(15)).until(d -> {
            String url = d.getCurrentUrl();
            assert url != null;
            return url.contains("/trains/order/timetable") || url.contains("/v9/trains/order/timetable");
        });

        selectFlexibleFare(DRIVER);
    }

    @Step("Accept cookies if present")
    private void acceptCookiesIfPresent(long timeoutSeconds) {
        try {
            new WebDriverWait(DRIVER, Duration.ofSeconds(timeoutSeconds)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[@data-cookiefirst-action='accept' or contains(.,'Accept')]")
                    )
            ).click();
        } catch (TimeoutException ignored) {
        }
    }

    @Step("Enter station in input {inputId}: {stationName}")
    private void enterStation(String inputId, String stationName) {
        WebElement input = WAIT.until(ExpectedConditions.elementToBeClickable(By.id(inputId)));
        input.clear();
        input.click();
        input.sendKeys(stationName);

        try {
            WebElement suggestion = new WebDriverWait(DRIVER, Duration.ofSeconds(3)).until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//div[contains(@class,'autocomplete-suggestion')][contains(.,'" + stationName + "')]")
                    )
            );
            suggestion.click();
            return;
        } catch (TimeoutException e) {
            acceptCookiesIfPresent(3);
        }

        try {
            input.click();
            input.sendKeys(Keys.ARROW_DOWN);
            input.sendKeys(Keys.ENTER);
            new WebDriverWait(DRIVER, Duration.ofSeconds(2)).until(d ->
                    input.getAttribute("value") != null &&
                    Objects.requireNonNull(input.getAttribute("value")).toLowerCase().contains(stationName.toLowerCase())
            );
            return;
        } catch (Exception ignored) {
        }

        ((JavascriptExecutor) DRIVER).executeScript(
                "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input'));",
                input, stationName);
    }

    @Step("Pick date: {targetDate}")
    private void pickDate(LocalDate targetDate) throws InterruptedException {
        WebElement dateWrapper = WAIT.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("div.ant-picker-input")));
        dateWrapper.click();

        while (true) {
            WebElement monthButton = WAIT.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("button.ant-picker-month-btn")));
            WebElement yearButton = DRIVER.findElement(By.cssSelector("button.ant-picker-year-btn"));

            String currentMonth = monthButton.getText().trim();
            String currentYear = yearButton.getText().trim();

            if (currentMonth.equalsIgnoreCase(targetDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                && currentYear.equals(String.valueOf(targetDate.getYear()))) {
                break;
            }

            WebElement nextButton = WAIT.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.ant-picker-header-next-btn")));
            nextButton.click();
            Thread.sleep(300);
        }

        WebElement dayCell = WAIT.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("td[title='" + targetDate + "']")));
        ((JavascriptExecutor) DRIVER).executeScript("arguments[0].scrollIntoView({block:'center'});", dayCell);
        dayCell.click();
    }

    @Step("Select first visible element")
    private WebElement findFirstVisible(By locator) {
        List<WebElement> elements = WAIT.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        return elements.stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
    }

    @Step("Select flexible fare on timetable page")
    private void selectFlexibleFare(WebDriver driver) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String currentUrl = driver.getCurrentUrl();

        assert currentUrl != null;
        if (currentUrl.contains("/v9/trains/order/timetable")) {
            By bulletTrainLocator = By.xpath("//div[.//span[translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')='BULLET TRAIN']]");
            wait.until(ExpectedConditions.visibilityOfElementLocated(bulletTrainLocator));
            WebElement firstTrainCard = driver.findElement(bulletTrainLocator);

            WebElement expandButton = firstTrainCard.findElement(
                    By.xpath(".//button[@data-variant='tertiary']")
            );

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", expandButton);
            Thread.sleep(500); // tiny delay for animations
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", expandButton);

            WebElement flexibleFareBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button[data-fare-name='Flexible']")
            ));
            flexibleFareBtn.click();

            WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button[data-variant='primary']")
            ));
            continueBtn.click();

        } else if (currentUrl.contains("/trains/order/timetable")) {

            WebElement seatButton = findFirstVisible(By.xpath("//button[contains(.,'Select Seats')]"));
            assertNotNull("No visible 'Select Seats' button found", seatButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", seatButton);
            wait.until(ExpectedConditions.elementToBeClickable(seatButton));
            seatButton.click();

            List<WebElement> fares = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath("//div[@data-fare-name='Flexible']")
            ));

            boolean clicked = false;
            for (WebElement fare : fares) {
                try {
                    WebElement button = fare.findElement(By.tagName("button"));
                    wait.until(ExpectedConditions.elementToBeClickable(button));
                    button.click();
                    clicked = true;
                    break;
                } catch (Exception ignored) {
                }
            }

            if (!clicked) {
                throw new RuntimeException("No clickable Flexible fare button found in old layout!");
            }
        } else {
            throw new RuntimeException("Unknown page layout: " + currentUrl);
        }
    }
}
