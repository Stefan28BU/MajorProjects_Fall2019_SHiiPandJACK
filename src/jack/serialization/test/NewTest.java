/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/

package jack.serialization.test;

import jack.serialization.New;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static jack.serialization.Message.NEWMESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NewTest {
    @Test
    @DisplayName("ToString")
    public void toStringTest() {
        assertEquals("NEW [google.com:8080]", new New("google.com", 8080).toString());
    }

    @Test
    @DisplayName("HashCode")
    public void hashCodeTest() {
        assertEquals(new New("name", 1).hashCode(), new New("name", 1).hashCode());
    }

    @Nested
    @DisplayName("Constructor")
    public class Constructor {
        @Test
        @DisplayName("Null host")
        public void testNullHost() {
            assertThrows(IllegalArgumentException.class, () -> new New(null, 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 65536})
        @DisplayName("Invalid port")
        public void testInvalidPort(int port) {
            assertThrows(IllegalArgumentException.class, () -> new New("what", port));
        }
    }

    @Nested
    @DisplayName("Setters and Getters")
    public class SettersAndGetters{
        @Test
        @DisplayName("Setter and Getter Port")
        public void portSetterGetterTest() {
            assertEquals(1, new New("name", 1).getPort());
        }

        @Test
        @DisplayName("Setter and Getter Host Name")
        public void hostNameSetterGetterTest() {
            assertEquals("name", new New("name", 1).getHost());
        }

        @Test
        @DisplayName("Get Operation Code")
        public void getOperationTest() {
            assertEquals(NEWMESSAGE, new New("lol", 1).getOperation());
        }
    }

    @Nested
    @DisplayName("Encode and Decode")
    public class EncodeAndDecode {
        @Test
        @DisplayName("Encode")
        public void encodeTest() {
            assertEquals(new String(new New("name", 8080).encode()), "N name:8080");
        }

        @Test
        @DisplayName("Decode")
        public void decodeTest() {
            assertEquals(New.decodeNew("name:8080"), new New("name", 8080));
        }

        @Test
        @DisplayName("Bad Port Number")
        public void badPortTest() {
            assertThrows(IllegalArgumentException.class, ()-> New.decodeNew("name:808w"));
        }
    }
}
