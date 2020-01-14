/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/

package jack.serialization;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Message Class
 *
 * @author Yufan Xu
 * @version 1.1
 */
public abstract class Message {

    // Operation code for each message type
    public static final String ACKMESSAGE = "A";
    public static final String ERRORMESSAGE = "E";
    public static final String NEWMESSAGE = "N";
    public static final String QUERYMESSAGE = "Q";
    public static final String RESPONSEMESSAGE = "R";

    // Minimum valid port number
    public static final int MINVALIDPORT = 1;

    // Maximum valid port number
    public static final int MAXVALIDPORT = 65535;

    // Using Ascii for encoding
    public static final Charset ENC = StandardCharsets.US_ASCII;

    /**
     * Validates the name of a host
     *
     * @param name name of the host
     */
    public static void hostNameValidation(String name) {

        // Check for null host name
        try {
            Objects.requireNonNull(name);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Null host name");
        }

        // Check for host name
        if (!name.matches("[a-zA-Z0-9.-]+")) {
            throw new IllegalArgumentException("Invalid host name");
        }
    }

    /**
     * Validates the port number
     *
     * @param port port number
     */
    public static void portValidation(int port) {

        // Check if port specified is within the valid range
        if (port < MINVALIDPORT || port > MAXVALIDPORT) {
            throw new IllegalArgumentException("Invalid port");
        }
    }

    /**
     * Validates basic message bytes
     *
     * @param msgBytes message bytes to decode
     */
    private static void basicMessageValidation(byte[] msgBytes) {

        // Validates for null message bytes
        try {
            Objects.requireNonNull(msgBytes);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Null message bytes");
        }

        // All messages should be at least two bytes long
        if (msgBytes.length < 2) {
            throw new IllegalArgumentException("Short message bytes: " + Arrays.toString(msgBytes));
        }

        // Check if message contains a space
        if (new String(msgBytes, ENC).toCharArray()[1] != ' ') {
            throw new IllegalArgumentException("Invalid message bytes: " + Arrays.toString(msgBytes));
        }
    }

    /**
     * Decodes a Message to appropriate type
     *
     * @param msgBytes bytes of encoded Message
     * @return a sub class of Message depending on the operation code
     * @throws IllegalArgumentException if arguments invalid
     */
    public static Message decode(byte[] msgBytes) throws IllegalArgumentException {

        // Validates received bytes
        basicMessageValidation(msgBytes);

        // Construct payload in String form if pass validation
        String message = new String(msgBytes, ENC);
        String operation = message.substring(0, 1);
        String payload = message.substring(2);

        // Return the appropriate static decode method for different operation
        switch (operation) {
            case ACKMESSAGE:
                return ACK.decodeACK(payload);
            case ERRORMESSAGE:
                return Error.decodeError(payload);
            case NEWMESSAGE:
                return New.decodeNew(payload);
            case QUERYMESSAGE:
                return Query.decodeQuery(payload);
            case RESPONSEMESSAGE:
                return Response.decodeResponse(payload);
            default:
                throw new IllegalArgumentException("Unexpected message");
        }
    }

    /**
     * Validates the payload decoded
     *
     * @param payload payload of decoded Message
     */
    public static void generalPayloadValidation(String payload) {

        // Check for null payload
        try {
            Objects.requireNonNull(payload);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Null payload");
        }

        // Check for empty payload
        if (payload.equals("")) {
            throw new IllegalArgumentException("Empty payload");
        }

        // Payloads of ACK, New has to contain a :
        if (!payload.contains(":")) {
            throw new IllegalArgumentException("Invalid payload");
        }

        String[] pair = payload.split(":");

        // Check if port number is valid
        try {
            Integer.parseInt(pair[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Port number not detected");
        }
    }

    /**
     * Abstract prototype of encode function
     *
     * @return encoded bytes
     */
    public abstract byte[] encode();

    /**
     * Abstract prototype of setOperation function
     *
     * @param operation operation code
     */
    public abstract void setOperation(String operation);

    /**
     * Abstract prototype of getOperation function
     *
     * @return operation code
     */
    public abstract String getOperation();
}
