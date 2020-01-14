/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 5
 * Class: CSI 4321
 *
 ************************************************/

package jack.server;

import jack.serialization.*;
import jack.serialization.Error;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static jack.client.Client.MAXPACKETSIZE;

/**
 * ServerHandler Class
 *
 * @author Yufan Xu
 * @version 1.2
 */
public class ServerHandler {

    private static List<String> serverList = new ArrayList<>();

    /**
     * This function creates a new service if the list does not have it
     *
     * @param receivedPayload the received payload in String form
     */
    private static void createNewService(String receivedPayload) {
        // If the New service is not in the list, adds to the list
        boolean duplicatePayload = false;
        for (var payload : serverList) {
            if (payload.equals(receivedPayload)) {
                duplicatePayload = true;
                break;
            }
        }
        if (!duplicatePayload) {
            serverList.add(receivedPayload);
        }
    }

    /**
     * Trim the receive buffer, store the data
     *
     * @param receivePacket received DatagramPacket
     * @return a StringBuilder that contains the received data
     */
    private static StringBuilder trimReceivedData(DatagramPacket receivePacket) {
        StringBuilder receivePacketData = new StringBuilder();

        // Store the received buffer without the empty bytes
        byte[] receivedBuffer = receivePacket.getData();
        for (byte b : receivedBuffer) {
            if (b != 0) {
                receivePacketData.append((char) b);
            }
        }
        return receivePacketData;
    }

    /**
     * Creates a new Response to send to the Client
     *
     * @param searchString search string sent by the client
     * @return a new Response
     */
    private static Response createResponse(String searchString) {
        Response response = new Response();

        // For the matching payload, add the service to the service list
        for (var payload : serverList) {
            String[] pair = payload.split(":");

            // If Q had payload of '*', add all services
            if (searchString.equals("*")) {
                response.addService(pair[0], Integer.parseInt(pair[1]));
            } else {
                if (pair[0].contains(searchString)) {
                    response.addService(pair[0], Integer.parseInt(pair[1]));
                }
            }
        }
        return response;
    }

    /**
     * Starts handling events for the server
     *
     * @param LOGGER server's logger for information display
     * @param socket DatagramSocket for packet transmit
     * @throws IOException if IO exception occurs
     */
    public static void start(Logger LOGGER, DatagramSocket socket) throws IOException {
        byte[] sendBuffer = null;

        // The server should run forever
        while (true) {

            // Initialize the receive packet every iteration
            byte[] tempBuffer = new byte[MAXPACKETSIZE];
            DatagramPacket receivePacket = new DatagramPacket(tempBuffer, MAXPACKETSIZE);

            // Receive receivePacket from client
            socket.receive(receivePacket);

            // Stores the received buffer without the empty padding
            StringBuilder receivePacketData = trimReceivedData(receivePacket);

            Message receivedMessage = null;

            // Decode the received message
            try {
                receivedMessage = Message.decode(receivePacketData.toString().getBytes());
            } catch (IllegalArgumentException e) {
                var errorMessage = "Invalid message: " + e.getMessage();
                LOGGER.warning(errorMessage);

                // Send an new Error packet to the Client
                sendBuffer = new Error(errorMessage).encode();
                socket.send(new DatagramPacket(sendBuffer, sendBuffer.length, receivePacket.getAddress(), receivePacket.getPort()));
            }

            // Handling events and actions for different received messages
            // Client shouldn't be sending R, A, and E
            if (receivedMessage instanceof Response ||
                    receivedMessage instanceof ACK ||
                    receivedMessage instanceof jack.serialization.Error) {
                var errorMessage = "Unexpected message type: " + receivedMessage;
                LOGGER.warning(errorMessage);

                // Send an new Error packet to the Client
                sendBuffer = new Error(errorMessage).encode();
                socket.send(new DatagramPacket(sendBuffer, sendBuffer.length, receivePacket.getAddress(), receivePacket.getPort()));

            } else if (receivedMessage instanceof Query) {
                LOGGER.info("Received Query: " + receivedMessage);
                String searchString = ((Query) receivedMessage).getSearchString();

                // Creates a new Response to send to the Client
                var response = createResponse(searchString);

                // Send an new Response to the client
                sendBuffer = response.encode();
                socket.send(new DatagramPacket(sendBuffer, sendBuffer.length, receivePacket.getAddress(), receivePacket.getPort()));

            } else if (receivedMessage instanceof New) {
                LOGGER.info("Received New: " + receivedMessage);

                String receivedPayload = ((New) receivedMessage).getHost() + ":" + ((New) receivedMessage).getPort();

                // Creates a new service
                createNewService(receivedPayload);

                // Send an new ACK to the client
                sendBuffer = new ACK(((New) receivedMessage).getHost(), ((New) receivedMessage).getPort()).encode();
                socket.send(new DatagramPacket(sendBuffer, sendBuffer.length, receivePacket.getAddress(), receivePacket.getPort()));
            }

            // Reset length to avoid shrinking buffer
            receivePacket.setLength(MAXPACKETSIZE);
        }
    }
}
