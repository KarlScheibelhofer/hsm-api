package at.karl.hsm;


import static io.restassured.RestAssured.given;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

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
public class KeyResourceTest {

    @Test
    @Order(1)
    public void testList() {
        given()
        .when().get("/keys")
        .then()
        .statusCode(200)
        .body("$.size()", is(0));
    }
    
    @Test
    @Order(2)
    public void testGetByNoExistingId() {
        long id = 999999;
        given()
        .when().get("/keys/{id}", id)
        .then()
        .statusCode(404);
    }
    
    @Test
    @Order(3)
    public void testAddDelete() {
        Map<String,String> requestBody = new HashMap<>();
        String name = "testkey1";
        requestBody.put("name", name);
        requestBody.put("algorithm", "EC_P256");
        requestBody.put("createdAt", LocalDateTime.parse("2019-10-17T11:23:47.123").toString());
    	int id = 
	        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
	        .when()
	            .post("/keys")
	        .then()
	            .statusCode(201)
	            .body("name", is(name))
	            .body("$", not(hasKey("encodedKey")))
	        .extract()
            	.path("id");

    	given()
	        .when()
	            .delete("/keys/{id}", id)
	        .then()
	            .statusCode(204);     
    }    

    @Test
    @Order(4)
    public void testGetById() {
        Map<String,String> requestBody = new HashMap<>();
        String name = "testkey2";
        String algorithm = "EC_P384";
        String createdAt = LocalDateTime.parse("2019-10-17T11:23:47.123").toString();
        requestBody.put("name", name);
        requestBody.put("algorithm", algorithm);
        requestBody.put("createdAt", createdAt);
    	int id = 
	        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
	        .when()
	            .post("/keys")
	        .then()
	            .statusCode(201)
	            .body("name", is(name))
	            .body("$", not(hasKey("encodedKey")))
	        .extract()
            	.path("id");

    	given()
	        .when()
	            .get("/keys/{id}", id)
	        .then()
                .statusCode(200)
	            .body("name", is(name))
	            .body("algorithm", is(algorithm))
	            .body("createdAt", is(createdAt))
	            .body("$", not(hasKey("encodedKey")))
                ;     

    }

    @Test
    @Order(5)
    public void testDeleteNonExistent() {
    	int id = 470815;
    	given()
	        .when()
	            .delete("/keys/{id}", id)
	        .then()
	            .statusCode(404);     
    } 

    @Test
    @Order(6)
    public void testAddCreatedAt() {
        Map<String,String> requestBody = new HashMap<>();
        String name = "mykey7";
        requestBody.put("name", name);
        Response response = 
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
            .when()
                .post("/keys")
            .then()
                .statusCode(201)
            .extract()
                .response();

        String createdAtStr = response.path("createdAt");
        LocalDateTime createdAt = LocalDateTime.parse(createdAtStr);
        assertThat(createdAt, within(2, ChronoUnit.SECONDS, LocalDateTime.now()));

        int id = response.path("id");
        given()
            .when()
                .delete("/keys/{id}", id)
            .then()
                .statusCode(204);     
    }    

    @Test
    @Order(7)
    public void testGenerate() {
        for (String keyAlgorithm : Arrays.asList(null, "EC_P256", "RSA_PSS_2048")) {
            Map<String,String> requestBody = new HashMap<>();
            String name = "myGenKey8";
            requestBody.put("name", name);
            requestBody.put("algorithm", keyAlgorithm);
            Response response = 
                given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                .when()
                    .post("/keys")
                .then()
                    .statusCode(201)
                .extract()
                    .response();
    
            String createdAtStr = response.path("createdAt");
            LocalDateTime createdAt = LocalDateTime.parse(createdAtStr);
            assertThat(createdAt, within(2, ChronoUnit.SECONDS, LocalDateTime.now()));
            
            String createdKeyAlgStr = response.path("algorithm");
            String expectedKeyAlgorithm = (keyAlgorithm != null) ? keyAlgorithm : "EC_P256";
            assertThat(createdKeyAlgStr, equalTo(expectedKeyAlgorithm));

            int id = response.path("id");
            given()
                .when()
                    .delete("/keys/{id}", id)
                .then()
                    .statusCode(204);     
        }
    }    

    @Test
    @Order(8)
    public void testGenerateInvalid() {
        Map<String,String> requestBody = Map.of(
            "name", "myGenKeyInvalidAlg",
            "algorithm", "ED25519"
        );
        
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
        .when()
            .post("/keys")
        .then()
            .statusCode(400);   
    }    

}
