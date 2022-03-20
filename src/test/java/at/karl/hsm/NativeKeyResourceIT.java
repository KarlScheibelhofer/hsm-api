package at.karl.hsm;

import io.quarkus.test.junit.NativeImageTest;

/**
 * Integration tests can only test the external API of the application 
 * because these tests run in a separate process. 
 */
@NativeImageTest
public class NativeKeyResourceIT extends KeyResourceTest {

    // Execute the same tests but in native mode.
}