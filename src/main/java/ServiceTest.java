import io.restassured.response.Response;
import static io.restassured.RestAssured.*;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static io.restassured.path.json.JsonPath.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ServiceTest {
    static int totalPages;
    static int totalUsers;

    //checking that overall amount of users matches claimed number
    static void totalUsersCheck() {
        int sum = 0;
        for (int page = 1; page <= totalPages; page++) {
            Object obj = get(Integer.toString(page)).
                    then().
                    contentType(JSON).extract().
                    path("data.size()");
            sum += (Integer)obj;
        }
        System.out.print("total user check: ");
        if (sum == totalUsers)
            System.out.println("PASSED");
        else
            System.out.println("FAILED");
    }

    //asserting that data fields are all present at "https://reqres.in/api/users?page=1"
    static void dataValidation() {
        System.out.print("data fields validation: ");
        String json = get("1").asString();
        try {
            assertThat(json, matchesJsonSchemaInClasspath("schema.json"));
            System.out.println("PASSED");
        } catch (AssertionError e){
            System.out.println("FAILED - doesn't match the schema " + e.getMessage());
        }
    }

    // validating that all structure fields match its contents
    static void fieldConentMatching() {
        int per_page = (Integer) get("1").then().contentType(JSON).extract().path("per_page");
        Object id, email, first_name, last_name, avatar;
        for (int i = 0; i < per_page; i++) {
            Response actual_user_response = get("https://reqres.in/api/users/"+Integer.toString(i+1)).then().extract().response();
            id = actual_user_response.path("data.id");
            email = actual_user_response.path("data.email");
            first_name = actual_user_response.path("data.first_name");
            last_name = actual_user_response.path("data.last_name");
            avatar = actual_user_response.path("data.avatar");
            Response user_to_match_response = get("1").then().contentType(JSON).extract().response();
            System.out.print("user " + Integer.toString(i+1) +  " field validation: ");
            try {
                user_to_match_response.then().body("data[" + Integer.toString(i) + "].id", equalTo(id));
                user_to_match_response.then().body("data[" + Integer.toString(i) + "].email", equalTo(email));
                user_to_match_response.then().body("data[" + Integer.toString(i) + "].first_name", equalTo(first_name));
                user_to_match_response.then().body("data[" + Integer.toString(i) + "].last_name", equalTo(last_name));
                user_to_match_response.then().body("data[" + Integer.toString(i) + "].avatar", equalTo(avatar));
                System.out.println("PASSED");
            } catch (AssertionError e) {
                System.out.println("FAILED - One of the fields doesn't match" + e.getMessage());
            }
        }
    }

    public static void main (String args[]) {
        baseURI = "https://reqres.in";
        basePath = "/api/users?page=";
        String json = get("1").asString();
        totalPages = from(json).getInt("total_pages");
        totalUsers = from(json).getInt("total");
        totalUsersCheck();
        dataValidation();
        fieldConentMatching();
        System.out.println("all tests finished working...");
    }
}
