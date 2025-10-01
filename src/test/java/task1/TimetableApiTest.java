package task1;

import helpers.ApiHelper;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Epic("Timetable API Tests")
@Feature("API Endpoint Verification")
public class TimetableApiTest {

    @Test
    @Description("Check that the /timetable endpoint returns trains with correct stations and dates")
    @Severity(SeverityLevel.CRITICAL)
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

        Assertions.assertEquals(200, resp.getStatusCode(), "Unexpected status code");

        String ct = resp.getContentType();
        Assertions.assertNotNull(ct, "Content-Type header is missing");
        Assertions.assertTrue(ct.toLowerCase().startsWith("application/json"), "Content-Type should start with 'application/json' but was: " + ct);

        JsonPath json = resp.jsonPath();

        String topDep = json.getString("departure_station.single_name");
        String topArr = json.getString("arrival_station.single_name");
        Assertions.assertEquals("Mecca", topDep, "Top-level departure_station.single_name mismatch");
        Assertions.assertEquals("Medina", topArr, "Top-level arrival_station.single_name mismatch");

        LocalDate expectedDate = LocalDate.parse("05.11.2025", DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        Map<String, ?> trains = json.getMap("trains");
        Assertions.assertNotNull(trains, "Response must contain 'trains' object");
        Assertions.assertFalse(trains.isEmpty(), "Trains map must not be empty");

        for (String trainKey : trains.keySet()) {
            String depName = json.getString("trains." + trainKey + ".departure_station.single_name");
            String arrName = json.getString("trains." + trainKey + ".arrival_station.single_name");
            String departureDatetime = json.getString("trains." + trainKey + ".departure_datetime");

            Assertions.assertNotNull(depName, String.format("Train %s: departure_station.single_name is null", trainKey));
            Assertions.assertNotNull(arrName, String.format("Train %s: arrival_station.single_name is null", trainKey));
            Assertions.assertNotNull(departureDatetime, String.format("Train %s: departure_datetime is null", trainKey));

            Assertions.assertTrue(depName.startsWith("Mecca"), String.format("Train %s: expected departure station 'Mecca' but was '%s'", trainKey, depName));
            Assertions.assertTrue(arrName.startsWith("Medina"), String.format("Train %s: expected arrival station 'Medina' but was '%s'", trainKey, arrName));

            OffsetDateTime odt = OffsetDateTime.parse(departureDatetime);
            LocalDate actualDate = odt.toLocalDate();
            Assertions.assertEquals(expectedDate, actualDate, String.format("Train %s: expected departure date %s but was %s",
                    trainKey, expectedDate, actualDate));
        }
    }
}
