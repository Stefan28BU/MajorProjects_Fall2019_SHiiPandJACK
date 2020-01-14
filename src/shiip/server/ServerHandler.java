/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 3
 * Class: CSI 4321
 *
 ************************************************/

package shiip.server;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import shiip.serialization.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static shiip.client.Client.PREFACELENGTH;
import static shiip.client.Client.PREFACESTR;
import static shiip.server.Server.*;

/**
 * ServerHandler Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class ServerHandler {

    /**
     * This function handles all the events for Client
     *
     * @param socket         Client Socket
     * @param framer         framer for serialization
     * @param deframer       deframer for deserialization
     * @param encoder        encoder for encoding the Headers
     * @param decoder        decoder for decoding the Headers
     * @param sidList        list of Stream IDs containing the valid Stream ID
     * @param in             input stream for serialization
     * @param out            output stream for deserialization
     * @param serverThreadID thread ID of server
     * @param documentRoot   root of server documents
     * @throws IOException           if IO exception occurs
     * @throws BadAttributeException if attributes invalid
     */
    public static void execute(Socket socket, Framer framer, Deframer deframer, Encoder encoder, Decoder decoder, List<Integer> sidList, InputStream in, OutputStream out, int serverThreadID, String documentRoot) throws IOException, BadAttributeException {

        // Validate preface
        String prefaceStr = new String(in.readNBytes(PREFACELENGTH));

        if (!prefaceStr.equals(PREFACESTR)) {
            LOGGER.warning("Bad Preface: " + Arrays.toString(prefaceStr.getBytes()));
            socket.close();
            return;
        }

        while (true) {
            Message message;

            // Get received message from Client
            // If exceptions occurs, evaluate the exceptions and log the appropriate message
            try {
                message = Message.decode(deframer.getFrame(), decoder);
            } catch (BadAttributeException | IOException e) {
                if (e instanceof BadAttributeException) {
                    LOGGER.warning("Thread[" + serverThreadID + "]: " + "Invalid message: " + e.getMessage());

                } else if (e instanceof SocketTimeoutException) {

                    // If SocketTimeoutException is caught, meaning the socket expired
                    // Then close the client socket connection
                    System.err.println("Client socket timed out: " + ((SocketTimeoutException) e).getMessage());
                    socket.close();
                    break;
                } else {
                    LOGGER.warning("Thread[" + serverThreadID + "]: " + "Unable to parse: " + e.getMessage());
                }
                break;
            }

            // Log appropriate message for non-Headers received messages
            // If Headers is received, start handling Headers
            if (message instanceof Data) {
                LOGGER.warning("Thread[" + serverThreadID + "]: " + "Unexpected message: " + message);
            } else if (message instanceof Settings | message instanceof Window_Update) {
                LOGGER.info("Thread[" + serverThreadID + "]: " + "Received message: " + message);
            } else if (message instanceof Headers) {
                FileInputStream fileInputStream;

                // Validates for received Headers, in invalid then skip sending Data packets
                try {
                    Objects.requireNonNull(fileInputStream = receivedHeadersPathsValidation(message, framer, encoder, documentRoot, serverThreadID, "regular"));
                    Objects.requireNonNull(sidList = receivedHeadersAttributesValidation(sidList, message, serverThreadID, "regular"));
                } catch (NullPointerException e) {
                    continue;
                }

                // Store the data in file to a String Builder
                StringBuilder textBuilder = new StringBuilder();
                Reader reader = new BufferedReader(new InputStreamReader(fileInputStream, Charset.forName(StandardCharsets.UTF_8.name())));

                int c;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }

                // Starts sending Data packets in parallel
                executeDataSendingThreads(textBuilder, framer, message, encoder);

                // Close the FileInputStream after finishing sending
                fileInputStream.close();
            }
        }

        // Close Input and Output Streams after finish sending
        in.close();
        out.close();

        // If the client socket is close manually, meaning the socket is timed out
        if (socket.isClosed()) {
            throw new SocketTimeoutException("New connection established");
        }
    }

    /**
     * @param textBuilder stringBuilder that contains the data to send
     * @param framer      framer for serialization
     * @param message     received message
     * @param encoder     encoder for encoding the Headers
     */
    public static void executeDataSendingThreads(StringBuilder textBuilder, Framer framer, Message message, Encoder encoder) {

        // Use Executor Service to generate new threads while needed
        ExecutorService executorService = Executors.newCachedThreadPool();

        // Sending Data in parallel
        executorService.execute(new Runnable() {
            public void run() {
                try {
                    synchronized (this) {
                        sendHeaders(message, framer, encoder);
                        sendData(textBuilder, framer, message, encoder);
                    }
                } catch (IOException | BadAttributeException e) {
                    System.err.println(e.getMessage());
                }
            }
        });
        executorService.shutdown();
    }

    /**
     * Sends the header before each Data message
     *
     * @param message message with types
     * @param framer  framer for serialization
     * @param encoder encoder for encoding the header
     * @throws BadAttributeException if attributes invalid
     * @throws IOException           if IO exception occurs
     */
    public static synchronized void sendHeaders(Message message, Framer framer, Encoder encoder) throws BadAttributeException, IOException {
        // Send the Headers to the client indicating there will be data packets to receive
        Headers headers = new Headers(message.getStreamID(), false);
        headers.addValue(":status", "200 Ok");
        framer.putFrame(headers.encode(encoder));
    }

    /**
     * This functions sends the packets to the Client
     *
     * @param textBuilder the data stored into the stringBuilder to send
     * @param framer      framer for serialization
     * @param message     deserialized message objects
     * @param encoder     encoder to encoding the headers
     * @throws BadAttributeException if attributes invalid
     * @throws IOException           if IOException occurs
     */
    public static synchronized void sendData(StringBuilder textBuilder, Framer framer, Message message, Encoder encoder) throws BadAttributeException, IOException {

        // Counter for counting the current packet size
        int sizeCount = 0;

        // Counter for counting the total data length
        int totalLengthCount = 0;

        StringBuilder temp = new StringBuilder();

        // Chop data to separate packets to send
        for (char ch : textBuilder.toString().toCharArray()) {
            totalLengthCount++;

            // If size current packet is less than the MAXDATASIZE, append char to string
            if (sizeCount < MAXDATASIZE) {
                temp.append(ch);
                sizeCount++;
            } else {
                temp.append(ch);

                // If current size reached to MAXDATASIZE, send the packet to client
                framer.putFrame(new Data(message.getStreamID(), false, temp.toString().getBytes()).encode(encoder));

                // Reset the string to write
                temp = new StringBuilder();
                sizeCount = 0;

                // Wait for MINDATAINTERVAL time then continue
                try {
                    Thread.sleep(MINDATAINTERVAL);
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }

            // If this char is the last char in the file, send the packet and finish sending
            if (totalLengthCount == textBuilder.toString().length()) {
                framer.putFrame(new Data(message.getStreamID(), true, temp.toString().getBytes()).encode(encoder));
                break;
            }
        }
    }

    /**
     * This function validates the attributes specifically checking for stream ID of received Headers
     *
     * @param sidList        list of stream ID of Headers
     * @param message        message Object received from Client
     * @param serverThreadID thread ID of server thread
     * @return list of stream ID
     */
    public static List<Integer> receivedHeadersAttributesValidation(List<Integer> sidList, Message message, int serverThreadID, String serverType) {

        // If the stream ID was not received before within the same connection, append the stream ID to the list
        // Otherwise, a duplicated stream ID was received, log the warning message
        if (!sidList.contains(message.getStreamID())) {
            sidList.add(message.getStreamID());
        } else {
            if (serverType.equalsIgnoreCase("regular")) {
                LOGGER.warning("Thread[" + serverThreadID + "]: " + "Duplicate request: " + message);
            } else {
                LOGGER.warning("Duplicate request: " + message);
            }
            return null;
        }

        // Try to create a new Headers only for stream ID validation
        // If BadAttributesException was caught meaning that stream ID is illegal, log the warning message
        try {
            new Headers(message.getStreamID(), false);
        } catch (BadAttributeException e) {
            if (serverType.equalsIgnoreCase("regular")) {
                LOGGER.warning("Thread[" + serverThreadID + "]: " + "Illegal stream ID " + message);
            } else {
                LOGGER.warning("Illegal stream ID " + message);
            }
            return null;
        }

        // If stream ID received passed the above validation, a modified list of stream ID should be returned
        // Otherwise, null is returned
        return sidList;
    }

    /**
     * This function validates the paths requested by Client
     *
     * @param message        message objects sent by Client
     * @param framer         framer for serialization
     * @param encoder        encoder for encoding the Headers
     * @param documentRoot   root of document
     * @param serverThreadID thread ID of server thread
     * @return a file inputStream contains the file to write to
     * @throws BadAttributeException if arguments invalid
     * @throws IOException           if IOException occurs
     */
    public static FileInputStream receivedHeadersPathsValidation(Message message, Framer framer, Encoder encoder, String documentRoot, int serverThreadID, String serverType) throws BadAttributeException, IOException {

        // Get the file to write from the Headers
        String fileName = ((Headers) message).getValue(":path");

        // Set the absolute path to the file to send
        String absolutePath = documentRoot + fileName;

        FileInputStream fileInputStream = null;

        // Try to set up the file input stream with absolute path
        try {
            fileInputStream = new FileInputStream(absolutePath);
        } catch (IOException e) {

            // If exception occurs, we need to send appropriate headers to the client
            Headers headers = new Headers(message.getStreamID(), false);

            // If the requested file is a directory, log and send the headers with error message
            if (Files.isDirectory(Paths.get(absolutePath))) {
                if (serverType.equalsIgnoreCase("regular")) {
                    LOGGER.warning("Thread[" + serverThreadID + "]: " + "Cannot request directory: " + fileName.substring(1));
                } else {
                    LOGGER.warning("Cannot request directory: " + fileName.substring(1));
                }
                headers.addValue(":status", "404 Cannot request directory");

            } else {

                // If no path is given by the client, log and send headers with 404 File not found
                // Otherwise, file is unable to open, log appropriate message
                if (fileName == null) {
                    if (serverType.equalsIgnoreCase("regular")) {
                        LOGGER.warning("Thread[" + serverThreadID + "]: " + "No or bad path");
                    } else {
                        LOGGER.warning("No or bad path");
                    }
                } else {
                    if (serverType.equalsIgnoreCase("regular")) {
                        LOGGER.warning("Thread[" + serverThreadID + "]: " + "File not found: " + fileName.substring(1));
                    } else {
                        LOGGER.warning("File not found: " + fileName.substring(1));
                    }
                }
                headers.addValue(":status", "404 File not found");
            }

            // Send the Headers to the client
            framer.putFrame(headers.encode(encoder));

            return null;
        }

        // If all the above validation is passed, should return a FileInputStream to send
        // Otherwise, null is returned
        return fileInputStream;
    }
}
