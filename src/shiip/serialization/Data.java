/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization;

import com.twitter.hpack.Encoder;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

import static shiip.serialization.Framer.HEADERLENGTH;
import static shiip.serialization.Framer.MAXPAYLOADLENGTH;

/**
 * Data Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class Data extends Message {
    /**
     * end value for Data
     */
    private boolean end;

    /**
     * Bytes of Data
     */
    private byte[] data;

    /**
     * Minimum stream ID allowed for Data
     */
    private static final int MINDATASTREAMID = 1;

    /**
     * Data constructor
     *
     * @param streamID stream ID
     * @param isEnd    true if last data message
     * @param data     bytes of application data
     * @throws BadAttributeException - if attributes invalid (set protocol spec)
     */
    public Data(int streamID, boolean isEnd, byte[] data) throws BadAttributeException {

        // Use setters to assign fields
        this.setStreamID(streamID);
        this.setData(data);
        this.setEnd(isEnd);
        this.setCode(DATA_MESSAGE);
    }

    /**
     * Overwritten equals for Data
     *
     * @param o Data object in this case
     * @return true or false based on if they are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Data)) return false;
        Data data1 = (Data) o;
        return end == data1.end &&
                this.getStreamID() == data1.getStreamID() &&
                Arrays.equals(data, data1.data);
    }

    /**
     * Overwritten hashCode for Data
     *
     * @return integer result of hash code
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(end, this.getStreamID());
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    /**
     * Get the data in the payload
     *
     * @return byte array data
     */
    public byte[] getData() {
        return this.data;
    }

    /**
     * Determine if the Data is the end of Data messages
     *
     * @return the end value
     */
    public boolean isEnd() {
        return this.end;
    }

    /**
     * Sets the Data payload
     *
     * @param data the payload for Data
     * @throws BadAttributeException If attribute validation fails
     */
    public void setData(byte[] data) throws BadAttributeException {

        // Data cannot be null
        try {
            Objects.requireNonNull(data);
        } catch (NullPointerException e) {
            throw new BadAttributeException("Error: Null Data", Arrays.toString((byte[]) null), new NullPointerException());
        }
        // If data being passed is too big then throw exception
        if (data.length > MAXPAYLOADLENGTH) {
            throw new BadAttributeException("Error: Data too big", Arrays.toString(data), new NullPointerException());
        }
        this.data = data;
    }

    /**
     * Set if this is the end of Data messages
     *
     * @param end end value
     */
    public void setEnd(boolean end) {
        this.end = end;
    }

    /**
     * Overwritten toString for Data
     *
     * @return message for Data
     */
    @Override
    public String toString() {
        return "Data: StreamID=" + this.getStreamID() + " isEnd=" + this.isEnd() + " data=" + this.getData().length;
    }

    /**
     * encodes Data message
     *
     * @param encoder encoder to encode message
     * @return byte array of encoded message
     */
    public byte[] encode(Encoder encoder) {

        byte[] encodedData = new byte[data.length + HEADERLENGTH];

        // Type code for Data
        encodedData[0] = DATA_MESSAGE;

        // If isEnd is set, then set END_STREAM flag
        if (isEnd()) {
            encodedData[1] |= 1;
        }

        byte[] sidB = ByteBuffer.allocate(4).putInt(this.getStreamID()).array();

        // Encode the stream id to bytes
        System.arraycopy(sidB, 0, encodedData, 2, 4);

        // Encode the date in the payload
        System.arraycopy(data, 0, encodedData, 6, encodedData.length - 6);

        return encodedData;
    }

    /**
     * Decode the Data message
     *
     * @param msgBytes the encoded bytes of Data message
     * @return a new decoded Data object
     * @throws BadAttributeException if attributes validation failed
     */
    public static Data decodeData(byte[] msgBytes) throws BadAttributeException {
        byte[] sidBytes = new byte[4];

        // Copy bytes for stream id
        System.arraycopy(msgBytes, 2, sidBytes, 0, sidBytes.length);

        // Convert sid bytes to integer
        ByteBuffer sidWrapper = ByteBuffer.wrap(sidBytes);
        int sid = sidWrapper.getInt();
        byte flags = msgBytes[1];

        dataValidation(msgBytes, sid, flags);

        boolean isEnd = false;

        // If detected END_STREAM flag, set isEnd
        if ((byte) ((flags) & 1) == 1) {
            isEnd = true;
        }

        byte[] dataMessage = new byte[msgBytes.length - 6];

        // Copy data in the payload
        System.arraycopy(msgBytes, 6, dataMessage, 0, dataMessage.length);

        return new Data(sid, isEnd, dataMessage);
    }

    /**
     * Validates Data message
     *
     * @param msgBytes    encoded bytes for Data
     * @param encodedSID  encoded stream ID for Data
     * @param encodedFlag encoded flags for Data
     * @throws BadAttributeException if validation fails
     */
    private static void dataValidation(byte[] msgBytes, int encodedSID, byte encodedFlag) throws BadAttributeException {

        // If message is too long, throw exception
        if (msgBytes.length > MAXPAYLOADLENGTH + HEADERLENGTH) {
            throw new BadAttributeException("Deserialization Error: Message too long", String.valueOf(msgBytes.length));
        }

        // Detect bad flags in Data
        if ((byte) ((encodedFlag >> 3) & 1) == 1) {
            throw new BadAttributeException("Deserialization Error: Invalid Flag for DATA", String.valueOf(encodedFlag));
        }

        // Stream id for Data cannot be zero
        if (encodedSID < MINDATASTREAMID) {
            throw new BadAttributeException("Deserialization Error: Invalid Stream ID for DATA", String.valueOf(encodedFlag));
        }
    }

    /**
     * Overwrites the superclass method
     *
     * @param streamID set stream ID
     * @throws BadAttributeException if validation fails
     */
    public void setStreamID(int streamID) throws BadAttributeException {
        if (streamID < MINDATASTREAMID) {
            throw new BadAttributeException("Stream ID must be >= 1", String.valueOf(streamID));
        }
        super.setStreamID(streamID);
    }
}
