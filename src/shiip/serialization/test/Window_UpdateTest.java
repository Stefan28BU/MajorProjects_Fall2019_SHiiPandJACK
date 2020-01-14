/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import shiip.serialization.BadAttributeException;
import shiip.serialization.Message;
import shiip.serialization.Window_Update;

import static org.junit.jupiter.api.Assertions.*;

public class Window_UpdateTest {

    @Nested
    @DisplayName("Tests Constructor")
    public class WUConstructor {

        // invalid sid
        @ParameterizedTest
        @ValueSource(ints = {-1, -22, -2147483648})
        @DisplayName("Invalid Stream ID")
        public void windowUpdateInvalidSIDTest(int sid) {
            assertThrows(BadAttributeException.class, () -> new Window_Update(sid, 1));
        }

        // valid sid
        @ParameterizedTest
        @ValueSource(ints = {0, 2147483647, 1})
        @DisplayName("Valid Stream ID")
        public void windowUpdateValidSIDTest(int sid) {
            assertDoesNotThrow(() -> new Window_Update(sid, 1));
        }

        // invalid increment value
        @ParameterizedTest
        @ValueSource(ints = {0, -1, -2147483648})
        @DisplayName("Invalid Size Increment")
        public void windowUpdateInvalidIncTest(int inc) {
            assertThrows(BadAttributeException.class, () -> new Window_Update(1, inc));
        }

        // valid increment value
        @ParameterizedTest
        @ValueSource(ints = {2, 2147483647, 1})
        @DisplayName("Valid Size Increment")
        public void windowUpdateValidIncTest(int inc) {
            assertDoesNotThrow(() -> new Window_Update(1, inc));
        }

        // message length for window update
        @Test
        @DisplayName("Message Length")
        public void windowUpdateLengthTest() throws BadAttributeException {
            var wu = new Window_Update(1, 1);
            var wuBytes = wu.encode(null);

            assertEquals(wuBytes.length, 10);
        }

        @Test
        @DisplayName("Message Length")
        public void testWUGetCode() throws BadAttributeException {
            var wu = new Window_Update(1, 1);

            assertEquals(wu.getCode(), 8);
        }
    }

    @Nested
    @DisplayName("Setters and Getters")
    public class SetGetIncrement {

        // getter and setter matching
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 999})
        @DisplayName("Size Increment Matching")
        public void setGetIncrementValueTest(int inc) throws BadAttributeException {
            var window_update = new Window_Update(1, 1);

            window_update.setIncrement(inc);

            assertEquals(window_update.getIncrement(), inc);
        }
    }

    @Nested
    @DisplayName("Tests ToString")
    public class WindowUpdateToString {

        // tostring validation
        @Test
        @DisplayName("ToString Matching")
        public void settingsToStringIsSameTest() throws BadAttributeException {
            var window_update = new Window_Update(1, 1);

            assertEquals("Window_Update: StreamID=1 increment=1", window_update.toString());
        }
    }

    @Nested
    @DisplayName("Window Update Validation")
    public class WindowUpdateValidation {

        // r bit in payload validation
        @Test
        @DisplayName("R Bit")
        public void rBitDecodeTest() throws BadAttributeException {
            var encoded = new byte[10];

            int sid = -1;
            var sidBytes = new byte[4];
            for (int i = 0; i < 4; i++) {
                sidBytes[i] = (byte) (sid >>> (i * 8));
            }

            encoded[0] = 0x8;
            encoded[1] = 1;

            for (int i = 2; i < 6; i++) {
                encoded[i] = sidBytes[i - 2];
            }

            int inc = -1;
            var incBytes = new byte[4];
            for (int i = 0; i < 4; i++) {
                incBytes[i] = (byte) (inc >>> (i * 8));
            }

            for (int i = 6; i < 10; i++) {
                encoded[i] = incBytes[i - 6];
            }

            assertDoesNotThrow(() -> Message.decode(encoded, null));
            assertEquals(2147483647, Message.decode(encoded, null).getStreamID());
            assertEquals(2147483647, Window_Update.decodeWindowUpdate(encoded).getIncrement());
        }

        // valid wu message
        @Test
        @DisplayName("Valid Window Update")
        public void validWUTest() {
            assertDoesNotThrow(() -> Message.decode(new byte[]{8, 0, 0, 0, 0, 0, 0, 0, 0, 1}, null));
        }

        // bad increment value deocode
        @Test
        @DisplayName("Bad Increment Value")
        public void badIncTest() {
            assertThrows(BadAttributeException.class, () -> Message.decode(new byte[]{8, 0, 0, 0, 0, 0, 0, 0, 0, 0}, null));
        }

        // bad length decode
        @Test
        @DisplayName("Bad Length")
        public void badLengthTest() {

            assertThrows(BadAttributeException.class, () -> Window_Update.decode(new byte[]{8, 0, 0, 0, 0, 4, 0, 0, 1}, null));
        }
    }

    @Nested
    @DisplayName("Hashcode and Equals")
    public class HashCodeAndEquals {

        // Test Equals
        @Test
        @DisplayName("Hashcode")
        public void hashcodeTest() throws BadAttributeException {
            assertEquals(new Window_Update(1,1).hashCode(), new Window_Update(1,1).hashCode());
        }

        // Test hashCode
        @Test
        @DisplayName("Equals")
        public void equalsTest() throws BadAttributeException {
            assertTrue(new Window_Update(1,1).equals(new Window_Update(1,1)));
        }
    }
}
