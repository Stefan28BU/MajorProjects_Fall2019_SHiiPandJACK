/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/

package jack.serialization.test;

import jack.serialization.Message;
import jack.serialization.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static jack.serialization.Message.ENC;
import static jack.serialization.Message.RESPONSEMESSAGE;
import static org.junit.jupiter.api.Assertions.*;

public class ResponseTest {
    @Test
    @DisplayName("ToString")
    public void toStringTest() {
        var response = new Response();

        response.addService("wind", 1111);
        response.addService("google.com", 8080);
        response.addService("fire", 2222);

        assertEquals("RESPONSE [fire:2222][google.com:8080][wind:1111]", response.toString());
    }

    @Test
    @DisplayName("HashCode")
    public void hashCodeTest() {
        var response1 = new Response();
        response1.addService("wind", 1111);
        response1.addService("google.com", 8080);
        response1.addService("fire", 2222);

        var response2 = new Response();
        response2.addService("wind", 1111);
        response2.addService("google.com", 8080);
        response2.addService("fire", 2222);

        assertEquals(response2.hashCode(), response1.hashCode());
    }

    @Nested
    @DisplayName("Constructor")
    public class Constructor {
        @Test
        @DisplayName("Null host")
        public void testNullHost() {
            assertThrows(IllegalArgumentException.class, () -> new Response().addService(null, 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 65536})
        @DisplayName("Invalid port")
        public void testInvalidPort(int port) {
            assertThrows(IllegalArgumentException.class, () -> new Response().addService("host", port));
        }
    }

    @Nested
    @DisplayName("Setters and Getters")
    public class SettersAndGetters {
        @Test
        @DisplayName("Setter and Getter Service List")
        public void servicesSetterGetterTest() {
            var response = new Response();
            response.addService("h1", 1);
            response.addService("h2", 2);
            response.addService("h3", 3);

            var serviceList = new ArrayList<String>();

            serviceList.add("h1:1");
            serviceList.add("h2:2");
            serviceList.add("h3:3");

            assertIterableEquals(serviceList, response.getServiceList());
        }

        @Test
        @DisplayName("Get Operation Code")
        public void getOperationTest() {
            assertEquals(RESPONSEMESSAGE, new Response().getOperation());
        }

        @Test
        @DisplayName("Duplicate service")
        public void duplicateService() {
            var response = new Response();
            response.addService("name", 1);
            response.addService("name", 1);

            assertEquals("RESPONSE [name:1]", response.toString());
        }
    }

    @Nested
    @DisplayName("Encode and Decode")
    public class EncodeAndDecode {
        @Test
        @DisplayName("Encode")
        public void encodeTest() {
            var response = new Response();

            response.addService("wind", 1111);
            response.addService("google.com", 8080);
            response.addService("fire", 2222);

            assertEquals(new String(response.encode()), "R fire:2222 google.com:8080 wind:1111 ");
        }

        @Test
        @DisplayName("Decode")
        public void decodeTest() {
            var response = new Response();

            response.addService("wind", 1111);
            response.addService("google.com", 8080);
            response.addService("fire", 2222);

            assertEquals(Response.decodeResponse("fire:2222 google.com:8080 wind:1111"), response);
        }

        @Test
        @DisplayName("Decode Invalid")
        public void decodeInvalidTest() {
            assertThrows(IllegalArgumentException.class, ()-> Message.decode(new String(new byte[] {65, 32}, StandardCharsets.UTF_16LE).getBytes()));
        }

        @Test
        @DisplayName("Bad Port Number")
        public void badPortTest() {
            assertThrows(IllegalArgumentException.class, () -> Response.decodeResponse("name:808w "));
        }
    }
}
