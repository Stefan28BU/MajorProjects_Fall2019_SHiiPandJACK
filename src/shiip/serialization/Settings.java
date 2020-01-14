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
 * Settings Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class Settings extends Message {

    /**
     * The stream ID for Settings should be 0
     */
    private static final int FIXEDSETTINGSSTREAMID = 0;

    /**
     * The flags field for Settings should be 1
     */
    private static final int FIXEDSETTINGFLAGS = 1;

    /**
     * Overwritten equals for Settings
     *
     * @param o Settings object in this case
     * @return true or false based on if they are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Settings)) return false;
        Settings settings = (Settings) o;
        return this.getStreamID() == settings.getStreamID();
    }

    /**
     * Overwritten hashCode for Settings
     *
     * @return integer result of hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getStreamID());
    }

    /**
     * Settings constructor
     *
     * @throws BadAttributeException if validation fails
     */
    public Settings() throws BadAttributeException {
        this.setStreamID(FIXEDSETTINGSSTREAMID);

        this.setCode(SETTINGS_MESSAGE);
    }

    /**
     * Overwritten toString for Settings
     *
     * @return Settings message to display
     */
    @Override
    public String toString() {
        return "Settings: StreamID=" + this.getStreamID();
    }

    /**
     * encode the Settings message
     *
     * @param encoder encoder to encode message
     * @return byte array of encoded Settings message
     */
    @Override
    public byte[] encode(Encoder encoder) {
        int sid = this.getStreamID();

        byte[] settings = new byte[6];

        settings[0] = SETTINGS_MESSAGE;
        settings[1] = FIXEDSETTINGFLAGS;

        // Storing stream id into bytes
        byte[] sidB = ByteBuffer.allocate(4).putInt(sid).array();

        System.arraycopy(sidB, 0, settings, 2, settings.length - 2);

        return settings;
    }

    /**
     * Decode Settings
     *
     * @param msgBytes encoded bytes for Settings
     * @return new Settings object
     * @throws BadAttributeException if validation fails
     */
    public static Settings decodeSettings(byte[] msgBytes) throws BadAttributeException {
        settingsValidation(msgBytes);

        return new Settings();
    }

    /**
     * Validation for Settings
     *
     * @param msgBytes encoded bytes of Settings
     * @throws BadAttributeException if validation fails
     */
    private static void settingsValidation(byte[] msgBytes) throws BadAttributeException {
        byte[] streamIDBytes = new byte[4];

        // Convert bytes of sid to integer
        System.arraycopy(msgBytes, 2, streamIDBytes, 0, streamIDBytes.length);
        ByteBuffer SIDWrapped = ByteBuffer.wrap(streamIDBytes);

        int encodedSID = SIDWrapped.getInt();

        // Stream id for Settings has to be zero
        if (encodedSID != FIXEDSETTINGSSTREAMID) {
            throw new BadAttributeException("Deserialization Error: Invalid Stream ID for SETTINGS", String.valueOf(encodedSID));
        }
    }

    /**
     * Overwritten from super class
     *
     * @param streamID stream ID to set
     * @throws BadAttributeException if validation fail
     */
    public void setStreamID(int streamID) throws BadAttributeException {
        if (streamID != FIXEDSETTINGSSTREAMID) {
            throw new BadAttributeException("Stream ID must be 0", String.valueOf(streamID));
        }
        super.setStreamID(streamID);
    }
}
