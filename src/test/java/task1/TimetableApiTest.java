package task1;

import helpers.ApiHelper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.Assert.*;

public class TimetableApiTest {

    @Test
    public void testTimetable_v2_returns_expected_trains() {
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

        Response resp = ApiHelper.postTimetableSearch(body);

        assertEquals("Unexpected status code", 200, resp.getStatusCode());

        String ct = resp.getContentType();
        assertNotNull("Content-Type header is missing", ct);
        assertTrue("Content-Type should start with 'application/json' but was: " + ct,
                ct.toLowerCase().startsWith("application/json"));

        JsonPath json = resp.jsonPath();

        String topDep = json.getString("departure_station.single_name");
        String topArr = json.getString("arrival_station.single_name");
        assertEquals("Top-level departure_station.single_name mismatch", "Mecca", topDep);
        assertEquals("Top-level arrival_station.single_name mismatch", "Medina", topArr);

        LocalDate expectedDate = LocalDate.parse("05.11.2025", DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        Map<String, ?> trains = json.getMap("trains");
        assertNotNull("Response must contain 'trains' object", trains);
        assertFalse("Trains map must not be empty", trains.isEmpty());

        for (String trainKey : trains.keySet()) {
            String depName = json.getString("trains." + trainKey + ".departure_station.single_name");
            String arrName = json.getString("trains." + trainKey + ".arrival_station.single_name");
            String departureDatetime = json.getString("trains." + trainKey + ".departure_datetime");

            assertNotNull(String.format("Train %s: departure_station.single_name is null", trainKey), depName);
            assertNotNull(String.format("Train %s: arrival_station.single_name is null", trainKey), arrName);
            assertNotNull(String.format("Train %s: departure_datetime is null", trainKey), departureDatetime);

            assertTrue(String.format("Train %s: expected departure station 'Mecca' but was '%s'", trainKey, depName), depName.startsWith("Mecca"));
            assertTrue(String.format("Train %s: expected arrival station 'Medina' but was '%s'", trainKey, arrName), arrName.startsWith("Medina"));

            OffsetDateTime odt = OffsetDateTime.parse(departureDatetime);
            LocalDate actualDate = odt.toLocalDate();
            assertEquals(String.format("Train %s: expected departure date %s but was %s",
                    trainKey, expectedDate, actualDate), expectedDate, actualDate);
        }
    }
}
