package task3;

import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("Search History API Tests")
@Feature("API Endpoint Verification")
public class SearchHistoryApiTests {

    private static final String BASE_URL = "https://back.rail.ninja";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    private String encodeHistoryCookie(String json) {
        String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(base64, StandardCharsets.UTF_8);
    }

    @Test
    @Description("Check that /station/history returns 200 and JSON data")
    @Severity(SeverityLevel.CRITICAL)
    public void testHistoryEndpointReturns200AndJson() {
        String cookieValue = encodeHistoryCookie("""
            [
              {
                "passengers": {"adults": 1, "children": 0, "children_age": []},
                "form-mode": "basic-mode",
                "legs": {
                  "1": {
                    "departure_station": "672",
                    "arrival_station": "580",
                    "departure_date": "2025-12-31"
                  }
                }
              }
            ]
            """);

        Response response = given()
                .cookie("search_history", cookieValue)
                .when()
                .get("/api/v1/station/history")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .extract().response();

        assertThat(response.jsonPath().getList("$"), is(not(empty())));
    }

    @Test
    @Description("Verify that departure and arrival stations are correctly returned from the search history API")
    @Severity(SeverityLevel.CRITICAL)
    public void testDepartureAndArrivalStationsCorrect() {
        String cookieValue = encodeHistoryCookie("""
            [
              {
                "passengers": {"adults": 1, "children": 0, "children_age": []},
                "form-mode": "basic-mode",
                "legs": {
                  "1": {
                    "departure_station": "672",
                    "arrival_station": "580",
                    "departure_date": "2025-12-31"
                  }
                }
              }
            ]
            """);

        Response response = given()
                .cookie("search_history", cookieValue)
                .when()
                .get("/api/v1/station/history")
                .then()
                .statusCode(200)
                .extract().response();

        String dep = response.jsonPath().getString("[0].legs.1.departure_station.single_name");
        String arr = response.jsonPath().getString("[0].legs.1.arrival_station.single_name");

        assertThat(dep, equalTo("Prague"));
        assertThat(arr, equalTo("Vienna"));
    }

    @Test
    @Description("Verify that departure and arrival stations are correctly returned from the search history API")
    @Severity(SeverityLevel.CRITICAL)
    public void testDepartureDateCorrect() {
        String cookieValue = encodeHistoryCookie("""
            [
              {
                "passengers": {"adults": 1, "children": 0, "children_age": []},
                "form-mode": "basic-mode",
                "legs": {
                  "1": {
                    "departure_station": "672",
                    "arrival_station": "580",
                    "departure_date": "2025-12-31"
                  }
                }
              }
            ]
            """);

        Response response = given()
                .cookie("search_history", cookieValue)
                .when()
                .get("/api/v1/station/history")
                .then()
                .statusCode(200)
                .extract().response();

        String date = response.jsonPath().getString("[0].legs.1.departure_date");
        assertThat(date, startsWith("2025-12-31"));
    }

    @Test
    @Description("Verify that an empty search history cookie returns an empty response list")
    @Severity(SeverityLevel.MINOR)
    public void testEmptyCookieReturnsEmptyHistory() {
        Response response = given()
                .cookie("search_history", "")
                .when()
                .get("/api/v1/station/history")
                .then()
                .statusCode(200)
                .extract().response();

        assertThat(response.jsonPath().getList("$"), is(empty()));
    }

    @Test
    @Description("Verify that multiple search entries are correctly returned from the search history API")
    @Severity(SeverityLevel.NORMAL)
    public void testMultipleSearchesReturned() {
        String cookieValue = encodeHistoryCookie("""
            [
              {
                "passengers": {"adults": 1, "children": 0, "children_age": []},
                "form-mode": "basic-mode",
                "legs": {
                  "1": {
                    "departure_station": "672",
                    "arrival_station": "580",
                    "departure_date": "2025-12-31"
                  }
                }
              },
              {
                "passengers": {"adults": 2, "children": 1, "children_age": [5]},
                "form-mode": "basic-mode",
                "legs": {
                  "1": {
                    "departure_station": "580",
                    "arrival_station": "672",
                    "departure_date": "2026-01-10"
                  }
                }
              }
            ]
            """);

        Response response = given()
                .cookie("search_history", cookieValue)
                .when()
                .get("/api/v1/station/history")
                .then()
                .statusCode(200)
                .extract().response();

        assertThat(response.jsonPath().getList("$"), hasSize(2));
    }
}
