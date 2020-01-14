/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization.test;

import org.junit.jupiter.api.DisplayName;
import shiip.serialization.BadAttributeException;
import shiip.serialization.Message;
import shiip.serialization.Settings;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsTest {

    @Nested
    @DisplayName("Settings Validation")
    public class SettingsMessageValidation {

        // see if sid is valid
        @Test
        @DisplayName("Stream ID Validation")
        void settingsSIDTest() throws BadAttributeException {
            assertEquals(new Settings().getStreamID(), 0x0);
        }

        // see if length of message is valid
        @Test
        @DisplayName("Message Length Validation")
        void settingsLengthTest() throws BadAttributeException {
            var settings = new Settings();
            var sBytes = settings.encode(null);

            assertEquals(sBytes.length, 6);
        }
    }

    @Nested
    @DisplayName("Tests ToString")
    public class SettingsToString {

        // toString validation
        @Test
        @DisplayName("ToString Matching")
        public void settingsToStringIsSameTest() throws BadAttributeException {
            var settings = new Settings();
            settings.setStreamID(0);

            assertEquals("Settings: StreamID=0", settings.toString());
        }
    }

    @Nested
    @DisplayName("Tests ToString")
    public class SettingsValidation {
        @Test
        @DisplayName("Valid Settings")
        void settingsSIDTest() {
            assertThrows(BadAttributeException.class, () -> new Settings().setStreamID(1));
        }


        // valid settings bytes
        @Test
        @DisplayName("Valid Settings")
        void validSettingsTest() {
            assertDoesNotThrow(() -> Message.decode(new byte[]{0x4, 0x1, 0, 0, 0, 0}, null));
        }

        // invalid settings bytes
        @Test
        @DisplayName("Invalid Settings")
        void invalidSettingsTest() {
            assertThrows(BadAttributeException.class, () -> Message.decode(new byte[]{0x4, 0x1, 0, 0, 0, 1}, null));
        }

        // settings with valid length
        @Test
        @DisplayName("Valid Length Settings")
        void validSettingsLengthTest() throws BadAttributeException {
            var decoded = Message.decode(new byte[]{0x4, 0x1, 0, 0, 0, 0, 0, 0, 1}, null);


            assertEquals(6, decoded.encode(null).length);
        }

        // settings matching
        @Test
        @DisplayName("Settings Message Matching")
        void settingsMessageTest() throws BadAttributeException {
            var settings = new Settings();

            var encoded = settings.encode(null);

            var decoded = Message.decode(encoded, null);

            assertEquals(decoded.getStreamID(), 0);
            assertEquals(decoded.getCode(), 0x4);
            assertEquals(encoded[1], 0x1);
            assertEquals(encoded[0], 0x4);
        }
    }

    @Nested
    @DisplayName("Hashcode and Equals")
    public class HashCodeAndEquals {

        // Test Equals
        @Test
        @DisplayName("Hashcode")
        public void hashcodeTest() throws BadAttributeException {
            assertEquals(new Settings().hashCode(), new Settings().hashCode());
        }

        // Test hashCode
        @Test
        @DisplayName("Equals")
        public void equalsTest() throws BadAttributeException {
            assertTrue(new Settings().equals(new Settings()));
        }
    }
}
