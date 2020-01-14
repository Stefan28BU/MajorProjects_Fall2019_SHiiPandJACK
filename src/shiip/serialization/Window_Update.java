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
import java.util.Objects;

/**
 * Window_Update Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class Window_Update extends Message {
    /**
     * increment value of window size
     */
    private int increment;

    /**
     * stream ID for Window_Update
     */
    private int sid;

    /**
     * The fixed Message length of Window_Update is always 10 bytes
     */
    private static final int WINDOWUPDATESIZE = 10;

    /**
     * The minimum increment should be increment by 1
     */
    private static final int MININCREMENTSIZE = 1;

    /**
     * Overwritten equals for Window_Update
     *
     * @param o Window_Update object in this case
     * @return true or false based on if they are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Window_Update)) return false;
        Window_Update that = (Window_Update) o;
        return increment == that.increment &&
                sid == that.sid;
    }

    /**
     * Overwritten hashCode for Window_Update
     *
     * @return integer result of hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(increment, sid);
    }

    /**
     * Constructor of Window_Update
     *
     * @param streamID  stream ID of windowUpdate
     * @param increment increment size of window
     * @throws BadAttributeException if attributes invalid
     */
    public Window_Update(int streamID, int increment) throws BadAttributeException {
        this.sid = streamID;
        this.setStreamID(sid);
        this.setIncrement(increment);
        this.setCode(WINDOW_UPDATE_MESSAGE);
    }

    /**
     * Get the incremented size
     *
     * @return integer of increment size
     */
    public int getIncrement() {
        return this.increment;
    }

    /**
     * Set the increment size value
     *
     * @param increment increment value
     * @throws BadAttributeException if attributes invalid
     */
    public void setIncrement(int increment) throws BadAttributeException {
        if (increment < MININCREMENTSIZE) {
            throw new BadAttributeException("Error: Invalid Increment Value", String.valueOf(increment));
        }
        this.increment = increment;
    }

    /**
     * Overwritten toString for Window_Update
     *
     * @return String message to display
     */
    @Override
    public String toString() {
        return "Window_Update: StreamID=" + this.getStreamID() + " increment=" + this.getIncrement();
    }

    /**
     * Encode the message as windowUpdate
     *
     * @param encoder encoder to encode
     * @return byte array of encoded message
     */
    public byte[] encode(Encoder encoder) {
        int sid = this.getStreamID();

        byte[] windowUp = new byte[10];

        windowUp[0] = WINDOW_UPDATE_MESSAGE;

        // storing stream id into bytes
        byte[] sidB = ByteBuffer.allocate(4).putInt(sid).array();

        System.arraycopy(sidB, 0, windowUp, 2, 4);

        // storing increment value into bytes
        int inc = this.getIncrement();

        byte[] incB = ByteBuffer.allocate(4).putInt(inc).array();

        System.arraycopy(incB, 0, windowUp, 6, windowUp.length - 6);

        return windowUp;
    }

    /**
     * Decode Window_Update message
     *
     * @param msgBytes encoded bytes
     * @return new Window_Update object
     * @throws BadAttributeException if validation fails
     */
    public static Window_Update decodeWindowUpdate(byte[] msgBytes) throws BadAttributeException {
        window_updateValidation(msgBytes);

        // Ignore the r bit in the payload of windowUpdate by clearing it
        msgBytes[6] &= 0x7f;

        byte[] sidBytes = new byte[4];

        // Get the stream id from bytes to integer
        System.arraycopy(msgBytes, 2, sidBytes, 0, sidBytes.length);

        ByteBuffer sidWrapper = ByteBuffer.wrap(sidBytes);
        int sid = sidWrapper.getInt();

        byte[] incBytes = new byte[4];

        // Get the increment value from bytes to integer
        System.arraycopy(msgBytes, 6, incBytes, 0, incBytes.length);

        ByteBuffer incWrapper = ByteBuffer.wrap(incBytes);
        int inc = incWrapper.getInt();

        return new Window_Update(sid, inc);
    }

    /**
     * Validation for Window_Update
     *
     * @param msgBytes encoded message bytes
     * @throws BadAttributeException if validation fails
     */
    private static void window_updateValidation(byte[] msgBytes) throws BadAttributeException {

        // Length of Window_Update has to be 10
        if (msgBytes.length != WINDOWUPDATESIZE) {
            throw new BadAttributeException("Deserialization Error: Invalid Length for Window_Update", String.valueOf(msgBytes.length));
        }
    }
}
