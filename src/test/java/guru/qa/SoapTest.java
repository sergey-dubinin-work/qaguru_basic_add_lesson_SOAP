package guru.qa;

import io.restassured.RestAssured;
import io.restassured.internal.util.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static io.restassured.RestAssured.given;

public class SoapTest {

    @Test
    void getCountriesTest() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("getCountryRequest.xml");

        final String requestBody = new String(IOUtils.toByteArray(is));

        RestAssured.baseURI = "http://localhost:8080/ws";

        given()
                .contentType("text/xml")
                .when()
                .body(requestBody)
                .log().all()
                .post("/getCountry")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK);

    }

}
