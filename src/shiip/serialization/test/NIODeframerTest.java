/************************************************
 *
 * Author: Yufan Xu
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/

package shiip.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shiip.serialization.NIODeframer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * This class specifically tests NIODeframer class
 *
 * @author Yufan Xu
 */
public class NIODeframerTest {

    @Test
    @DisplayName("Test deframer")
    public void test() {
        NIODeframer framer = new NIODeframer();
        assertNull(framer.getFrame(new byte[] { 0 , 0, 1}));
        assertNull(framer.getFrame(new byte[] { 0, 0, 0, 0, 0, 0}));
        assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 'a' }, framer.getFrame(new byte[] { 'a' }));
    }
}
