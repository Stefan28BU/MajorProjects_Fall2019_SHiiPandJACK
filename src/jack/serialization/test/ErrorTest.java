/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/

package jack.serialization.test;

import jack.serialization.Error;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static jack.serialization.Message.ERRORMESSAGE;
import static org.junit.jupiter.api.Assertions.*;

public class ErrorTest {
    @Test
    @DisplayName("ToString")
    public void toStringTest() {
        assertEquals("ERROR error", new Error("error").toString());
    }

    @Test
    @DisplayName("HashCode")
    public void hashCodeTest() {
        assertEquals(new Error("error").hashCode(), new Error("error").hashCode());
    }

    @Nested
    @DisplayName("Constructor")
    public class Constructor {
        @Test
        @DisplayName("Null error message")
        public void testNullErrorMessage() {
            assertThrows(IllegalArgumentException.class, () -> new Error(null));
        }

        @Test
        @DisplayName("Short error message")
        public void testInvalidErrorMessage() {
            assertThrows(IllegalArgumentException.class, () -> new Error(""));
        }
    }

    @Nested
    @DisplayName("Setters and Getters")
    public class SettersAndGetters {
        @Test
        @DisplayName("Setter and Getter Error Message")
        public void errorSetterGetterTest() {
            assertEquals("error", new Error("error").getErrorMessage());
        }

        @Test
        @DisplayName("Get Operation Code")
        public void getOperationTest() {
            assertEquals(ERRORMESSAGE, new Error("error").getOperation());
        }
    }

    @Nested
    @DisplayName("Encode and Decode")
    public class EncodeAndDecode {
        @Test
        @DisplayName("Encode")
        public void encodeTest() {
            assertEquals(new String(new Error("error").encode()), "E error");
        }

        @Test
        @DisplayName("Decode")
        public void decodeTest() {
            assertEquals(Error.decodeError("error"), new Error("error"));
        }
    }
}
