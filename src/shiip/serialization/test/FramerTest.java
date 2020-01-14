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
import shiip.serialization.Framer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static shiip.serialization.Framer.HEADERLENGTH;
import static shiip.serialization.Framer.MAXPAYLOADLENGTH;

/**
 * Framer Test Class
 *
 * @author Yufan Xu
 * @version 1.1
 */
class FramerTest {

    /**
     * This class specifically tests the constructor of Framer
     *
     * @author Yufan Xu
     */
    @Nested
    @DisplayName("Framer Constructor")
    public class FramerConstructor {

        // Tests if make a Framer with null argument would throw exception
        @Test
        @DisplayName("Null OutputStream Test")
        void nullOutputStreamExceptionTest() {
            assertThrows(NullPointerException.class, () -> new Framer(null));
        }

        // Tests if valid message works
        @Test
        @DisplayName("Not Null OutputStream Test")
        void notNullOutputStreamExceptionTest() {
            assertDoesNotThrow(() -> {
                new Framer(new ByteArrayOutputStream());
            });
        }
    }

    /**
     * This class specifically tests the putFrame() function of Framer
     *
     * @author Yufan Xu
     */
    @Nested
    @DisplayName("PutFrame Function")
    public class PutFrameFunction {

        // Tests if putFrame() function throws IllegalArgumentException correctly
        @ParameterizedTest
        @ValueSource(ints = {MAXPAYLOADLENGTH + HEADERLENGTH + 1, HEADERLENGTH - 1, 0, 200099202})
        @DisplayName("PutFrame IllegalArgumentException Test")
        void putFrameIllegalArgumentExceptionTest(int length) {
            assertThrows(IllegalArgumentException.class, () ->
                    new Framer(new ByteArrayOutputStream()).putFrame(
                            new byte[length])
            );
        }

        // Tests if putFrame() function is good at edge case
        @ParameterizedTest
        @ValueSource(ints = {MAXPAYLOADLENGTH + HEADERLENGTH, HEADERLENGTH})
        @DisplayName("PutFrame Valid Argument Edge Case Test")
        void putFrameValidArgumentEdgeCaseTest(int length) {
            assertDoesNotThrow(() ->
                    new Framer(new ByteArrayOutputStream()).putFrame(
                            new byte[length])
            );
        }

        // Tests if putFrame() function handles valid data correctly
        @ParameterizedTest(name = "{index} => message = ''{0}''")
        @ValueSource(strings = {"------", "1321231231231231231231", "\n\n\n\n\0\0", "jesus christ"})
        @DisplayName("PutFrame Valid Argument Test")
        void putFrameValidArgumentTest(String message) {
            assertDoesNotThrow(() ->
                    new Framer(new ByteArrayOutputStream()).putFrame(message.getBytes())
            );
        }

        // Tests if putFrame() function throws NullPointerException correctly
        @Test
        @DisplayName("PutFrame NullPointerException Test")
        void putFrameNullPointerExceptionTest() {
            assertThrows(NullPointerException.class, () ->
                    new Framer(new ByteArrayOutputStream()).putFrame(null)
            );
        }

        // Tests if putFrame() function encodes message correctly
        @Test
        @DisplayName("PutFrame Message EncodingTest")
        void putFrameMessageEncodingTest() throws IOException {
            var out = new ByteArrayOutputStream();
            new Framer(out).putFrame(new byte[]{9, 8, 7, 6, 5, 4, 2});
            assertArrayEquals(new byte[]{0, 0, 1, 9, 8, 7, 6, 5, 4, 2}, out.toByteArray());
        }
    }
}