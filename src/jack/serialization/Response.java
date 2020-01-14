/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/

package jack.serialization;

import java.util.*;

/**
 * Response Class
 *
 * @author Yufan Xu
 * @version 1.1
 */
public class Response extends Message {

    // List of services added
    private List<String> serviceList;

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
        if (!(o instanceof Response)) return false;
        Response response = (Response) o;
        return getServiceList().equals(response.getServiceList()) &&
                getOperation().equals(response.getOperation());
    }

    /**
     * Overwritten hashCode
     *
     * @return hashed integer value
     */
    @Override
    public int hashCode() {
        return Objects.hash(getServiceList(), getOperation());
    }

    /**
     * (non-JavaDoc)
     * <p>
     * Response constructor
     */
    public Response() {
        this.setOperation(RESPONSEMESSAGE);
        serviceList = new ArrayList<>();
    }

    /**
     * Gets the list of services added
     *
     * @return list of services
     */
    public List<String> getServiceList() {
        return serviceList;
    }

    /**
     * Adds new service pair to the service list
     *
     * @param host name of the host
     * @param port port number
     * @throws IllegalArgumentException if arguments invalid
     */
    public final void addService(String host, int port) throws IllegalArgumentException {

        // Call super class's method hostNameValidation to validate host name
        Message.hostNameValidation(host);

        // Call super class's method portValidation to validate port number
        Message.portValidation(port);

        if (!serviceList.contains(host + ":" + port)) {
            serviceList.add(host + ":" + port);

            // Append the new service and sort the list
            this.sortResponse();
        }
    }

    /**
     * Overwrites toString function
     *
     * @return a String of specified message
     */
    public String toString() {
        StringBuilder response = new StringBuilder("RESPONSE ");

        for (var str : serviceList) {
            response.append("[").append(str).append("]");
        }
        return response.toString();
    }

    /**
     * Decodes a Response Message
     *
     * @param payload String form of payload
     * @return an new Response Message
     */
    public static Response decodeResponse(String payload) {
        Response response = new Response();

        if (payload.contains(" ")) {
            String[] services = payload.split(" ");

            // Adds service from decoded message to the service list
            for (var service : services) {
                Message.generalPayloadValidation(service);

                String[] pair = service.split(":");

                response.addService(pair[0], Integer.parseInt(pair[1]));
            }
        } else if (!payload.contains(" ") && payload.length() > 0) {
            throw new IllegalArgumentException("No space");
        }
        return response;
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
     * Encodes a Response Message
     *
     * @return encoded bytes
     */
    @Override
    public byte[] encode() {
        StringBuilder encoded = new StringBuilder(operation + " ");

        // Adds the services to the encoded String
        for (var service : serviceList) {
            encoded.append(service).append(" ");
        }

        return encoded.toString().getBytes(ENC);
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

    /**
     * <non-javadoc>
     * <p>
     * Sorts the service list in ascending order
     */
    private void sortResponse() {

        // Use a custom comparator for response compairasion
        serviceList.sort(new Comparator<String>() {
            /**
             * Overwritten compare for sort function
             *
             * @param str1 first string to compare
             * @param str2 second string to compare
             * @return compared flag
             */
            public int compare(String str1, String str2) {

                String str1StringPart = str1.replaceAll("\\d", "");
                String str2StringPart = str2.replaceAll("\\d", "");

                if (str1StringPart.equalsIgnoreCase(str2StringPart)) {
                    return extractInt(str1) - extractInt(str2);
                }
                return str1.compareTo(str2);
            }

            /**
             * Compares the integer value in ascending order
             *
             * @param str string to compare
             * @return the smaller number
             */
            int extractInt(String str) {
                String number = str.replaceAll("\\D", "");

                // return 0 if no digits found
                return number.isEmpty() ? 0 : Integer.parseInt(number);
            }
        });
    }
}
