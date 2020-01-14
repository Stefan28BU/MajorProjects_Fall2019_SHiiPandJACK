/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization.test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import shiip.serialization.BadAttributeException;
import shiip.serialization.Data;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class DataTest {

    @Nested
    @DisplayName("Constructor")
    public class DataConstructor {

        // test invalid stream ID
        @Test
        @DisplayName("Invalid Stream ID")
        public void constructorInvalidSIDTest() {
            assertThrows(BadAttributeException.class, () -> new Data(-1, false, new byte[]{1, 0, 2, 0, 0}));
        }

        // test thrown exception while data is null
        @Test
        @DisplayName("Invalid Data")
        public void constructorInvalidDataTest() {
            assertThrows(BadAttributeException.class, () -> new Data(1, false, null));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    public class DataEqualsAndHashCode {

        // test equals function
        @ParameterizedTest
        @CsvSource({"1,1", "12,12", "3,3", "9,9"})
        @DisplayName("Same Data")
        void dataEqualsTest(String id1, String id2) throws BadAttributeException {
            assertTrue(new Data(Integer.parseInt(id1), true, new byte[]{0, 0, 0})
                    .equals(new Data(Integer.parseInt(id2), true, new byte[]{0, 0, 0})));
        }

        // test equals function with different data
        @ParameterizedTest
        @CsvSource({"1,1", "2,2", "3,3", "12,12"})
        @DisplayName("Different Data")
        void dataNotEqualDiffDataTest(String id1, String id2) throws BadAttributeException {
            assertFalse(new Data(Integer.parseInt(id1), true, new byte[]{0, 0, 0, 0})
                    .equals(new Data(Integer.parseInt(id2), true, new byte[]{0, 0, 0})));
        }

        // test isend
        @ParameterizedTest
        @CsvSource({"10,10", "21,21", "23,23", "122,122"})
        @DisplayName("Different IsEnd")
        void dataNotEqualDiffIsEndTest(String id1, String id2) throws BadAttributeException {
            assertFalse(new Data(Integer.parseInt(id1), false, new byte[]{0, 0, 0})
                    .equals(new Data(Integer.parseInt(id2), true, new byte[]{0, 0, 0})));
        }

        // test different stream ID
        @ParameterizedTest
        @CsvSource({"1,10", "2,21", "2,23", "12,122"})
        @DisplayName("Different Stream ID")
        void dataNotEqualDiffSIDTest(String id1, String id2) throws BadAttributeException {
            assertFalse(new Data(Integer.parseInt(id1), true, new byte[]{0, 0, 0})
                    .equals(new Data(Integer.parseInt(id2), true, new byte[]{0, 0, 0})));
        }

        // test everything diffrernet
        @ParameterizedTest
        @CsvSource({"12,10", "29,21", "2,23", "2,122"})
        @DisplayName("Everything Different")
        void dataNotEqualAllDiffTest(String id1, String id2) throws BadAttributeException {
            assertFalse(new Data(Integer.parseInt(id1), false, new byte[]{0, 0, 0, 0})
                    .equals(new Data(Integer.parseInt(id2), true, new byte[]{0, 0, 0})));
        }

        // same for hashcode
        @ParameterizedTest
        @CsvSource({"1,1", "2,2", "3,3", "12,12", "3, 3"})
        @DisplayName("Hash Code Equal")
        void dataHCEqualsTest(String id1, String id2) throws BadAttributeException {
            assertEquals(new Data(Integer.parseInt(id1), true, new byte[]{0, 0, 0})
                    .hashCode(), new Data(Integer.parseInt(id2), true, new byte[]{0, 0, 0}).hashCode());
        }

        // same for hashcode
        @ParameterizedTest
        @CsvSource({"1,1", "2,2", "3,3", "12,12", "3, 3"})
        @DisplayName("Hash Code Not Equal")
        void dataHCNotEqualDiffDataTest(String id1, String id2) throws BadAttributeException {
            assertNotEquals(new Data(Integer.parseInt(id1), true, new byte[]{0, 0, 0})
                    .hashCode(), new Data(Integer.parseInt(id2), true, new byte[]{0, 0, 1}).hashCode());
        }

        // same for hashcode
        @ParameterizedTest
        @CsvSource({"1,1", "2,2", "3,3", "12,12", "3, 3"})
        @DisplayName("Hash Code Different IsEnd")
        void dataHCNotEqualDiffIsEndTest(String id1, String id2) throws BadAttributeException {
            assertNotEquals(new Data(Integer.parseInt(id1), true, new byte[]{0, 0, 0})
                    .hashCode(), new Data(Integer.parseInt(id2), false, new byte[]{0, 0, 0}).hashCode());
        }

        // same for hashcode
        @ParameterizedTest
        @CsvSource({"1,12", "23,2", "311,3", "122,12", "2, 1"})
        @DisplayName("Hash Code Different Stream ID")
        void dataHCNotEqualDiffSIDTest(String id1, String id2) throws BadAttributeException {
            assertNotEquals(new Data(Integer.parseInt(id1), true, new byte[]{0, 0, 0})
                    .hashCode(), new Data(Integer.parseInt(id2), true, new byte[]{0, 0, 0}).hashCode());
        }

        // same for hashcode
        @ParameterizedTest
        @CsvSource({"1,12", "32,2", "321,3", "122,12", "2, 3"})
        @DisplayName("Hash Code All Different")
        void dataHCNotEqualAllDiffTest(String id1, String id2) throws BadAttributeException {
            assertNotEquals(new Data(Integer.parseInt(id1), false, new byte[]{0, 0, 0, 0})
                    .hashCode(), new Data(Integer.parseInt(id2), true, new byte[]{0, 0, 0}).hashCode());
        }
    }

    @Nested
    @DisplayName("Setters and Getters")
    public class DataSettersAndGetters {

        // test getdata matching
        @Test
        @DisplayName("Data Matching")
        void dataGetDataEqualsTest() throws BadAttributeException {
            assertArrayEquals(new Data(1, false, new byte[]{0, 0, 1}).getData(),
                    new Data(1, true, new byte[]{0, 0, 1}).getData());
        }

        // test getdata with different value
        @Test
        @DisplayName("Data Different")
        void dataGetDataNotEqualTest() throws BadAttributeException {
            assertFalse(Arrays.equals(new Data(1, false, new byte[]{0, 0, 2}).getData(),
                    new Data(1, true, new byte[]{0, 0, 1}).getData()));
        }

        // test is end
        @Test
        @DisplayName("Same IsEnd")
        void dataIsEndEqualsTest() throws BadAttributeException {
            assertEquals(new Data(1, false, new byte[]{0, 0, 2}).isEnd(),
                    new Data(1, false, new byte[]{0, 0, 1}).isEnd());
        }

        // test isend with different end value
        @Test
        @DisplayName("Different IsEnd")
        void dataIsEndNotEqualTest() throws BadAttributeException {
            assertNotEquals(new Data(1, false, new byte[]{0, 0, 2}).isEnd(),
                    new Data(1, true, new byte[]{0, 0, 1}).isEnd());
        }

        // setend and isend matching
        @Test
        @DisplayName("IsEnd Matching")
        void dataSetEndIsEndTest() throws BadAttributeException {
            var data = new Data(1, false, new byte[]{0, 0, 1});

            data.setEnd(true);

            assertTrue(data.isEnd());
        }

        // set data and get data matching
        @Test
        @DisplayName("Setter and Getter Match")
        void dataSetDataGetDataTest() throws BadAttributeException {
            var data = new Data(1, false, new byte[]{0, 0, 1});

            data.setData(new byte[]{0, 1, 2, 3, 4, 5});

            assertArrayEquals(data.getData(), new byte[]{0, 1, 2, 3, 4, 5});
        }
    }

    @Nested
    @DisplayName("ToString")
    public class DataToString {

        // toString working
        @Test
        @DisplayName("Test ToString")
        public void dataToStringIsSameTest() throws BadAttributeException {
            var data = new Data(1, false, new byte[]{0, 0, 1});

            assertEquals("Data: StreamID=1 isEnd=false data=3", data.toString());
        }
    }

    @Nested
    @DisplayName("Data Validation")
    public class DataValidation {

        // not the last data test
        @Test
        @DisplayName("Not the End")
        public void dataNotEndTest() throws BadAttributeException {
            var data = new Data(1, false, new byte[]{0, 0, 1});
            var encoded = data.encode(null);

            var end = true;

            if ((byte) ((encoded[1]) & 1) == 0x0) {
                end = false;
            }

            assertFalse(end);
        }

        // the end data test
        @Test
        @DisplayName("End of Data Message")
        public void dataIsEndTest() throws BadAttributeException {
            var data = new Data(1, true, new byte[]{0, 0, 1});
            var encoded = data.encode(null);

            var end = false;

            if ((byte) ((encoded[1]) & 1) == 0x1) {
                end = true;
            }

            assertTrue(end);
        }

        // decoded end data
        @Test
        @DisplayName("Decode End Data")
        public void dataIsEndDecodeTest() throws BadAttributeException {
            var data = new Data(1, true, new byte[]{0, 0, 1});
            var encoded = data.encode(null);
            var decoded = Data.decodeData(encoded);

            assertTrue(decoded.isEnd());
        }

        // decoded not end data
        @Test
        @DisplayName("Decode Non End Data")
        public void dataNotEndDecodeTest() throws BadAttributeException {
            var data = new Data(1, false, new byte[]{0, 0, 1});
            var encoded = data.encode(null);
            var decoded = Data.decodeData(encoded);

            assertFalse(decoded.isEnd());
        }

        // original and decoded data matching
        @Test
        @DisplayName("Data Matching")
        public void dataSameTest() throws BadAttributeException {
            var data = new Data(1, true, new byte[]{0, 0, 1, 9, 9, 8});

            var encoded = data.encode(null);

            var decodedData = Data.decode(encoded, null);

            assertTrue(data.equals(decodedData));
            assertEquals(data.hashCode(), decodedData.hashCode());
        }

        // bad flags for data exception
        @ParameterizedTest
        @ValueSource(bytes = {8, 9, 25, 10, 31})
        @DisplayName("Bad Flags for Data")
        public void badFlagTest(byte b) {
            assertThrows(BadAttributeException.class, () -> Data.decode(new byte[]{0, b, 0, 0, 0, 1, 0}, null));
        }

        // good flags for data
        @ParameterizedTest
        @ValueSource(bytes = {7, 32, 1, 0, 39})
        @DisplayName("Good Flags")
        public void goodFlagTest(byte b) {
            assertDoesNotThrow(() -> Data.decode(new byte[]{0, b, 0, 0, 0, 1, 0}, null));
        }

        // invalid stream ID passed to decode
        @Test
        @DisplayName("Invalid Stream ID")
        public void invalidSIDDecodeTest() throws BadAttributeException {
            assertThrows(BadAttributeException.class, () -> Data.decode(new byte[]{0, 1, 0, 0, 0, 0, 0}, null));
        }

        // decoded end data is end
        @Test
        @DisplayName("Decoded End Data")
        public void decodedIsEndTest() throws BadAttributeException {
            assertTrue(Data.decodeData(new byte[]{0, 1, 0, 0, 0, 1, 0}).isEnd());
        }
    }
}
