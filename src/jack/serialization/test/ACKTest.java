/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/

package jack.serialization.test;

import jack.serialization.ACK;
import jack.serialization.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static jack.serialization.Message.ACKMESSAGE;
import static org.junit.jupiter.api.Assertions.*;

public class ACKTest {
    @Test
    @DisplayName("ToString")
    public void toStringTest() {
        assertEquals("ACK [google.com:8080]", new ACK("google.com", 8080).toString());
    }

    @Test
    @DisplayName("HashCode")
    public void hashCodeTest() {
        assertEquals(new ACK("name", 1).hashCode(), new ACK("name", 1).hashCode());
    }

    @Nested
    @DisplayName("Constructor")
    public class Constructor {
        @Test
        @DisplayName("Null host")
        public void testNullHost() {
            assertThrows(IllegalArgumentException.class, () -> new ACK(null, 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 65536})
        @DisplayName("Invalid port")
        public void testInvalidPort(int port) {
            assertThrows(IllegalArgumentException.class, () -> new ACK("what", port));
        }
    }

    @Nested
    @DisplayName("Setters and Getters")
    public class SettersAndGetters {
        @Test
        @DisplayName("Setter and Getter Port")
        public void portSetterGetterTest() {
            assertEquals(1, new ACK("name", 1).getPort());
        }

        @Test
        @DisplayName("Setter and Getter Host Name")
        public void hostNameSetterGetterTest() {
            assertEquals("name", new ACK("name", 1).getHost());
        }

        @Test
        @DisplayName("Get Operation Code")
        public void getOperationTest() {
            assertEquals(ACKMESSAGE, new ACK("lol", 1).getOperation());
        }
    }

    @Nested
    @DisplayName("Encode and Decode")
    public class EncodeAndDecode {
        @Test
        @DisplayName("Encode")
        public void encodeTest() {
            assertEquals(new String(new ACK("name", 8080).encode()), "A name:8080");
        }

        @Test
        @DisplayName("Decode")
        public void decodeTest() {
            assertEquals(ACK.decodeACK("name:8080"), new ACK("name", 8080));
        }

        @Test
        @DisplayName("Decode invalid")
        public void deocdeInvalid() {
            assertThrows(IllegalArgumentException.class, ()-> Message.decode(new byte[] {65}));
        }

        @Test
        @DisplayName("Bad Port Number")
        public void badPortTest() {
            assertThrows(IllegalArgumentException.class, ()-> ACK.decodeACK("name:808w"));
        }
    }
}
