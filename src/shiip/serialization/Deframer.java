/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Objects;

import static shiip.serialization.Framer.*;

/**
 * Deframer Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class Deframer {

    /**
     * Inputstream for read
     */
    private DataInputStream in;

    /**
     * Constructor of Deframer
     *
     * @param in input stream
     * @throws NullPointerException if in being passed is null
     */
    public Deframer(InputStream in) throws NullPointerException {
        Objects.requireNonNull(in, "InputStream is null");

        this.in = new DataInputStream(in);
    }

    /**
     * Gets the next frame
     *
     * @throws IOException              if IO problem occurs
     * @throws EOFException             if nothing is in the file
     * @throws IllegalArgumentException if argument is illegal
     * @returns byte[] of headers + payload
     */
    public byte[] getFrame() throws IOException, EOFException, IllegalArgumentException {
        byte[] lenBytes = new byte[PREFIXLENGTHBYTELENGTH];

        // Read three bytes for prefix length
        in.readFully(lenBytes);

        byte[] message = new byte[getNextMessageLength(lenBytes)];

        // Reads the rest of the message
        in.readFully(message);

        return message;
    }

    /**
     * This functions gets the frame size based on prefix length
     *
     * @param lenBytes three byte length field
     * @return the length of rest of the frame
     */
    public static int getNextMessageLength(byte[] lenBytes) {
        byte[] fourByteLength = new byte[4];

        // Puts the 3-byte payload length bytes into a 4-byte array, then convert to integer
        System.arraycopy(lenBytes, 0, fourByteLength, 1, lenBytes.length);
        ByteBuffer wrapper = ByteBuffer.wrap(fourByteLength);

        int payloadLength = wrapper.getInt();

        // If length of payload is not valid, throw exception
        if (payloadLength > MAXPAYLOADLENGTH || payloadLength < 0) {
            throw new IllegalArgumentException("InputStream is not legal\n" +
                    "Prefix length: " + payloadLength + "\n");
        }

        return payloadLength + HEADERLENGTH;
    }
}
