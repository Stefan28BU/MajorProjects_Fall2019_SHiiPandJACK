/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 6
 * Class: CSI 4321
 *
 ************************************************/

package shiip.server;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import shiip.serialization.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static shiip.client.Client.*;

/**
 * Server Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class ServerAIO {

    /**
     * Logger for server
     */
    public final static Logger LOGGER = Logger.getLogger(ServerAIO.class.getName());

    /**
     * Port number
     */
    private static int port;

    /**
     * Root of documents to send
     */
    private static String documentRoot;

    /**
     * List of stream ID for checking the duplicates
     */
    private static List<Integer> sidList;

    /**
     * Buffer size (bytes)
     */
    private static final int BUFSIZE = 256;

    /**
     * Validates the command line arguments and store to variables
     *
     * @param args command lind arguments
     */
    private static void argumentsValidation(String[] args) {

        // Only 2 command line inputs is valid
        if (args.length != 2) {
            throw new IllegalArgumentException("Parameter(s): <port> <document root>");
        }
        documentRoot = args[1];

        // If user input invalid numbers for port or number of threads, set default server port and thread number
        // If user failed to input integers for port and number of threads, terminate server
        try {
            port = Integer.parseInt(args[0]);

            if (port <= 0) {
                throw new IllegalArgumentException("Bad port");
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
        }
    }

    /**
     * Main function for ServerAIO
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        // Validates command line inputs
        argumentsValidation(args);

        // Initialize logger and logger file
        initializeLoggerHandler();

        // Check for IO exception and InterruptedException while running
        try {

            // Establish server socket channel
            AsynchronousServerSocketChannel listenChannel = AsynchronousServerSocketChannel.open();

            // Bind local port
            listenChannel.bind(new InetSocketAddress(port));

            // Create accept handler
            listenChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

                @Override
                public void completed(AsynchronousSocketChannel clntChan, Void attachment) {
                    listenChannel.accept(null, this);
                    try {
                        handleAccept(clntChan);
                    } catch (IOException e) {
                        failed(e, null);
                    }
                }

                @Override
                public void failed(Throwable e, Void attachment) {
                    LOGGER.log(Level.WARNING, "Close Failed", e);
                }
            });

            // Block until current thread dies
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Server Interrupted", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "I/O exception occurred", e);
        }
    }

    /**
     * Called after each accept completion
     *
     * @param clntChan channel of new client
     * @throws IOException if I/O problem
     */
    public static void handleAccept(final AsynchronousSocketChannel clntChan) throws IOException {

        // Allocate buffer for reading
        ByteBuffer buf = ByteBuffer.allocateDirect(BUFSIZE);

        // Initialize stream ID list
        sidList = new ArrayList<>();

        // Create read handler
        clntChan.read(buf, buf, new CompletionHandler<Integer, ByteBuffer>() {
            public void completed(Integer bytesRead, ByteBuffer buf) {

                // Reset buffer pointer position
                buf.rewind();

                // Get the preface
                byte[] prefaceBytes = new byte[PREFACELENGTH];
                buf.get(prefaceBytes);

                // Get the remaining buffer
                byte[] remaining = new byte[buf.remaining()];
                buf.get(remaining);

                // Validate preface
                String prefaceStr = new String(prefaceBytes);

                if (!prefaceStr.equals(PREFACESTR)) {
                    LOGGER.warning("Bad Preface: " + Arrays.toString(prefaceBytes));
                }

                // Pass the buffer without the preface and start handling read buffer
                try {
                    ByteBuffer noPreface = ByteBuffer.wrap(remaining);
                    handleRead(clntChan, noPreface, bytesRead);
                } catch (IOException | BadAttributeException e) {
                    if (e instanceof IOException) {
                        LOGGER.warning("Unable to parse: " + e.getMessage());
                    }
                }
            }

            public void failed(Throwable ex, ByteBuffer v) {
                try {
                    clntChan.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Close Failed", e);
                }
            }
        });
    }

    /**
     * Called after each read completion
     *
     * @param clntChan channel of new client
     * @param buf      byte buffer used in read
     * @throws IOException if I/O problem
     */
    public static void handleRead(final AsynchronousSocketChannel clntChan, ByteBuffer buf, int bytesRead) throws IOException, BadAttributeException {
        if (bytesRead == -1) {
            clntChan.close();
        } else if (bytesRead > 0) {

            // Reset buffer pointer
            buf.rewind();

            // For checking if deframer returned null
            boolean isNull = false;

            // Store the buffer bytes
            byte[] receivedMessageBytes = new byte[buf.remaining()];
            buf.get(receivedMessageBytes);

            // Set up Framer and Deframer
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Framer framer = new Framer(out);
            NIODeframer nioDeframer = new NIODeframer(receivedMessageBytes);

            // Set up De/Encoder
            Decoder decoder = new Decoder(FIXEDMAXHEADERWINDOWSIZE, FIXEDMAXHEADERWINDOWSIZE);
            Encoder encoder = new Encoder(FIXEDMAXHEADERWINDOWSIZE);

            Message message = null;

            // Start decoding the received Message
            // Check for null
            try {
                message = Message.decode(nioDeframer.getFrame(receivedMessageBytes), decoder);
            } catch (Exception e) {
                if (e instanceof BadAttributeException) {
                    LOGGER.warning("Invalid message: " + e.getMessage());
                }
            }

            // Log appropriate message for non-Headers received messages
            // If Headers is received, start handling Headers
            if (message instanceof Data) {
                LOGGER.warning("Unexpected message: " + message);
            } else if (message instanceof Settings | message instanceof Window_Update) {
                LOGGER.info("Received message: " + message);
            } else if (message instanceof Headers) {
                FileInputStream fileInputStream = null;

                boolean proceed = true;

                // Check for null values, if passed the check then proceed
                try {
                    // Validates for received Headers, in invalid then skip sending Data packets
                    Objects.requireNonNull(fileInputStream = ServerHandler.receivedHeadersPathsValidation(message, framer, encoder, documentRoot, 0, "AIO"));
                    Objects.requireNonNull(sidList = ServerHandler.receivedHeadersAttributesValidation(sidList, message, 0, "AIO"));
                } catch (NullPointerException e) {
                    proceed = false;
                }

                // If proceed, send the requested Data back to the Client
                if (proceed) {
                    // Store the data in file to a String Builder
                    StringBuilder textBuilder = new StringBuilder();
                    Reader reader = new BufferedReader(new InputStreamReader(fileInputStream, Charset.forName(StandardCharsets.UTF_8.name())));

                    int c;
                    while ((c = reader.read()) != -1) {
                        textBuilder.append((char) c);
                    }

                    // Starts sending Data packets in parallel
                    ServerHandler.sendHeaders(message, framer, encoder);
                    ServerHandler.sendData(textBuilder, framer, message, encoder);

                    // Close the FileInputStream after finishing sending
                    fileInputStream.close();
                }
            }

            // Starts writing data
            if (message instanceof Headers) {
                ByteBuffer sendBuffer = ByteBuffer.allocate(out.size());
                sendBuffer.put(out.toByteArray());
                sendBuffer.flip();

                writeToSocket(clntChan, sendBuffer);
            } else {
                buf.clear();
                readFromSocket(clntChan, buf);
            }
        }
    }

    /**
     * This function handles the write operation
     *
     * @param clntChan client channel
     * @param buf      buffer to write
     */
    public static void writeToSocket(AsynchronousSocketChannel clntChan, ByteBuffer buf) {
        clntChan.write(buf, buf, new CompletionHandler<Integer, ByteBuffer>() {
            public void completed(Integer bytesWritten, ByteBuffer buf) {
                try {
                    handleWrite(clntChan, buf);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Handle Write Failed", e);
                }
            }

            public void failed(Throwable ex, ByteBuffer buf) {
                try {
                    clntChan.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Close Failed", e);
                }
            }
        });
    }

    /**
     * This function handles the read operation
     *
     * @param clntChan client channel
     * @param buf      buffer to read
     */
    public static void readFromSocket(AsynchronousSocketChannel clntChan, ByteBuffer buf) {
        clntChan.read(buf, buf, new CompletionHandler<Integer, ByteBuffer>() {
            public void completed(Integer bytesRead, ByteBuffer buf) {
                try {
                    handleRead(clntChan, buf, bytesRead);
                } catch (IOException | BadAttributeException e) {
                    if (e instanceof IOException) {
                        LOGGER.warning("Unable to parse: " + e.getMessage());
                    }
                }
            }

            public void failed(Throwable ex, ByteBuffer v) {
                try {
                    clntChan.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Close Failed", e);
                }
            }
        });
    }

    /**
     * Called after each write
     *
     * @param clntChan channel of new client
     * @param buf      byte buffer used in write
     * @throws IOException if I/O problem
     */
    public static void handleWrite(final AsynchronousSocketChannel clntChan, ByteBuffer buf) throws IOException {
        if (buf.hasRemaining()) {
            writeToSocket(clntChan, buf);
        } else {
            buf.clear();
            readFromSocket(clntChan, buf);
        }
    }
}