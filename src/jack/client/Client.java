/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/

package jack.client;

import jack.serialization.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.*;
import java.util.Objects;

import static jack.client.MulticastClient.MULTICASTCLIENTID;

/**
 * Client Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class Client {

    // Maximum size of received packet
    public static final int MAXPACKETSIZE = 8192;

    // Timeout for the Client is set to 3 seconds
    private static final int UDPCLIENTTIMEOUT = 3000;

    // Maximum times of retransmit is 3 times
    private static final int MAXRETRANSMIT = 3;

    // Socket for the Client
    private static DatagramSocket socket;

    // Packet to send
    private static DatagramPacket sendPacket;

    // Address of the server
    private static InetAddress serverAddress;

    // Multicast address for receiving;
    private static MulticastSocket multicastSocket;

    /**
     * Validates the command line arguments
     *
     * @param args command line arguments
     */
    public static void parameterValidation(String[] args, String clientType) {

        // Validates multicast arguments and normal arguments
        if (clientType.equalsIgnoreCase(MULTICASTCLIENTID)) {
            if (args.length != 2) {
                System.err.println("Bad parameters: <multicast address> <port>");
                System.exit(1);
            }
        } else {
            // Check if the total length of arguments is 4
            if (args.length != 4) {
                System.err.println("Bad parameters: " + "Incorrect number of inputs");
                System.exit(1);
            }
        }

        // Validates Op and payload
        try {
            Message.hostNameValidation(args[0]);
            Message.portValidation(Integer.parseInt(args[1]));

            if (!clientType.equalsIgnoreCase(MULTICASTCLIENTID)) {
                String message = args[2] + " " + args[3];
                Message.decode(message.getBytes());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Bad parameters: " + e.getMessage());

            System.exit(1);
        }
    }

    /**
     * This handles the Messages received for the Client
     *
     * @param receivedMessage received Message from the server
     * @param args            command line arguments
     */
    public static void clientActionHandler(Message receivedMessage, String[] args, String clientType) {
        boolean terminateClient = false;

        // Check for type of received Message
        if (receivedMessage instanceof Query || receivedMessage instanceof New) {
            System.err.println("Unexpected message type");

            // Prints the message if multicast
            if (clientType.equalsIgnoreCase(MULTICASTCLIENTID)) {
                System.out.println(receivedMessage);
            }
        } else if (receivedMessage instanceof jack.serialization.Error) {
            System.err.println(receivedMessage);

            // Prints the message if multicast
            if (clientType.equalsIgnoreCase(MULTICASTCLIENTID)) {
                System.out.println(receivedMessage);
            }

            terminateClient = true;
        } else if (receivedMessage instanceof Response) {

            // Check if Q is sent to the server
            if (args[2].equals("Q")) {
                System.out.println(receivedMessage);
                terminateClient = true;
            } else {
                System.err.println("Unexpected Response");
            }
        } else if (receivedMessage instanceof ACK) {

            // Check if N is sent to the server
            if (args[2].equals("N")) {
                String[] pair = receivedMessage.toString().split(" ");

                // Check if the received message's payload matches the command line passed payload
                if (pair[1].equals("[" + args[3] + "]")) {
                    System.out.println(receivedMessage);
                    terminateClient = true;
                } else {
                    System.err.println("Unexpected ACK");
                }
            } else {
                System.err.println("Unexpected ACK");
            }
        } else {
            return;
        }

        // Closes the connection to the server and terminate the Client if needed
        if (terminateClient && !clientType.equalsIgnoreCase(MULTICASTCLIENTID)) {
            socket.close();
            System.exit(0);
        }
    }

    /**
     * Initializes variables for the Client to use
     *
     * @param args command line arguments
     * @throws IOException if I/O Exception occurs
     */
    public static void initializeClient(String[] args, String clientType) throws IOException {
        // Validates command line arguments
        parameterValidation(args, clientType);

        // Read the server's address
        serverAddress = InetAddress.getByName(args[0]);

        // Creates sockets for multicast and normal clients
        if (clientType.equalsIgnoreCase(MULTICASTCLIENTID)) {

//            // Test if multicast address
//            if (!serverAddress.isMulticastAddress()) {
//                throw new IllegalArgumentException("Not a multicast address");
//            }

            // Creates a multicast socket and join the multicast group
            multicastSocket = new MulticastSocket(Integer.parseInt(args[1]));
            multicastSocket.joinGroup(serverAddress);
        } else {

            // Creates a normal socket and set the timeout to 3 seconds
            socket = new DatagramSocket();
            socket.setSoTimeout(UDPCLIENTTIMEOUT);

            // Encoded message being passed
            String message = args[2] + " " + args[3];

            // Establish the send and received packet
            sendPacket = new DatagramPacket(message.getBytes(), message.length(), serverAddress, Integer.parseInt(args[1]));
        }
    }


    /**
     * Main function of the Client
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Client.start(args, "normal");
    }

    /**
     * Receive a Message from the server
     *
     * @return the received message
     * @throws IOException if IO exception occurs
     */
    public static Message receiveMessage(String clientType) throws IOException {
        byte[] tempBuffer = new byte[MAXPACKETSIZE];

        // Packet to receive
        DatagramPacket receivePacket = new DatagramPacket(tempBuffer, tempBuffer.length);

        if (clientType.equalsIgnoreCase(MULTICASTCLIENTID)) {
            multicastSocket.receive(receivePacket);
        } else {
            socket.receive(receivePacket);
        }

        byte[] receivedBuffer = receivePacket.getData();
        StringBuilder receivePacketData = new StringBuilder();

        for (byte b : receivedBuffer) {
            if (b != 0) {
                receivePacketData.append((char) b);
            }
        }

        // Reattempt message reception if it was sent from another server
        if (!receivePacket.getAddress().equals(serverAddress)) {
            System.err.println("Unexpected message source: Received packet from an unknown source");
        }

        Message receivedMessage = null;

        // Create a Message from the receive packet and validates the Message
        // Reattempt reception if validation fails
        try {
            receivedMessage = Message.decode(receivePacketData.toString().getBytes());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid message: " + e.getMessage());
        }

        return receivedMessage;
    }

    /**
     * Starts the client protocol
     *
     * @param args       command line arguments
     * @param clientType type of client
     */
    public static void start(String[] args, String clientType) {

        // Terminates the Client if I/O exception occurs
        try {

            // Initialization
            initializeClient(args, clientType);

            // Handling multicast and normal client here
            if (clientType.equalsIgnoreCase(MULTICASTCLIENTID)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

                // Runs if quit is false
                while (true) {
                    // Reading data using readLine
                    String command = reader.readLine();

                    if (command.equalsIgnoreCase("quit")) {
                        break;
                    }
                    try {
                        Message receivedMessage = null;
                        try {
                            receivedMessage = Objects.requireNonNull(receiveMessage(clientType));
                        } catch (NullPointerException e) {
                            continue;
                        }

                        // Start handling receive Message for the Client
                        clientActionHandler(receivedMessage, args, clientType);

                    } catch (InterruptedIOException e) {
                        break;
                    }
                }

                // Leave the multicast group
                multicastSocket.leaveGroup(serverAddress);

                // Close connection
                multicastSocket.close();

            } else {
                // Attempt of retransmit
                int retransmitAttempt = 0;

                // For avoiding send infinite amount of packets
                boolean packetSent = false;

                // Loop will stop when reach to the maximum number of retransmit attempt
                while (retransmitAttempt < MAXRETRANSMIT) {
                    if (!packetSent) {
                        socket.send(sendPacket);
                        packetSent = true;
                    }

                    try {
                        Message receivedMessage = null;
                        try {
                            receivedMessage = Objects.requireNonNull(receiveMessage(clientType));
                        } catch (NullPointerException e) {
                            continue;
                        }

                        // Start handling receive Message for the Client
                        clientActionHandler(receivedMessage, args, clientType);

                    } catch (InterruptedIOException e) {
                        retransmitAttempt += 1;

                        // This opens the gate to re-send the packet
                        packetSent = false;
                    }
                }

                // Closes connection to the server
                socket.close();
            }

        } catch (IOException e) {
            System.err.println("Communication problem: " + e.getMessage());
            if (clientType.equalsIgnoreCase(MULTICASTCLIENTID)) {
                multicastSocket.close();
            } else {
                socket.close();
            }

            System.exit(1);
        }
    }
}
