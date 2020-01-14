/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 5
 * Class: CSI 4321
 *
 ************************************************/

package jack.server;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Server Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class Server {

    // Timeout for socket
    public static final int TIMEOUT = 20000;

    // Logger for the server
    public final static Logger LOGGER = Logger.getLogger(jack.server.Server.class.getName());

    // Server socket for connection with the client;
    private static DatagramSocket socket;

    /**
     * (non-javadoc)
     * <p>
     * This function initializes the file handler for our logger class
     */
    private static void initializeLoggerHandler() {
        FileHandler fileHandler;

        // Setting up the logger with file handler and formatter
        try {
            fileHandler = new FileHandler("jack.log");
            LOGGER.addHandler(fileHandler);
            fileHandler.setFormatter(new SimpleFormatter());
        } catch (SecurityException | IOException e) {
            LOGGER.warning("Communication problem: " + e.getMessage());
        }
    }

    /**
     * Main function of Server
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        // Log error message if I/O Exception occurs
        try {

            // Sets up the log file and logger
            initializeLoggerHandler();

            // Test for correct argument list
            if (args.length != 1) {
                LOGGER.warning("Parameter(s): <Port>");
                System.exit(1);
            }

            // Initialize socket
            try {
                socket = new DatagramSocket(Integer.parseInt(args[0]));
                socket.setSoTimeout(TIMEOUT);

            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid port: " + e.getMessage());
            }

            // Start handling the server protocols
            ServerHandler.start(LOGGER, socket);

        } catch (IOException e) {
            LOGGER.warning("Communication problem: " + e.getMessage());
        }
    }
}