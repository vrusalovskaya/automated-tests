package task3;

import helpers.ApiHelper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.*;

public class SearchHistoryIntegrationTests {

    @Test
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
        assertEquals(200, postResp.getStatusCode());

        String searchHistoryCookie = postResp.getCookie("search_history");
        assertNotNull("Expected search_history cookie in POST response", searchHistoryCookie);

        Response getResp = ApiHelper.getSearchHistory(searchHistoryCookie);
        assertEquals(200, getResp.getStatusCode());

        JsonPath json = getResp.jsonPath();
        assertFalse("History should not be empty", json.getList("$").isEmpty());

        String depName = json.getString("legs.'1'.departure_station.single_name[0]");
        String arrName = json.getString("legs.'1'.arrival_station.single_name[0]");
        String depDateStr = json.getString("legs.'1'.departure_date[0]");

        OffsetDateTime odt = OffsetDateTime.parse(depDateStr);
        LocalDate actualDate = odt.toLocalDate();
        LocalDate expectedDate = LocalDate.parse("05.11.2025", DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        assertEquals("Mecca", depName);
        assertEquals("Medina", arrName);
        assertEquals(expectedDate, actualDate);

        int adults = json.getInt("passengers.adults[0]");
        int children = json.getInt("passengers.children[0]");
        assertEquals(1, adults);
        assertEquals(0, children);
    }

    @Test
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
        assertEquals(200, postResp.getStatusCode());

        String cookie = postResp.getCookie("search_history");
        assertNotNull(cookie);

        Response getResp = ApiHelper.getSearchHistory(cookie);
        assertEquals(200, getResp.getStatusCode());

        JsonPath json = getResp.jsonPath();

        assertEquals("Station mismatch for leg 1 dep",
                "Mecca", json.getString("legs.'1'.departure_station.single_name[0]"));
        assertEquals("Station mismatch for leg 1 arr",
                "Medina", json.getString("legs.'1'.arrival_station.single_name[0]"));

        assertEquals("Station mismatch for leg 2 dep",
                "Medina", json.getString("legs.'2'.departure_station.single_name[0]"));
        assertEquals("Station mismatch for leg 2 arr",
                "Mecca", json.getString("legs.'2'.arrival_station.single_name[0]"));

        assertEquals(2, json.getInt("passengers.adults[0]"));
        assertEquals(1, json.getInt("passengers.children[0]"));
    }

    @Test
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
        assertEquals(3, adults);
        assertEquals(2, children);

        int[] ages = json.getList("passengers.children_age[0]", Integer.class).stream().mapToInt(i -> i).toArray();
        assertArrayEquals(new int[]{4, 6}, ages);
    }

    @Test
    public void testSearchHistory_emptyCookie_returnsEmptyList() {
        Response getResp = ApiHelper.getSearchHistory("invalid_cookie_value");
        assertEquals(200, getResp.getStatusCode());

        JsonPath json = getResp.jsonPath();
        assertTrue("History should be empty for invalid cookie", json.getList("$").isEmpty());
    }

}
