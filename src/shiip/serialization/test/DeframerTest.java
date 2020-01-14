/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization.test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import shiip.serialization.Deframer;
import shiip.serialization.Framer;

import java.io.*;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deframer Test Class
 *
 * @author Yufan Xu
 * @version 1.1
 */
class DeframerTest {

    /**
     * This class specifically tests the constructor of Deframer
     *
     * @author Yufan Xu
     */
    @Nested
    @DisplayName("Deframer Constructor")
    public class DeframerConstructor {

        // Tests if make a Deframer with null argument would throw exception
        @Test
        @DisplayName("Null InputStream Test")
        void nullInputStreamExceptionTest() {
            assertThrows(NullPointerException.class, () -> new Deframer(null));
        }

        // Series of tests to see if valid message works
        @ParameterizedTest(name = "{index} => message = ''{0}''")
        @ValueSource(strings = {"", "1321231231231231231231", "\nddd"})
        @DisplayName("Not Null InputStream Test")
        void notNullInputStreamExceptionTest(String message) {
            assertDoesNotThrow(() -> {
                new Deframer(new ByteArrayInputStream(message.getBytes()));
            });
        }
    }

    /**
     * This class specifically tests the getFrame() function of Framer
     *
     * @author Yufan Xu
     */
    @Nested
    @DisplayName("Function GetFrame")
    public class GetFrameFunction {

        // Tests if getFrame() function throws EOFException correctly
        @Test
        @DisplayName("GetFrame EOFException 1")
        void getFrameEOFExceptionTest1() {
            byte[][] twoDBytes = {{0}, {0, 0, 1}, {0, 0, 1, 0, 0, 0}, {0, 0, 1, 0, 0, 0, 0, 0, 0}};

            for (byte[] bytes : twoDBytes) {
                assertThrows(EOFException.class, () -> new Deframer(
                        new ByteArrayInputStream(bytes)).getFrame());
            }
        }

        // Tests if getFrame() function throws IOException correctly
        @Test
        @DisplayName("GetFrame IOException 1")
        void getFrameIOExceptionTest1() {
            byte[][] twoDBytes = {{0, 0}, {0, 0, 1}, {0, 0, 1, 0}, {0, 0, 1, 0, 1, 1, 0, 0, 0}};

            for (byte[] bytes : twoDBytes) {
                assertThrows(IOException.class, () -> new Deframer(
                        new ByteArrayInputStream(bytes)).getFrame());
            }

        }

        // Tests if getFrame() function throws IllegalArgumentException correctly
        @Test
        @DisplayName("GetFrame IllegalArgumentException")
        void getFrameIllegalArgumentExceptionTest() {
            assertThrows(IllegalArgumentException.class, () -> new Deframer(
                    new ByteArrayInputStream(new byte[]{1, 1, 1})).getFrame());
        }

        // Tests if getFrame() works
        @Test
        @DisplayName("GetFrame Message DecodingTest")
        void getFrameMessageDecodingTest() throws IOException {
            var in = new ByteArrayInputStream(new byte[]{0, 0, 1, 9, 8, 7, 6, 5, 4, 2});

            assertArrayEquals(new byte[]{9, 8, 7, 6, 5, 4, 2}, new Deframer(in).getFrame());
        }
    }

    /**
     * This class specifically tests frammed and deframmed message
     *
     * @author Yufan Xu
     */
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Test Factory")
    public static class FramerDeframerTestFactory {
        private byte[] messageIn;
        private byte[] messageOut;
        private int frammedLen;
        private int deframmedLen;
        private int originLen;
        private byte[] frammedMessage;

        // Initialization for testing
        @BeforeAll
        @DisplayName("Initialization")
        public void initialize() throws IOException {
            var message = "------this is a message";
            originLen = message.length();

            messageIn = message.getBytes();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Framer framer = new Framer(out);
            framer.putFrame(messageIn);
            frammedLen = out.size();

            frammedMessage = out.toByteArray();
            InputStream in = new ByteArrayInputStream(frammedMessage);
            Deframer deframer = new Deframer(in);
            messageOut = deframer.getFrame();

            deframmedLen = messageOut.length;
        }

        // See if the frammed and deframmed message match
        @Test
        @DisplayName("Before and After Message Match")
        public void testMessageMatch() {
            assertArrayEquals(messageIn, messageOut);
        }

        // See if frammed message has correct length
        @Test
        @DisplayName("Before and After Message Length Match")
        public void testFrammedLength() {
            assertEquals(deframmedLen + 3, frammedLen);
        }

        // See if prefix length match
        @Test
        @DisplayName("Encoded Payload Length Match")
        public void testEncodedLength() {
            var prefixLen = new byte[3];

            for (int i = 0; i < 3; i++) {
                prefixLen[i] = frammedMessage[i];
            }

            byte[] fourByteLength = new byte[4];

            // Puts the 3-byte payload length bytes into a 4-byte array, then convert to integer
            for (int i = 0; i < prefixLen.length; i++) {
                fourByteLength[i + 1] = prefixLen[i];
            }
            ByteBuffer wrapper = ByteBuffer.wrap(fourByteLength);

            int payloadLength = wrapper.getInt();

            assertEquals(originLen - 6, payloadLength);
        }
    }
}