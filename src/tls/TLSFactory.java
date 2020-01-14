package tls;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TLSFactory {
    // Specify TLS version to use
    private static final String TLSVERSION = "TLS";
    // Specify ALPN protocol to negotiate
    private static final String HTTP2PROTOCOL = "h2";

    /**
     * Gets a TLS client socket for HTTP2
     *
     * @param server identity of host to connect to
     * @param port port of host to connect to
     *
     * @return connected/initialized socket
     *
     * @throws Exception if connection/initialization fails
     */
    public static Socket getClientSocket(String server, int port) throws Exception {
        // Set very trusting trust manager
        final SSLContext ctx = SSLContext.getInstance(TLSVERSION);
        ctx.init(null, new TrustManager[] { new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        } }, null);
        // Create SSL socket factory and generate new, connected socket
        final SSLSocketFactory ssf = ctx.getSocketFactory();
        final SSLSocket s = (SSLSocket) ssf.createSocket(server, port);
        // Select application (HTTP2) protocol
        final SSLParameters p = s.getSSLParameters();
        p.setApplicationProtocols(new String[] { HTTP2PROTOCOL });
        s.setSSLParameters(p);

        // Execute TLS connection
        s.startHandshake();

        return s;
    }

    /**
     * Create initialized listening socket
     *
     * @param port             port to listen on
     * @param keystorefile     name of key store file
     * @param keystorepassword password for key store file
     *
     * @return initialized server socket
     *
     * @throws Exception if unable to create socket
     */
    public static ServerSocket getServerListeningSocket(final int port, final String keystorefile,
                                                        final String keystorepassword) throws Exception {
        // Set the keystore and its password
        System.setProperty("javax.net.ssl.keyStorePassword", keystorepassword);
        System.setProperty("javax.net.ssl.keyStore", keystorefile);
        // Create a server-side SSLSocket
        SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        SSLServerSocket servSocket = (SSLServerSocket) factory.createServerSocket(port);
        // servSocket.setEnabledProtocols(new String[] { TLSVERSION });

        return servSocket;
    }

    /**
     * Block until connection then return new, connected socket
     *
     * @param servSocket socket waiting on connections
     *
     * @return connected socket
     *
     * @throws IOException if problem handling new connection
     */
    public static Socket getServerConnectedSocket(final ServerSocket servSocket) throws IOException {
        SSLSocket socket = (SSLSocket) servSocket.accept();

        // Get an SSLParameters object from the SSLSocket
        SSLParameters sslp = socket.getSSLParameters();

        // Populate SSLParameters with the ALPN values
        // As this is server side, put them in order of preference
        sslp.setApplicationProtocols(new String[] { HTTP2PROTOCOL });

        // Populate the SSLSocket object with the ALPN values
        socket.setSSLParameters(sslp);

        // Make TLS handshake
        socket.startHandshake();

        return socket;
    }
}