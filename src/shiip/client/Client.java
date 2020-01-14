/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;

import shiip.serialization.*;
import tls.TLSFactory;

import static shiip.serialization.Message.ENC;

/**
 * Client Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class Client {

    /**
     * Header size and window size
     */
    public static final int FIXEDMAXHEADERWINDOWSIZE = 4096;

    /**
     * Counter for counting closed files
     */
    private static int fileClosedCount = 0;

    /**
     * This map stores the stream ID with the file associated with the ID
     * So that we can write stuff in parallel
     */
    private static Map<Integer, FileOutputStream> filePairs = new HashMap<>();

    /**
     * Minimum number of arguments
     */
    private static final int MINARGUMENTSLENGTH = 2;

    /**
     * Preface length
     */
    public static final int PREFACELENGTH = 24;

    /**
     * Preface Bytes
     */
    public static final String PREFACESTR = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";

    /**
     * This function validates command line arguments
     *
     * @param args String [] of command line arguments
     */
    public static void clientArgumentsValidation(String[] args) {

        // There has to be at least a server and a port entered
        if (args.length < MINARGUMENTSLENGTH) {
            throw new IllegalArgumentException("Parameter(s): <server> <port> <file to download>");
        }
        try {
            Objects.requireNonNull(args[0], "Server is not specified");
            Objects.requireNonNull(args[1], "Port is not specified");
        } catch (NullPointerException e) {
            System.err.println(e.getMessage());
        }

        try {
            Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Starts the client thread
     *
     * @param args       commandline arguments
     * @param clientType type of client application
     */
    public static void startClient(String args[], String clientType) {
        clientArgumentsValidation(args);

        String serverPath = args[0];

        // Might throw NumberFormatException
        int port = Integer.parseInt(args[1]);

        if (port < 0) {
            port = 8080;
        }

        // List that stores all the paths
        List<String> pageList = addPaths(args);

        Socket socket = null;
        OutputStream out = null;
        Deframer deframer = null;
        Framer framer = null;

        try {
            // Set up socket connection based on client type
            if (clientType.equalsIgnoreCase("tls")) {
                socket = TLSFactory.getClientSocket(serverPath, port);
            } else {
                socket = new Socket(serverPath, port);
            }

            // set up framer and deframer
            out = socket.getOutputStream();
            deframer = new Deframer(socket.getInputStream());
            framer = new Framer(out);

        } catch (Exception e) {
            System.err.println(e.getMessage());

            // Close OutputStream and socket
            try {
                Objects.requireNonNull(out).close();
                socket.close();
            } catch (NullPointerException | IOException ex) {
                System.err.println(ex.getMessage());
            }

            System.exit(1);
        }

        try {
            // Set up De/Encoder
            Decoder decoder = new Decoder(FIXEDMAXHEADERWINDOWSIZE, FIXEDMAXHEADERWINDOWSIZE);
            Encoder encoder = new Encoder(FIXEDMAXHEADERWINDOWSIZE);

            // Send connection preface and Settings frame
            if (out != null) {
                out.write(PREFACESTR.getBytes(ENC));
            }
            framer.putFrame(new Settings().encode(null));

            constructFiles(pageList, framer, encoder, serverPath);
            messageActionHandler(deframer, framer, decoder);

            // Close OutputStream
            if (out != null) {
                out.close();
            }

            // Close Socket connection
            socket.close();
        } catch (BadAttributeException | NullPointerException | IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);

            // Close OutputStream and socket
            try {
                out.close();
                socket.close();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    /**
     * Main function of Client
     *
     * @param args String [] of command line arguments
     */
    public static void main(String[] args) {
        Client.startClient(args, "tls");
    }

    /**
     * This function handles all the Message Handling based on Message type received
     *
     * @param deframer deframer that deconstruct a frame
     * @param framer   framer that construct a frame
     * @param decoder  decoder for decoding the Header Message
     * @throws BadAttributeException if invalid attributes were detected
     * @throws IOException           if IOException occurs
     */
    public static void messageActionHandler(Deframer deframer, Framer framer, Decoder decoder) throws
            BadAttributeException, IOException {
        Message message = null;
        boolean unableToParseDisplayed = false;
        boolean invalidMessageDisplayed = false;

        // This terminates if finish writing to files
        while (fileClosedCount != filePairs.size()) {

            // If exception occurs, terminate the client
            try {
                message = Message.decode(deframer.getFrame(), decoder);
            } catch (BadAttributeException | IOException e) {
                if (e instanceof BadAttributeException) {
                    if (!invalidMessageDisplayed) {
                        System.err.println("Invalid message: " + e.getMessage());
                        invalidMessageDisplayed = true;
                    }
                    continue;
                } else {
                    if (!unableToParseDisplayed) {
                        System.err.println("Unable to parse: " + e.getMessage());
                        unableToParseDisplayed = true;
                    }
                    continue;
                }
            }

            unableToParseDisplayed = false;
            invalidMessageDisplayed = false;

            // If Settings or Window_Update is received, just display message
            if (message instanceof Settings || message instanceof Window_Update) {
                System.out.println("Received message: " + message);
            } else {
                if (message instanceof Headers) {
                    handleHeaders(message);
                } else if (message instanceof Data) {
                    handleData(message, framer);
                }
            }
        }

        fileClosedCount = 0;
    }

    /**
     * This function stores all the paths specified in the terminal
     *
     * @param args Terminal arguments entered
     * @return a list of paths
     */
    public static List<String> addPaths(String[] args) {

        // List that stores all the paths
        List<String> pageList = new ArrayList<>();

        // '/' is required at the beginning of a path
        for (int i = 2; i < args.length; i++) {
            if (args[i].toCharArray()[0] != '/') {
                throw new IllegalArgumentException("path is not valid");
            }

            // Add path to the list
            pageList.add(args[i]);
        }
        return pageList;
    }

    /**
     * This function creates files to download
     *
     * @param pageList   stores all the paths specified in the arguments
     * @param framer     frames for serialization
     * @param encoder    encoder for encoding the Headers
     * @param serverPath path for server
     * @throws BadAttributeException if attributes invalid
     * @throws IOException           if I/O exception occurs
     */
    public static void constructFiles(List<String> pageList, Framer framer, Encoder encoder, String serverPath) throws
            BadAttributeException, IOException {
        // Set a initial stream ID
        int streamID = 1;

        // Create Headers frame
        // Create files and store them with its stream ID
        for (var link : pageList) {
            filePairs.put(streamID, new FileOutputStream(configFileName(link).toString()));

            // Create request header for default page
            Headers header = new Headers(streamID, false);

            header.addValue(":method", "GET");
            header.addValue(":authority", serverPath);
            header.addValue(":scheme", "https");
            header.addValue(":path", link);
            header.addValue("accept-encoding", "deflate");

            // Send request
            framer.putFrame(header.encode(encoder));

            // Increment stream ID by 2 so it stays odd
            streamID += 2;
        }
    }

    /**
     * This function replaces each '/' with a '-'
     *
     * @param path path specified by the user
     * @return a StringBuilder form of path
     */
    private static StringBuilder configFileName(String path) {
        StringBuilder linkB = new StringBuilder(path);

        for (int i = 0; i < linkB.length(); i++) {
            if (linkB.charAt(i) == '/') {
                linkB.setCharAt(i, '-');
            }
        }

        return linkB;
    }

    /**
     * This function handles the events for Data message
     *
     * @param message message of Data
     * @param framer  framer for message
     * @throws IOException           if IO Exception occurs
     * @throws BadAttributeException if attributes invalid
     */
    private static void handleData(Message message, Framer framer) throws IOException, BadAttributeException {
        Data data = (Data) message;

        // If the stream ID received is not requested, print error message and go to next iteration
        if (!filePairs.containsKey(data.getStreamID())) {
            System.err.println("Unexpected stream ID: " + data);
        } else {
            // Display Data message
            System.out.println("Received message: " + data);

            // Write to the file associated with the stream ID
            filePairs.get(data.getStreamID()).write(data.getData());

            // If Data is the end message, we are done writing, so close the file
            if (data.isEnd()) {
                filePairs.get(data.getStreamID()).close();
                fileClosedCount++;
            }

            // If data has zero length, abort sending Window_Update
            if (data.getData().length != 0) {
                framer.putFrame(new Window_Update(0, data.getData().length).encode(null));
                framer.putFrame(new Window_Update(data.getStreamID(), data.getData().length).encode(null));
            }
        }
    }

    /**
     * This function handles the events for headers message
     *
     * @param message message of headers
     * @throws IOException if IO exception occurs
     */
    private static void handleHeaders(Message message) throws IOException {
        Headers headers = (Headers) message;

        // If the stream ID received is not requested, print error message and go to next iteration
        if (!filePairs.containsKey(headers.getStreamID())) {
            System.err.println("Unexpected stream ID: " + headers);
        } else {
            // Display headers message
            System.out.println("Received message: " + headers);

            // If status value does not start at 200, print error message and go to next iteration
            if (!headers.getValue(":status").startsWith("200")) {
                System.err.println("Bad status: " + headers.getValue(":status"));

                filePairs.get(headers.getStreamID()).close();
                fileClosedCount++;
            }
        }
    }
}