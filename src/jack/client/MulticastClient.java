/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 7
 * Class: CSI 4321
 *
 ************************************************/

package jack.client;

/**
 * MulticastClient Class
 * <p>
 * This class uses functions from the client class to avoid duplicate code
 * <p>
 * The functions from Client class handles the multicast client based on the ID being passed to function "start"
 *
 * @author Yufan Xu
 * @version 1.0
 */
public class MulticastClient {

    /**
     * The identifier for the multicast client
     */
    public static final String MULTICASTCLIENTID = "multicast";

    /**
     * Main function of the Client
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Client.start(args, MULTICASTCLIENTID);
    }
}
