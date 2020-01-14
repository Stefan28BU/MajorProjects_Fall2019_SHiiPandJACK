/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization.test;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import org.junit.jupiter.api.*;
import shiip.serialization.BadAttributeException;
import shiip.serialization.Headers;

import static org.junit.jupiter.api.Assertions.*;

public class HeaderTest {

    @Nested
    @DisplayName("To String")
    public class ToString {
        @Test
        public void toStringTest() throws BadAttributeException {
            var headers = new Headers(5, false);

            headers.addValue("method", "GET");
            headers.addValue("color", "blue");
            assertEquals("Headers: StreamID=5 isEnd=false ([color=blue][method=GET])", headers.toString());
        }
    }

    @Nested
    @DisplayName("Hashcode and Equals")
    public class HashCodeAndEquals {

        // Test Equals 1
        @Test
        @DisplayName("Hashcode 1")
        public void hashcodeTest1() throws BadAttributeException {
            assertEquals(new Headers(1, true).hashCode(), new Headers(1, true).hashCode());
        }

        // Test Equals 2
        @Test
        @DisplayName("Hashcode 2")
        public void hashcodeTest2() throws BadAttributeException {
            assertNotEquals(new Headers(1, false).hashCode(), new Headers(1, true).hashCode());
        }

        // Test hashCode 1
        @Test
        @DisplayName("Equals 1")
        public void equalsTest1() throws BadAttributeException {
            assertTrue(new Headers(1, true).equals(new Headers(1, true)));
        }

        // Test hashCode 2
        @Test
        @DisplayName("Equals 1")
        public void equalsTest2() throws BadAttributeException {
            assertFalse(new Headers(1, false).equals(new Headers(1, true)));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Headers Validation")
    public class HeadersValidation {
        private Headers headers = null;
        private byte[] encodedBytes;
        private Decoder decoder = new Decoder(4096, 4096);
        private Encoder encoder = new Encoder(4096);

        // Initialize test variables
        @BeforeAll
        @DisplayName("Initialize")
        public void initialize() throws BadAttributeException {
            headers = new Headers(1, false);
            headers.addValue("a", "1");
            headers.addValue("b", "2");
            headers.addValue("c", "3");

            encodedBytes = headers.encode(encoder);
        }

        // Reset variable value
        @AfterEach
        @DisplayName("Reset values")
        public void reset() {
            encodedBytes = headers.encode(encoder);
        }

        // Test for null deocder
        @Test
        @DisplayName("Null decoder")
        public void nullDecoderTest() {
            assertThrows(NullPointerException.class, () -> Headers.decode(encodedBytes, null));
        }

        // Test for null decoder
        @Test
        @DisplayName("Not null decoder")
        public void notNullDecoderTest() {
            assertDoesNotThrow(() -> Headers.decode(encodedBytes, decoder));
        }

        // Test for null enocder
        @Test
        @DisplayName("Null encoder")
        public void nullEncoderTest() {
            assertThrows(NullPointerException.class, () -> new Headers(1, false).encode(null));
        }

        // Test for null decoder
        @Test
        @DisplayName("Not null encoder")
        public void notNullEncoderTest() {
            assertDoesNotThrow(() -> new Headers(1, false).encode(encoder));
        }

        // Test for null name
        @Test
        @DisplayName("Null name test")
        public void nullNameTest() {
            assertThrows(BadAttributeException.class, () -> headers.addValue(null, "2"));
        }

        // Test for not null name
        @Test
        @DisplayName("Not null name test")
        public void notNullNameTest() {
            assertDoesNotThrow(() -> headers.addValue("lolk", "as"));
        }

        // Test for null value
        @Test
        @DisplayName("Null value test")
        public void nullValueTest() {
            assertThrows(BadAttributeException.class, () -> headers.addValue("1", null));
        }

        // Test for not null value
        @Test
        @DisplayName("Not null value test")
        public void notNullValueTest() {
            assertDoesNotThrow(() -> headers.addValue("lolk", "s"));
        }

        // Test for invalid stream ID
        @Test
        @DisplayName("Invalid Stream ID")
        public void badSIDTest() {
            for (int i = 0; i < 4; i++) {
                encodedBytes[i + 2] = 0;
            }
            assertThrows(BadAttributeException.class, () -> Headers.decode(encodedBytes, decoder));
        }

        // Test for bad flags
        @Test
        @DisplayName("Bad flags")
        public void badFlagTest() {
            encodedBytes[1] = 0;
            assertThrows(BadAttributeException.class, () -> Headers.decode(encodedBytes, decoder));
        }
    }
}
