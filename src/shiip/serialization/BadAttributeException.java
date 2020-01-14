/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization;

import java.io.Serializable;

/**
 * BadAttributeException Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class BadAttributeException extends Exception implements Serializable {
    /**
     * serial ID for BadAttributeException
     */
    private static final long serialVersionUID = 1L;

    /**
     * Attribute that caused the exception
     */
    private String attribute;

    /**
     * Constructor 1 of BadAttributeException
     *
     * @param message   message to display
     * @param attribute attribute that caused the exception
     */
    public BadAttributeException(String message, String attribute) {
        super(message);
        this.attribute = attribute;
    }

    /**
     * Constructor 2 of BadAttributeException
     *
     * @param message   message to display
     * @param attribute attribute caused the exception
     * @param cause     the actual Exception that caused BadAttributeException
     */
    public BadAttributeException(String message, String attribute, Throwable cause) {
        super(message, cause);
        this.attribute = attribute;
    }

    /**
     * Get the attribute that caused the exception
     *
     * @return the attribute caused the exception
     */
    public String getAttribute() {
        return this.attribute;
    }
}
