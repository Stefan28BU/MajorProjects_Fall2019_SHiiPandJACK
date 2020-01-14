/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import shiip.serialization.BadAttributeException;

import static org.junit.jupiter.api.Assertions.*;

public class BadAttributeExceptionTest {
    @Nested
    @DisplayName("Constructor")
    public class BAEConstructor {

        // test getter and setter
        @ParameterizedTest
        @CsvSource({"m1, a1", "m2, a2", "m3, a3", "m4, a4"})
        @DisplayName("Getters and Matching")
        public void badAEConstructorTest1(String message, String attribute) {
            assertEquals(new BadAttributeException(message, attribute).getAttribute(), attribute);
            assertEquals(new BadAttributeException(message, attribute).getMessage(), message);
        }

        // test getCause
        @Test
        @DisplayName("GetCause Matchign")
        public void badAEConstructorTest2() {
            assertEquals(new BadAttributeException("m1", "a1", new NullPointerException()).getCause().getClass(), NullPointerException.class);
        }
    }
}
