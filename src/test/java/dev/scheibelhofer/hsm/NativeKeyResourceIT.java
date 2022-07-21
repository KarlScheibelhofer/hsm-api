package dev.scheibelhofer.hsm;

import io.quarkus.test.junit.QuarkusIntegrationTest;

/**
 * Integration tests can only test the external API of the application 
 * because these tests run in a separate process. 
 */
@QuarkusIntegrationTest
public class NativeKeyResourceIT extends KeyResourceTest {

    // Execute the same tests but in native mode.
}