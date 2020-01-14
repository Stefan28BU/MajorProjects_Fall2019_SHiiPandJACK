/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization;

import com.twitter.hpack.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import static shiip.serialization.Framer.HEADERLENGTH;

/**
 * Message Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public abstract class Message {

    /**
     * stream ID of Message for validation only
     */
    private int streamID;

    /**
     * Type code of Message for validation only
     */
    private static byte typeCode;

    /**
     * Type encoded for serialization
     */
    private static byte encodedType;

    /**
     * General minimum stream ID for Message
     */
    public static final int MINSTREAMID = 0;

    /**
     * Type code for Data is 0x0
     */
    public static final byte DATA_MESSAGE = 0x0;

    /**
     * Type code for Headers is 0x1
     */
    public static final byte HEADERS_MESSAGE = 0x1;

    /**
     * Type code for Settings is 0x4
     */
    public static final byte SETTINGS_MESSAGE = 0x4;

    /**
     * Type code for Window_Update is 0x8
     */
    public static final byte WINDOW_UPDATE_MESSAGE = 0x8;

    /**
     * Charset for writing connection prefix into the OutputStream
     */
    public static final Charset ENC = StandardCharsets.US_ASCII;

    /**
     * Validation for encoded Message
     *
     * @param msgBytes encoded bytes
     * @return byte array of modified encoded message
     * @throws BadAttributeException if validation fails
     */
    private static byte[] encodedMessageValidationAndUpdate(byte[] msgBytes) throws BadAttributeException {
        Objects.requireNonNull(msgBytes, "Deserialization Error: message cannot be null");

        // Check if length too small
        if (msgBytes.length < HEADERLENGTH) {
            throw new BadAttributeException("Deserialization Error: Bad Message Length", String.valueOf(msgBytes.length));
        }
        encodedType = msgBytes[0];

        // Check if bad type exists
        if (encodedType != DATA_MESSAGE &&
                encodedType != HEADERS_MESSAGE &&
                encodedType != SETTINGS_MESSAGE &&
                encodedType != WINDOW_UPDATE_MESSAGE) {
            throw new BadAttributeException("Deserialization Error: Bad Message Type", String.valueOf(encodedType));
        }

        // Ignore the r bit by clearing it
        msgBytes[2] = (byte) (msgBytes[2] & 0x7f);

        // This returns the modified bytes (ignoring r bits)
        return msgBytes;
    }

    /**
     * Decode message
     *
     * @param msgBytes encoded message bytes
     * @param decoder  decoder for decode messages
     * @return associated decode functions
     * @throws BadAttributeException if attributes invalid
     */
    public static Message decode(byte[] msgBytes, Decoder decoder) throws BadAttributeException {
        byte[] newMsgBytes = encodedMessageValidationAndUpdate(msgBytes);

        // Based on message type, call the decode functions for each message type
        if (encodedType == DATA_MESSAGE) {
            return Data.decodeData(newMsgBytes);
        } else if (encodedType == SETTINGS_MESSAGE) {
            return Settings.decodeSettings(newMsgBytes);
        } else if (encodedType == WINDOW_UPDATE_MESSAGE) {
            return Window_Update.decodeWindowUpdate(newMsgBytes);
        } else if (encodedType == HEADERS_MESSAGE) {
            return new Headers(newMsgBytes, decoder);
        } else {
            throw new BadAttributeException("Unexpected message type", Arrays.toString(msgBytes));
        }
    }

    /**
     * Encode the decoded message
     *
     * @param encoder encoder for encoding message
     * @return byte array of encoded message
     */
    public abstract byte[] encode(Encoder encoder);

    /**
     * Get the type code
     *
     * @return type code
     */
    public byte getCode() {
        return typeCode;
    }

    /**
     * Get stream ID
     *
     * @return stream ID
     */
    public int getStreamID() {
        return streamID;
    }

    /**
     * Set the stream ID
     *
     * @param streamID stream ID to set
     * @throws BadAttributeException if attribute invalid
     */
    public void setStreamID(int streamID) throws BadAttributeException {

        // Stream id cannot be negative
        if (streamID < MINSTREAMID) {
            throw new BadAttributeException("Serialization Error: Invalid Stream ID" + streamID, String.valueOf(streamID));
        }

        this.streamID = streamID;
    }

    /**
     * Set the type code
     *
     * @param code type code to set
     */
    public void setCode(byte code) {
        typeCode = code;
    }
}
