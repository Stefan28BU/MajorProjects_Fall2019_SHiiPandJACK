/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/

package jack.serialization.test;

import jack.serialization.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MessageTest {

    @Nested
    @DisplayName("Host and Port")
    public class MessageHostNameAndPort {

        @ParameterizedTest
        @ValueSource(strings = {"", ",,,/", "a-.}"})
        @DisplayName("Host name validation")
        public void hostNameTest(String host) {
            assertThrows(IllegalArgumentException.class, () -> Message.hostNameValidation(host));
        }

        @Test
        @DisplayName("Null host name")
        public void nullHostNameTest() {
            assertThrows(IllegalArgumentException.class, () -> Message.hostNameValidation(null));
        }

        @Test
        @DisplayName("Null payload")
        public void nullPayloadTest() {
            assertThrows(IllegalArgumentException.class, () -> Message.generalPayloadValidation(null));
        }

        @Test
        @DisplayName("Empty payload")
        public void emptyPayloadTest() {
            assertThrows(IllegalArgumentException.class, () -> Message.generalPayloadValidation(""));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 65536})
        @DisplayName("Port validation")
        public void hostNameTest(int port) {
            assertThrows(IllegalArgumentException.class, () -> Message.portValidation(port));
        }
    }

    @Nested
    @DisplayName("Decode")
    public class Decode {
        @ParameterizedTest
        @ValueSource(strings = {"Z wrong", "X wrong", "a wrong", "r wrong"})
        @DisplayName("Unexpected type")
        public void invalidTypeTest(String message) {
            assertThrows(IllegalArgumentException.class, () -> Message.decode(message.getBytes()));
        }

        @Test
        @DisplayName("Null message")
        public void nullMessageTest() {
            assertThrows(IllegalArgumentException.class, () -> Message.decode(null));
        }

        @Test
        @DisplayName("Invalid payload")
        public void invalidPayloadTest() {
            assertThrows(IllegalArgumentException.class, () -> Message.decode("A wrong-12".getBytes()));
        }

        @Test
        @DisplayName("Invalid message")
        public void invalidMessageTest() {
            assertThrows(IllegalArgumentException.class, () -> Message.decode("Awrong:12".getBytes()));
        }

        @ParameterizedTest
        @ValueSource(strings = {"A wrong:125", "R wrong:1 wrong:2 sad:3 ", "N wrong:1", "Q wrong", "E error"})
        @DisplayName("Good messages")
        public void validMessageTest(String message) {
            assertDoesNotThrow(() -> Message.decode(message.getBytes()));
        }
    }
}
