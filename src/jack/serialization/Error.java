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
 * Error Class
 *
 * @author Yufan Xu
 * @version 1.1
 */
public class Error extends Message {

    // Error message
    private String errorMessage;

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
        if (!(o instanceof Error)) return false;
        Error error = (Error) o;
        return getErrorMessage().equals(error.getErrorMessage()) &&
                getOperation().equals(error.getOperation());
    }

    /**
     * Overwritten hashCode
     * @return hashed integer value
     */
    @Override
    public int hashCode() {
        return Objects.hash(getErrorMessage(), getOperation());
    }

    /**
     * Error constructor
     *
     * @param errorMessage specified error message
     * @throws IllegalArgumentException if arguments invalid
     */
    public Error(String errorMessage) throws IllegalArgumentException {
        this.setOperation(ERRORMESSAGE);
        this.setErrorMessage(errorMessage);
    }

    /**
     * Gets the error message
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message
     *
     * @param errorMessage the error message
     * @throws IllegalArgumentException if arguments invalid
     */
    public final void setErrorMessage(String errorMessage) throws IllegalArgumentException {

        // Check if errorMessage being passed is null
        try {
            Objects.requireNonNull(errorMessage);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Null error message");
        }

        // Check if error message is an empty String
        if (errorMessage.equals("")) {
            throw new IllegalArgumentException("Error message too short");
        }
        this.errorMessage = errorMessage;
    }

    /**
     * Overwrites toString function
     *
     * @return a String of specified message
     */
    public String toString() {
        return "ERROR " + errorMessage;
    }

    /**
     * Decodes an Error Message
     *
     * @param payload String form of payload
     * @return an new Error Message
     */
    public static Error decodeError(String payload) {
        return new Error(payload);
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
     * Encodes an Error Message
     *
     * @return encoded bytes
     */
    @Override
    public byte[] encode() {
        String encoded = operation + " " + errorMessage;
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
