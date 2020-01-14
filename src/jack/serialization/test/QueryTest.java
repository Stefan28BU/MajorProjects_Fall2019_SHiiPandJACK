/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/

package jack.serialization.test;

import jack.serialization.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static jack.serialization.Message.QUERYMESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QueryTest {

    @Test
    @DisplayName("ToString")
    public void toStringTest() {
        assertEquals("QUERY search", new Query("search").toString());
    }

    @Test
    @DisplayName("HashCode")
    public void hashCodeTest() {
        assertEquals(new Query("search").hashCode(), new Query("search").hashCode());
    }

    @Nested
    @DisplayName("Constructor")
    public class Constructor {
        @Test
        @DisplayName("Null search string")
        public void testNullSearchString() {
            assertThrows(IllegalArgumentException.class, () -> new Query(null));
        }
    }

    @Nested
    @DisplayName("Setters and Getters")
    public class SettersAndGetters {
        @Test
        @DisplayName("Setter and Getter Query Message")
        public void stringSetterGetterTest() {
            assertEquals("search", new Query("search").getSearchString());
        }

        @Test
        @DisplayName("Get Operation Code")
        public void getOperationTest() {
            assertEquals(QUERYMESSAGE, new Query("search").getOperation());
        }
    }

    @Nested
    @DisplayName("Encode and Decode")
    public class EncodeAndDecode {
        @Test
        @DisplayName("Encode")
        public void encodeTest() {
            assertEquals(new String(new Query("search").encode()), "Q search");
        }

        @Test
        @DisplayName("Decode")
        public void decodeTest() {
            assertEquals(Query.decodeQuery("search"), new Query("search"));
        }
    }
}
