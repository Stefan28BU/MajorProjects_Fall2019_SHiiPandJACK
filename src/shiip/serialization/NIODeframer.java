/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 6
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static shiip.serialization.Framer.*;

/**
 * Message Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class NIODeframer {

    /**
     * List of frames
     */
    private List<byte[]> frameList;

    /**
     * Remaining length of buffer
     */
    private int lengthRemain;

    /**
     * Total Length read
     */
    private int totalLengthRead = 0;

    /**
     * Buffer for checking the remaining buffer
     */
    private byte[] checkBuffer;

    /**
     * Buffer that keeps the initial buffer
     */
    private byte[] finalBuffer;

    /**
     * Gets all the frames in the buffer
     *
     * @return list of frames
     */
    public List<byte[]> getAllFrames() {
        boolean go = false;

        while (!go) {
            for (var b : checkBuffer) {
                if (b != 0) {
                    go = true;
                    break;
                }
            }
            go = true;
            frameList.add(this.getFrame(checkBuffer));
        }

        return frameList;
    }

    /**
     * constructor for NIODeframer
     */
    public NIODeframer() {
        frameList = new ArrayList<>();
    }

    /**
     * constructor for NIODeframer
     */
    public NIODeframer(byte[] buffer) {
        frameList = new ArrayList<>();

        checkBuffer = new byte[buffer.length];
        finalBuffer = new byte[buffer.length];

        for (int i = 0; i < checkBuffer.length; i++) {
            checkBuffer[i] = buffer[i];
            finalBuffer[i] = buffer[i];
        }
    }

    /**
     * Gets the next frame
     *
     * @param buffer the bytes of next frame
     * @throws NullPointerException     if null buffer detected
     * @throws IllegalArgumentException if argument is illegal
     * @returns byte[] of headers + payload
     */
    public byte[] getFrame(byte[] buffer) throws NullPointerException, IllegalArgumentException {

        // Check for null buffer
        Objects.requireNonNull(buffer);

        byte[] prefixLengthBytes = new byte[PREFIXLENGTHBYTELENGTH];

        // Get the prefix length bytes if there is more than three bytes in the stream
        if (buffer.length >= PREFIXLENGTHBYTELENGTH) {
            System.arraycopy(buffer, 0, prefixLengthBytes, 0, prefixLengthBytes.length);
        } else {
            throw new IllegalArgumentException("Short prefix bytes");
        }

        // Gets the rest of the frame size
        int length = Deframer.getNextMessageLength(prefixLengthBytes);

        byte[] message = new byte[length];

        // Sets the total read length
        totalLengthRead += PREFIXLENGTHBYTELENGTH + message.length;

        // Sets the remaining length
        lengthRemain = checkBuffer.length - totalLengthRead;

        // Check if stream has enough bytes to write
        if (finalBuffer.length >= totalLengthRead) {
            for (int i = 0; i < message.length; i++) {
                message[i] = buffer[i + PREFIXLENGTHBYTELENGTH];
            }
        } else {
            return null;
        }

        byte[] temp = new byte[lengthRemain];

        for (int i = 0; i < temp.length; i++) {
            temp[i] = finalBuffer[i + totalLengthRead];
        }

        for (int i = 0; i < temp.length; i++) {
            checkBuffer[i] = temp[i];
        }

        return message;
    }
}
