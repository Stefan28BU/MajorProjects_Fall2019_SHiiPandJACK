/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization;

import java.io.*;
import java.util.Objects;

/**
 * Framer Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class Framer {
    /**
     * Length of prefix length
     */
    public static final int PREFIXLENGTHBYTELENGTH = 3;

    /**
     * Maximum length of payload
     */
    public static final int MAXPAYLOADLENGTH = 16384;

    /**
     * Length of header
     */
    public static final int HEADERLENGTH = 6;

    /**
     * This mask zeros out the most significant bit in a byte
     */
    private static final int BYTEMASK = 0xff;

    /**
     * This shifts a byte by 8 bits
     */
    private static final int BYTESHIFT = 8;

    /**
     * OutputStream to write
     */
    private OutputStream out;

    /**
     * OutputStream to write
     */
    private ByteArrayOutputStream bOut = new ByteArrayOutputStream();

    /**
     * Constructor of Framer
     *
     * @param out first integer to add
     * @throws NullPointerException if out being passed is null
     */
    public Framer(OutputStream out) throws NullPointerException {
        Objects.requireNonNull(out, "Deserialization Error: message cannot be null");

        this.out = out;
    }

    /**
     * Encode the prefix length and write all of them to output stream
     *
     * @param message first integer to add
     * @throws NullPointerException if message being passed is null
     * @throws IOException          if message length is greater than the max length
     */
    public void putFrame(byte[] message) throws IOException {
        frameBytesValidation(message);
        this.frame(message);
    }

    /**
     * Writes frammed bytes to OutputStream
     *
     * @param message frammed bytes
     * @throws IOException if IOException occurs
     */
    private void frame(byte[] message) throws IOException {
        int payloadLength = message.length - HEADERLENGTH;

        // Shift twice then mask to write highest byte
        out.write((payloadLength >> BYTESHIFT >> BYTESHIFT) & BYTEMASK);
        bOut.write((payloadLength >> BYTESHIFT >> BYTESHIFT) & BYTEMASK);

        // Shift once then mask to write second highest byte
        out.write((payloadLength >> BYTESHIFT) & BYTEMASK);
        bOut.write((payloadLength >> BYTESHIFT) & BYTEMASK);

        // Mask then write lowest byte
        out.write(payloadLength & BYTEMASK);
        bOut.write(payloadLength & BYTEMASK);

        // Write the actual message (payload + header) to the OutputStream
        out.write(message);
        bOut.write(message);

        out.flush();
        bOut.flush();
    }

    /**
     * Validation of message bytes
     *
     * @param message frammed bytes
     */
    private static void frameBytesValidation(byte[] message) {
        // Message validations
        Objects.requireNonNull(message, "Message cannot be null");

        // If length of message is not valid, throw exception
        if (message.length > MAXPAYLOADLENGTH + HEADERLENGTH || message.length < HEADERLENGTH) {
            throw new IllegalArgumentException("Message is not legal\n" +
                    "It might be:\n" +
                    "1) len(message(payload+header)) < 6 bytes\n" +
                    "2) len(message(payload+header)) > (6 + 16384) bytes\n");
        }
    }
}