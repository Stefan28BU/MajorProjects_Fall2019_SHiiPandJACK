/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/

package jack.serialization;

import java.util.Objects;

/**
 * New Class
 *
 * @author Yufan Xu
 * @version 1.1
 */
public class New extends Message {

    // Name of host
    private String host;

    // Port number
    private int port;

    // Type of operation
    private String operation;

    /**
     * Overwritten equals
     *
     * @param o object
     * @return true or false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof New)) return false;
        New aNew = (New) o;
        return getPort() == aNew.getPort() &&
                getHost().equals(aNew.getHost()) &&
                getOperation().equals(aNew.getOperation());
    }

    /**
     * Overwritten hashCode
     * @return hashed integer value
     */
    @Override
    public int hashCode() {
        return Objects.hash(getHost(), getPort(), getOperation());
    }

    /**
     * New constructor
     *
     * @param host name of host
     * @param port port number
     * @throws IllegalArgumentException if arguments invalid
     */
    public New(String host, int port) throws IllegalArgumentException {
        this.setOperation(NEWMESSAGE);
        this.setHost(host);
        this.setPort(port);
    }

    /**
     * Gets the host name
     *
     * @return the host name
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host name
     *
     * @param host name of host
     * @throws IllegalArgumentException if arguments invalid
     */
    public final void setHost(String host) throws IllegalArgumentException {

        // Call super class's method hostNameValidation to validate host name
        Message.hostNameValidation(host);

        this.host = host;
    }

    /**
     * Gets the port number
     *
     * @return the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port number
     *
     * @param port port number
     * @throws IllegalArgumentException if arguments invalid
     */
    public final void setPort(int port) throws IllegalArgumentException {

        // Call super class's method portValidation to validate port number
        Message.portValidation(port);
        this.port = port;
    }

    /**
     * Overwrites toString function
     *
     * @return a String of specified message
     */
    public String toString() {
        return "NEW [" + host + ":" + port + "]";
    }

    /**
     * Decodes an New Message
     *
     * @param payload String form of payload
     * @return an new New Message
     */
    public static New decodeNew(String payload) {
        Message.generalPayloadValidation(payload);

        String [] pair = payload.split(":");

        return new New(pair[0], Integer.parseInt(pair[1]));
    }

    /**
     * Sets the operation code
     *
     * @param operation the operation code
     */
    @Override
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Encodes an New Message
     *
     * @return encoded bytes
     */
    @Override
    public byte[] encode() {
        String encoded = operation + " " + host + ":" + port;
        return encoded.getBytes(ENC);
    }

    /**
     * Gets the operation code
     *
     * @return the operation code
     */
    @Override
    public String getOperation() {
        return operation;
    }
}
