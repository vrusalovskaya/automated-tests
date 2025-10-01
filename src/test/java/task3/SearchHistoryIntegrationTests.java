package task3;

import helpers.ApiHelper;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Epic("Search History API Tests")
@Feature("Integration tests for verifying search history behavior")
public class SearchHistoryIntegrationTests {

    @Test
    @Story("Verify cookie and returned search data match")
    @Description("Test that after performing a timetable search, the search_history cookie is set and matches the data returned by the search history API")
    @Severity(SeverityLevel.CRITICAL)
    public void testSearchHistory_cookieAndDataMatch() {
        String body = """
                {
                  "passengers": { "adults": 1, "children": 0, "children_age": [] },
                  "legs": {
                    "1": {
                      "departure_station": "23e9ca21-c51d-41be-b421-94e2da736ce3",
                      "arrival_station": "8fbfe521-8d0c-4187-9076-ad1731b42ae9",
                      "departure_date": "05.11.2025"
                    }
                  }
                }
                """;

        Response postResp = ApiHelper.postTimetableSearch(body);
        Assertions.assertEquals(200, postResp.getStatusCode());

        String searchHistoryCookie = postResp.getCookie("search_history");
        Assertions.assertNotNull(searchHistoryCookie, "Expected search_history cookie in POST response");

        Response getResp = ApiHelper.getSearchHistory(searchHistoryCookie);
        Assertions.assertEquals(200, getResp.getStatusCode());

        JsonPath json = getResp.jsonPath();
        Assertions.assertFalse(json.getList("$").isEmpty(), "History should not be empty");

        String depName = json.getString("legs.'1'.departure_station.single_name[0]");
        String arrName = json.getString("legs.'1'.arrival_station.single_name[0]");
        String depDateStr = json.getString("legs.'1'.departure_date[0]");

        OffsetDateTime odt = OffsetDateTime.parse(depDateStr);
        LocalDate actualDate = odt.toLocalDate();
        LocalDate expectedDate = LocalDate.parse("05.11.2025", DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        Assertions.assertEquals("Mecca", depName);
        Assertions.assertEquals("Medina", arrName);
        Assertions.assertEquals(expectedDate, actualDate);

        int adults = json.getInt("passengers.adults[0]");
        int children = json.getInt("passengers.children[0]");
        Assertions.assertEquals(1, adults);
        Assertions.assertEquals(0, children);
    }

    @Test
    @Story("Verify single round trip search is recorded correctly")
    @Description("Test that a round-trip search with multiple legs returns correct stations and passenger counts")
    @Severity(SeverityLevel.CRITICAL)
    public void testSearchHistory_singleRoundTripSearch() {
        String body = """
                {
                  "passengers": { "adults": 2, "children": 1, "children_age": [7] },
                  "legs": {
                    "1": {
                      "departure_station": "23e9ca21-c51d-41be-b421-94e2da736ce3",
                      "arrival_station": "8fbfe521-8d0c-4187-9076-ad1731b42ae9",
                      "departure_date": "06.11.2025"
                    },
                    "2": {
                      "departure_station": "8fbfe521-8d0c-4187-9076-ad1731b42ae9",
                      "arrival_station": "23e9ca21-c51d-41be-b421-94e2da736ce3",
                      "departure_date": "07.11.2025"
                    }
                  }
                }
                """;

        Response postResp = ApiHelper.postTimetableSearch(body);
        Assertions.assertEquals(200, postResp.getStatusCode());

        String cookie = postResp.getCookie("search_history");
        Assertions.assertNotNull(cookie);

        Response getResp = ApiHelper.getSearchHistory(cookie);
        Assertions.assertEquals(200, getResp.getStatusCode());

        JsonPath json = getResp.jsonPath();

        Assertions.assertEquals("Mecca", json.getString("legs.'1'.departure_station.single_name[0]"), "Station mismatch for leg 1 dep");
        Assertions.assertEquals("Medina", json.getString("legs.'1'.arrival_station.single_name[0]"), "Station mismatch for leg 1 arr");

        Assertions.assertEquals("Medina", json.getString("legs.'2'.departure_station.single_name[0]"), "Station mismatch for leg 2 dep");
        Assertions.assertEquals("Mecca", json.getString("legs.'2'.arrival_station.single_name[0]"), "Station mismatch for leg 2 arr");

        Assertions.assertEquals(2, json.getInt("passengers.adults[0]"));
        Assertions.assertEquals(1, json.getInt("passengers.children[0]"));
    }

    @Test
    @Story("Verify passenger counts in search history")
    @Description("Test that the passenger counts and children ages are returned correctly from search history API")
    @Severity(SeverityLevel.NORMAL)
    public void testSearchHistory_passengerCounts() {
        String body = """
                { "passengers": {"adults":3,"children":2,"children_age":[4,6]},
                  "legs":
                  {"1":
                  {"departure_station":"23e9ca21-c51d-41be-b421-94e2da736ce3",
                  "arrival_station":"8fbfe521-8d0c-4187-9076-ad1731b42ae9",
                  "departure_date":"07.11.2025"}}}
                """;

        Response postResp = ApiHelper.postTimetableSearch(body);
        String cookie = postResp.getCookie("search_history");

        Response getResp = ApiHelper.getSearchHistory(cookie);
        JsonPath json = getResp.jsonPath();

        int adults = json.getInt("passengers.adults[0]");
        int children = json.getInt("passengers.children[0]");
        Assertions.assertEquals(3, adults);
        Assertions.assertEquals(2, children);

        int[] ages = json.getList("passengers.children_age[0]", Integer.class).stream().mapToInt(i -> i).toArray();
        Assertions.assertArrayEquals(new int[]{4, 6}, ages);
    }

    @Test
    @Story("Verify empty cookie returns empty search history")
    @Description("Test that an invalid or empty search_history cookie returns an empty list from the search history API")
    @Severity(SeverityLevel.MINOR)
    public void testSearchHistory_emptyCookie_returnsEmptyList() {
        Response getResp = ApiHelper.getSearchHistory("invalid_cookie_value");
        Assertions.assertEquals(200, getResp.getStatusCode());

        JsonPath json = getResp.jsonPath();
        Assertions.assertTrue(json.getList("$").isEmpty(), "History should be empty for invalid cookie");
    }

}
