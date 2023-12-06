package org.acme.person.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;

@QuarkusTest
public class PersonResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/person/hello")
          .then()
             .statusCode(200)
             .body(is("hello Quarkus Application"));
    }

    // add more tests
}