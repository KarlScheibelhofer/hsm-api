package dev.scheibelhofer.hsm;


import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class) 
@TestMethodOrder(OrderAnnotation.class)
public class InfoResourceTest {

    @Test
    @Order(1)
    public void testGetSupportedAlgorithms() {
        Response response = given()
            .when().get("/info/key-algorithms")
        .then()
            .statusCode(200)
        .extract()
            .response();

        String algorithmList = response.getBody().asString();
        assertThat(algorithmList, equalTo("EC_ED25519 EC_ED448 EC_P256 EC_P384 EC_P521 RSA_PSS_2048"));  
    }
    
}
