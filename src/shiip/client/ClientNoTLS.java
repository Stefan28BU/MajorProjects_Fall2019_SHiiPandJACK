/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 6
 * Class: CSI 4321
 *
 ************************************************/

package shiip.client;

/**
 * ClientNoTLS Class
 *
 * @author Yufan Xu
 * @version 1.0
 */
public class ClientNoTLS {

    /**
     * Main function of ClientNoTls
     *
     * @param args String [] of command line arguments
     */
    public static void main(String[] args) {
        Client.startClient(args, "No tls");
    }
}
