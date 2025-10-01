package helpers;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ApiHelper {

    private static final String API_KEY = "INSERT_API_KEY";

    public static Response postTimetableSearch(String body) {
        return given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-currency", "USD")
                .header("X-API-User-Key", API_KEY)
                .body(body)
                .when()
                .post("https://back.rail.ninja/api/v2/timetable")
                .andReturn();
    }

    public static Response getSearchHistory(String searchHistoryCookie) {
        return given()
                .header("Accept", "application/json")
                .cookie("search_history", searchHistoryCookie)
                .when()
                .get("https://back.rail.ninja/api/v1/station/history")
                .andReturn();
    }
}
