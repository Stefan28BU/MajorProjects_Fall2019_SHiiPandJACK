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
 * Query Class
 *
 * @author Yufan Xu
 * @version 1.1
 */
public class Query extends Message {

    // Search String
    private String searchString;

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
        if (!(o instanceof Query)) return false;
        Query query = (Query) o;
        return getSearchString().equals(query.getSearchString()) &&
                getOperation().equals(query.getOperation());
    }

    /**
     * Overwritten hashCode
     *
     * @return hashed integer value
     */
    @Override
    public int hashCode() {
        return Objects.hash(getSearchString(), getOperation());
    }

    /**
     * Query constructor
     *
     * @param searchString string to search
     * @throws IllegalArgumentException if arguments invalid
     */
    public Query(String searchString) throws IllegalArgumentException {
        this.setOperation(QUERYMESSAGE);
        this.setSearchString(searchString);
    }

    /**
     * Gets the search string
     *
     * @return the search sting
     */
    public String getSearchString() {
        return searchString;
    }

    /**
     * Sets the search string
     *
     * @param searchString the search string
     * @throws IllegalArgumentException if arguments invalid
     */
    public final void setSearchString(String searchString) throws IllegalArgumentException {
        try {
            Objects.requireNonNull(searchString);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Null searchString");
        }
        if (!searchString.equals("*") && !searchString.equals("")) {

            // Validate the search string by using the host name validation
            Message.hostNameValidation(searchString);
        }

        this.searchString = searchString;
    }

    /**
     * Overwrites toString function
     *
     * @return a String of specified message
     */
    public String toString() {
        return "QUERY " + searchString;
    }

    /**
     * Decodes a Query Message
     *
     * @param payload String form of payload
     * @return an new Query Message
     */
    public static Query decodeQuery(String payload) {
        return new Query(payload);
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
     * Encodes a Query Message
     *
     * @return encoded bytes
     */
    @Override
    public byte[] encode() {
        String encoded = operation + " " + searchString;
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
