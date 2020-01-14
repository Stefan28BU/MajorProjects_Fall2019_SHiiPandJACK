/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.twitter.hpack.HeaderListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static shiip.serialization.Framer.HEADERLENGTH;

/**
 * Headers Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class Headers extends Message {

    /**
     * Charset for converting String to bytes and the opposite
     */
    private static final Charset CHARENC = StandardCharsets.US_ASCII;

    /**
     * End value of Headers
     */
    private boolean end;

    /**
     * This map stores names and values added to the Header, and sort the names automatically
     */
    private Map<String, String> valuePair = new TreeMap<>();

    /**
     * This OutputStream is for Encoder
     */
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    /**
     * Flags field of Headers if it is not the end of Headers
     */
    private static final byte NONENDHEADERSFLAG = 0x4;

    /**
     * Flags field of Headers if it is the end of Headers
     */
    private static final byte ENDHEADERSFLAG = 0x5;

    /**
     * Minimum stream ID for Headers
     */
    private static final byte MINHEADERSSTREAMID = 1;

    /**
     * Overwritten equals for Headers
     *
     * @param o Object, in this case Headers
     * @return truth value true or false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Headers)) return false;
        Headers headers = (Headers) o;
        return isEnd() == headers.isEnd() &&
                valuePair.equals(headers.valuePair) && this.getStreamID() == headers.getStreamID();
    }

    /**
     * Overwritten hashcode for Header
     *
     * @return integer value of hashed Headers
     */
    @Override
    public int hashCode() {
        return Objects.hash(isEnd(), valuePair, this.getStreamID());
    }

    /**
     * Headers constructor
     *
     * @param streamID Stream ID for Headers
     * @param isEnd    end value of Headers
     * @throws BadAttributeException if validation fails
     */
    public Headers(int streamID, boolean isEnd) throws BadAttributeException {
        if (streamID < MINHEADERSSTREAMID) {
            throw new BadAttributeException("Stream ID is 0", String.valueOf(streamID));
        }
        this.setStreamID(streamID);
        this.setEnd(isEnd);
        this.setCode(HEADERS_MESSAGE);
    }

    /**
     * Add value to Headers
     *
     * @param name  name field of Headers
     * @param value value field of Headers
     * @throws BadAttributeException if validation fails
     */
    public void addValue(String name, String value) throws BadAttributeException {
        this.headerValuesValidation(name, value);

        // Add name and its value to the map
        valuePair.put(name, value);
    }

    /**
     * Validation for values added into the Headers
     *
     * @param name  name field to add
     * @param value value field associated with the name
     * @throws BadAttributeException if validation fails
     */
    private void headerValuesValidation(String name, String value) throws BadAttributeException {

        // Name cannot be null
        try {
            Objects.requireNonNull(name, "name cannot be null");

        } catch (NullPointerException e) {
            throw new BadAttributeException("name cannot be null", name, new NullPointerException());
        }

        // Value cannot be null also
        try {
            Objects.requireNonNull(value, "value cannot be null");

        } catch (NullPointerException e) {
            throw new BadAttributeException("value cannot be null", value, new NullPointerException());
        }

        if (name.equals("") || value.equals("")) {
            throw new BadAttributeException("empty string", value);
        }

        // Legal delimiters in path
        String[] delimiters = {"(", ")", ",", "/", ";", "<", "=", ">", "?", "@", "[", "\\", "]", "{", "}", "\""};

        // Name needs to be in the range of these Ascii characters
        for (int i = 0; i < name.toCharArray().length; i++) {
            if ((byte) name.toCharArray()[i] < 0x21 || (byte) name.toCharArray()[i] > 0x7E) {
                throw new BadAttributeException("name contains non-visible char", name);
            } else {
                // Name cannot contain any delimiters
                for (String delimiter : delimiters) {
                    if (name.contains(delimiter)) {
                        throw new BadAttributeException("name contains a delimiter", name);
                    }
                }
            }
        }

        // Validation for values
        for (int i = 0; i < value.toCharArray().length; i++) {
            if ((byte) value.toCharArray()[i] < 0x20 || (byte) value.toCharArray()[i] > 0x7E) {
                if ((byte) value.toCharArray()[i] != 0x9) {
                    throw new BadAttributeException("value contains non-visible char", value);
                }
            }
        }
    }

    /**
     * Get the names added to Headers
     *
     * @return a sorted set of names
     */
    public SortedSet<String> getNames() {
        return new TreeSet<>(valuePair.keySet());
    }

    /**
     * Get the values added to Headers
     *
     * @param name name associated with the value
     * @return the value associated with the name
     */
    public String getValue(String name) {
        try {
            Objects.requireNonNull(name);
        } catch (NullPointerException e) {
            return null;
        }

        String value;

        try {
            value = Objects.requireNonNull(valuePair.get(name));
        } catch (NullPointerException e) {
            return null;
        }

        return value;
    }

    /**
     * Determines if last Headers
     *
     * @return end value
     */
    public boolean isEnd() {
        return this.end;
    }

    /**
     * Set the end value
     *
     * @param end end value to set
     */
    public void setEnd(boolean end) {
        this.end = end;
    }

    /**
     * Overwritten toString function
     *
     * @return message for Headers
     */
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("Headers: StreamID=" + this.getStreamID() + " isEnd=" + this.isEnd() + " (");
        for (String name : this.getNames()) {
            ret.append("[").append(name).append("=").append(this.getValue(name)).append("]");
        }
        ret.append(")");

        return ret.toString();
    }

    /**
     * Overwritten encode function for Message
     *
     * @param encoder encoder to encode Headers
     * @return byte array of encoded Headers
     */
    public byte[] encode(Encoder encoder) {
        Objects.requireNonNull(encoder, "encoder cannot be null");

        // Encoder the headers with name and associated value
        // IOException might occur in this step
        try {
            for (var name : this.getNames()) {
                encoder.encodeHeader(out, stringToBytes(name), stringToBytes(this.getValue(name)), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Bytes of stream ID
        byte[] sidB = ByteBuffer.allocate(4).putInt(this.getStreamID()).array();

        // The bytes for payload + header
        byte[] headerMessageBytes = new byte[HEADERLENGTH + out.size()];

        // Set the type code
        headerMessageBytes[0] = HEADERS_MESSAGE;

        // Set flags
        if (this.isEnd()) {
            headerMessageBytes[1] = ENDHEADERSFLAG;
        } else {
            headerMessageBytes[1] = NONENDHEADERSFLAG;
        }

        // Store the stream ID bytes
        System.arraycopy(sidB, 0, headerMessageBytes, 2, sidB.length);

        // Copy the encoded payload bytes
        for (int i = 0; i < out.size(); i++) {
            headerMessageBytes[i + 6] = out.toByteArray()[i];
        }

        return headerMessageBytes;
    }

    /**
     * Convert String to bytes
     *
     * @param v String value
     * @return byte array of String
     */
    private static byte[] stringToBytes(String v) {
        return v.getBytes(CHARENC);
    }

    /**
     * Convert bytes to String
     *
     * @param b byte array
     * @return String of byte array
     */
    private static String bytesToString(byte[] b) {
        return new String(b, CHARENC);
    }

    /**
     * Constructor for decoding Headers
     *
     * @param msgBytes encoded message bytes
     * @param decoder  decoder to decode the Headers
     * @throws BadAttributeException if validation fails
     */
    public Headers(byte[] msgBytes, Decoder decoder) throws BadAttributeException {
        headersValidation(msgBytes, decoder);
        this.setCode(HEADERS_MESSAGE);

        boolean isEnd = false;

        // Determine isEnd value
        if ((byte) ((msgBytes[1]) & 1) == 1) {
            isEnd = true;
        }

        // Set end value for Headers
        this.setEnd(isEnd);

        byte[] streamIDBytes = new byte[4];

        // Convert bytes of sid to integer
        System.arraycopy(msgBytes, 2, streamIDBytes, 0, streamIDBytes.length);
        ByteBuffer SIDWrapped = ByteBuffer.wrap(streamIDBytes);

        int encodedSID = SIDWrapped.getInt();

        // Set Stream ID for Headers
        this.setStreamID(encodedSID);

        // Name list
        List<String> names = new ArrayList<>();

        // Value list
        List<String> values = new ArrayList<>();

        // Decode header list from header block
        HeaderListener listener = new HeaderListener() {
            @Override
            public void addHeader(byte[] name, byte[] value, boolean sensitive) {

                // Add names to list
                names.add(bytesToString(name));

                // Add values to list
                values.add(bytesToString(value));
            }
        };
        byte[] encodedHeadersPayload = new byte[msgBytes.length - HEADERLENGTH];

        // Store encoded payload
        System.arraycopy(msgBytes, 6, encodedHeadersPayload, 0, encodedHeadersPayload.length);

        ByteArrayInputStream in = new ByteArrayInputStream(encodedHeadersPayload);

        // Decode the encoded payload
        try {
            decoder.decode(in, listener);
        } catch (IOException e) {
            decoder.endHeaderBlock();

            System.err.println(e.getMessage());
        }

        decoder.endHeaderBlock();

        // Add the decoded names and values to Headers
        for (int i = 0; i < names.size(); i++) {
            this.addValue(names.get(i), values.get(i));
        }
    }

    /**
     * Validation for Headers
     *
     * @param msgBytes encoded message for Headers
     * @param decoder  decoder to decode the Headers
     * @throws BadAttributeException if validation fails
     */
    private static void headersValidation(byte[] msgBytes, Decoder decoder) throws BadAttributeException {

        Objects.requireNonNull(decoder, "decoder cannot be null");

        byte encodedFlag = msgBytes[1];

        byte[] streamIDBytes = new byte[4];

        // Convert bytes of sid to integer
        System.arraycopy(msgBytes, 2, streamIDBytes, 0, streamIDBytes.length);
        ByteBuffer SIDWrapped = ByteBuffer.wrap(streamIDBytes);

        int encodedSID = SIDWrapped.getInt();

        // Stream ID of Headers cannot be zero
        if (encodedSID < MINHEADERSSTREAMID) {
            throw new BadAttributeException("Deserialization Error: Invalid StreamID for header", String.valueOf(encodedSID));
        }

        // Check if END_HDR flag is set
        if ((byte) ((encodedFlag >> 2) & 1) == 0) {
            throw new BadAttributeException("Deserialization Error: END_HDR not set", String.valueOf(encodedFlag));
        }

        // Check if bad flags exists
        if ((byte) ((encodedFlag >> 3) & 1) == 1 || (byte) ((encodedFlag >> 5) & 1) == 1) {
            throw new BadAttributeException("Deserialization Error: Bad Flag for Headers", String.valueOf(encodedFlag));
        }
    }
}
