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
 * ACK Class
 *
 * @author Yufan Xu
 * @version 1.1
 */
public class ACK extends Message {

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
        if (!(o instanceof ACK)) return false;
        ACK ack = (ACK) o;
        return getPort() == ack.getPort() &&
                getHost().equals(ack.getHost()) &&
                getOperation().equals(ack.getOperation());
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
     * ACK constructor
     *
     * @param host name of host
     * @param port port number
     * @throws IllegalArgumentException if arguments invalid
     */
    public ACK(String host, int port) throws IllegalArgumentException {
        this.setOperation(ACKMESSAGE);
        this.setHost(host);
        this.setPort(port);
    }

    /**
     * Overwrites toString function
     *
     * @return a String of specified message
     */
    public String toString() {
        return "ACK [" + host + ":" + port + "]";
    }

    /**
     * Gets the name of the host
     *
     * @return the name of the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets a name for the host
     *
     * @param host name of the host
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
     * Decodes an ACK Message
     *
     * @param payload String form of payload
     * @return an new ACK Message
     */
    public static ACK decodeACK(String payload) {
        Message.generalPayloadValidation(payload);

        String [] pair = payload.split(":");

        return new ACK(pair[0], Integer.parseInt(pair[1]));
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
     * Encodes an ACK Message
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
