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
import shiip.serialization.BadAttributeException;
import shiip.serialization.Message;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class specifically tests Message class
 *
 * @author Yufan Xu
 */
public class MessageTest {

    @Nested
    @DisplayName("Message Validation")
    public class MessageValidation {

        // invalid message type
        @Test
        @DisplayName("Invalid Type")
        public void invalidEncodedMessageTypeTest() {
            byte[][] messages = {
                    {0x3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                    {0x9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                    {0x7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            };
            for (var message : messages) {
                assertThrows(BadAttributeException.class, () -> Message.decode(message, null));
            }
        }

        // invalid flags
        @Test
        @DisplayName("Invalid Flag")
        public void invalidEncodedMessageFlagTest() {
            byte[][] messages = {
                    {0x0, 0x8, 0x1, 0, 0, 0, 0, 0, 0, 0, 0},
                    {0x0, 0x18, 0x1, 0, 0, 0, 0, 0, 0, 0, 0},
            };
            for (var message : messages) {
                assertThrows(BadAttributeException.class, () -> Message.decode(message, null));
            }
        }

        // test if r bit is ignored
        @Test
        @DisplayName("Test R Bit")
        public void rBitDecodeTest() throws BadAttributeException {
            var encoded = new byte[7];

            int sid = -1;
            var sidBytes = new byte[4];
            for (int i = 0; i < 4; i++) {
                sidBytes[i] = (byte) (sid >>> (i * 8));
            }

            encoded[0] = 0;
            encoded[1] = 1;

            for (int i = 2; i < 6; i++) {
                encoded[i] = sidBytes[i - 2];
            }

            assertDoesNotThrow(() -> Message.decode(encoded, null));
            assertEquals(2147483647, Message.decode(encoded, null).getStreamID());
        }

        // invalid stream id
        @Test
        @DisplayName("Invalid Stream ID")
        public void invalidEncodedMessageSIDTest() {
            byte[][] messages = {
                    {0x0, 0x7, 0x0, 0, 0, 0, 0, 0, 0, 0, 0},
                    {0x4, 0x7, 0x1, 0, 0, 0, 0, 0, 0, 0, 0},
            };
            for (var message : messages) {
                assertThrows(BadAttributeException.class, () -> Message.decode(message, null));
            }
        }

        // tests for valid messages
        @Test
        @DisplayName("Valid Messages")
        public void validEncodedMessageTest() {
            byte[][] messages = {
                    {0x0, 0x1, 0x1, 0, 0, 0, 0, 0, 0, 0, 0},
                    {0x0, 0x2, 0x2, 0, 0, 0, 0, 0, 0, 0, 0},
                    {0x4, 0x8, 0x0, 0, 0, 0, 0, 0, 0, 0, 0},
                    {0x8, 0, 0x1, 0, 0, 0, 0, 2, 0, 0},
                    {0x8, 0, 0, 0, 0, 0, 0, 1, 0, 0},
            };
            for (var message : messages) {
                assertDoesNotThrow(() -> Message.decode(message, null));
            }
        }

        // null message
        @Test
        @DisplayName("Test Null Message")
        public void nullMessageTest() {
            assertThrows(NullPointerException.class, () -> Message.decode(null, null));
        }

        // message with invalid length
        @Test
        @DisplayName("Message with Invalid Length")
        public void invalidLenMessageTest() {
            assertThrows(BadAttributeException.class, () -> Message.decode(new byte[]{0, 1, 1}, null));
        }
    }
}
