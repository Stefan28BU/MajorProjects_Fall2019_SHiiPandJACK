/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 3
 * Class: CSI 4321
 *
 ************************************************/

package shiip.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;

import shiip.serialization.*;
import tls.TLSFactory;

import static shiip.client.Client.FIXEDMAXHEADERWINDOWSIZE;

/**
 * Server Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class Server implements Runnable {

    /**
     * Logger for server
     */
    public final static Logger LOGGER = Logger.getLogger(Server.class.getName());

    /**
     * Socket for server
     */
    private static ServerSocket serverSocket;

    /**
     * Maximum size per data packet
     */
    public static final int MAXDATASIZE = 500;

    /**
     * Minimum time interval per data packet
     */
    public static final int MINDATAINTERVAL = 500;

    /**
     * Timeout for client socket is 20 seconds
     */
    private static final int MAXTIMETOTERMINATION = 20000;

    /**
     * Port number
     */
    private static int port;

    /**
     * Root of documents to send
     */
    private static String documentRoot;

    /**
     * Number of thread to start server
     */
    private static int threadNum;

    /**
     * Generated thread ID per client socket thread
     */
    private int serverThreadID;

    /**
     * Constructor of Server
     *
     * @param serverThreadID the ID given by user for each Client thread
     */
    public Server(int serverThreadID) {
        this.serverThreadID = serverThreadID;
    }

    /**
     * Validates the command line arguments and store to variables
     *
     * @param args command lind arguments
     */
    private static void argumentsValidation(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("Parameter(s): <port> <# of threads> <name of the document root>");
        }
        documentRoot = args[2];

        // If user input invalid numbers for port or number of threads, set default server port and thread number
        // If user failed to input integers for port and number of threads, terminate server
        try {
            port = Integer.parseInt(args[0]);
            threadNum = Integer.parseInt(args[1]);

            if (port <= 0) {
                throw new IllegalArgumentException("Bad port");
            }

            if (threadNum <= 0) {
                throw new IllegalArgumentException("Bad thread number");
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * (non-javadoc)
     * <p>
     * This function initializes the file handler for our logger class
     */
    private static void initializeLoggerHandler() {
        FileHandler fileHandler;

        // Setting up the logger with file handler and formatter
        try {
            fileHandler = new FileHandler("connections.log");
            LOGGER.addHandler(fileHandler);
            fileHandler.setFormatter(new SimpleFormatter());
        } catch (SecurityException | IOException e) {
            System.err.println(e.getMessage());
            try {
                serverSocket.close();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    /**
     * (non-javadoc)
     * <p>
     * This function initializes ServerSocket
     */
    private static void initializeServerSocket() {

        // If exception occurs, terminate server
        try {

            // Initialize server socket
            serverSocket = TLSFactory.getServerListeningSocket(port, "mykeystore", "secret");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            try {
                serverSocket.close();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
            System.exit(1);
        }
    }

    /**
     * (non-javadoc)
     * <p>
     * This function initializes the Client socket
     */
    private Socket initializeClientSocket() {
        Socket socket = null;

        // If exception occurs, try close the socket
        try {

            // Initialize client socket
            socket = TLSFactory.getServerConnectedSocket(serverSocket);

            // If I/O blocks for 20 seconds, socket is said to be timed out
            socket.setSoTimeout(MAXTIMETOTERMINATION);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            try {
                Objects.requireNonNull(socket).close();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
        return socket;
    }

    /**
     * (non-javadoc)
     * <p>
     * This function starts client threads
     */
    private static void start() {
        initializeLoggerHandler();
        initializeServerSocket();

        // Spawn new threads for clients
        // if Exception occurs at this point, try terminate server
        for (int i = 0; i < threadNum; i++) {
            try {

                // start a thread for client
                Thread clientThread = new Thread(new Server(i));
                Runtime.getRuntime().addShutdownHook(clientThread);
                clientThread.start();
            } catch (Exception e) {
                System.err.println(e.getMessage());

                try {
                    serverSocket.close();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }

                System.exit(1);
            }
        }
    }

    /**
     * Main function of Server
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        argumentsValidation(args);

        Server.start();
    }

    /**
     * (non-javadoc)
     * <p>
     * This overwrites the run() function in Thread
     */
    @Override
    public void run() {
        while (true) {
            Socket socket = null;
            List<Integer> sidList = new ArrayList<>();

            try {

                // Set up client socket
                socket = initializeClientSocket();

                // Set up InputStream and OutputStream
                InputStream in = Objects.requireNonNull(socket).getInputStream();

                OutputStream out = socket.getOutputStream();

                // Set up Framer and Deframer
                Deframer deframer = new Deframer(in);
                Framer framer = new Framer(out);

                // Set up De/Encoder
                Decoder decoder = new Decoder(FIXEDMAXHEADERWINDOWSIZE, FIXEDMAXHEADERWINDOWSIZE);
                Encoder encoder = new Encoder(FIXEDMAXHEADERWINDOWSIZE);

                // Starts client socket protocol
                ServerHandler.execute(socket, framer, deframer, encoder, decoder, sidList, in, out, this.serverThreadID, documentRoot);

            } catch (Exception e) {
                System.err.println(e.getMessage());

                // If Exception is not a SocketTimeoutException, try close current connection
                if (!(e instanceof SocketTimeoutException)) {
                    try {
                        Objects.requireNonNull(socket).close();
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }
                }
            }
        }
    }
}