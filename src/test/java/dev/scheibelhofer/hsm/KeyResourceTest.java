package dev.scheibelhofer.hsm;


import static io.restassured.RestAssured.given;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(strings = {"EC_P256", "EC_P384", "EC_P521", "RSA_PSS_2048", "EC_ED25519", "EC_ED448"})
    @Order(7)
    public void testGenerate(String keyAlgorithm) {
        Map<String,String> requestBody = new HashMap<>();
        String name = "myGenKey8-" + keyAlgorithm;
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

    @Test
    @Order(8)
    public void testGenerateInvalid() {
        Map<String,String> requestBody = Map.of(
            "name", "myGenKeyInvalidAlg",
            "algorithm", "Ed25519"
        );
        
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
        .when()
            .post("/keys")
        .then()
            .statusCode(400);   
    }    

    @ParameterizedTest
    @ValueSource(strings = {"EC_P256", "EC_P384", "EC_P521", "RSA_PSS_2048", "EC_ED25519", "EC_ED448"})
    @Order(9)
    public void testGenerateAndSign(String keyAlgorithm) throws Exception {
        Map<String,String> generateRequestBody = new HashMap<>();
        String name = "mySignKey9-" + keyAlgorithm;
        generateRequestBody.put("name", name);
        generateRequestBody.put("algorithm", keyAlgorithm);
        Response generateResponse = 
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(generateRequestBody)
            .when()
                .post("/keys")
            .then()
                .statusCode(201)
            .extract()
                .response();

        Integer keyId = generateResponse.path("id");
        byte[] dataToBeSigned = "Even completely meaningless text can be signed!".getBytes(StandardCharsets.UTF_8);
        String encodedPublcKeyB64 = generateResponse.path("encodedPublicKey");
        assertNotNull(encodedPublcKeyB64, "public key is null");
                                
        Response signResponse = 
            given()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(dataToBeSigned)
            .when()
                .post("/keys/{id}/sign", keyId)
            .then()
                .statusCode(200)
            .extract()
                .response();
        
        Integer signKeyId = signResponse.path("keyId");
        assertThat(signKeyId, equalTo(keyId));
        String signatureAlgorithm = signResponse.path("signatureAlgorithm");
        String expectedSignatureAlgorithm = switch (keyAlgorithm) {
            case "EC_P256" -> "SHA256withECDSA";
            case "EC_P384" -> "SHA384withECDSA";
            case "EC_P521" -> "SHA512withECDSA";
            case "RSA_PSS_2048" -> "RSASSA-PSS";
            case "EC_ED25519" -> "EdDSA";
            case "EC_ED448" -> "EdDSA";
            default -> fail("invalid key algorithm: " + keyAlgorithm);
        };
        assertThat(signatureAlgorithm, equalTo(expectedSignatureAlgorithm));

        String signatureValueB64 = signResponse.path("signatureValue");
        assertNotNull(signatureValueB64, "signature value is null");
        byte[] signatureValue = Base64.getMimeDecoder().decode(signatureValueB64);
        
        String keyType = switch (keyAlgorithm) {
            case "EC_P256", "EC_P384", "EC_P521" -> "EC";
            case "RSA_PSS_2048" -> "RSA";
            case "EC_ED25519", "EC_ED448" -> "EdDSA";
            default -> fail("invalid key algorithm: " + keyAlgorithm);
        };

        byte[] encodedPublcKey = Base64.getMimeDecoder().decode(encodedPublcKeyB64);
        KeyFactory keyFactory = KeyFactory.getInstance(keyType);
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedPublcKey));

        java.security.Signature sigService = java.security.Signature.getInstance(signatureAlgorithm);
        sigService.initVerify(publicKey);
        switch (signatureAlgorithm) {
          case "RSASSA-PSS" -> sigService.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
        }    
        sigService.update(dataToBeSigned);

        assertTrue(sigService.verify(signatureValue));        
    }    

}
