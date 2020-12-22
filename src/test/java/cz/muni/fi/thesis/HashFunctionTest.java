package cz.muni.fi.thesis;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class HashFunctionTest {

    @Test(expected = ArithmeticException.class)
    public void gcdIsNotOneTest() {
        new HashFunction(20, 12, 3);
    }

    @Test
    public void hashingTest() {
        HashFunction hf = new HashFunction(9, 2, 5);
        assertThat(hf.hash(0)).isEqualTo(5);
        assertThat(hf.hash(1)).isEqualTo(7);
        assertThat(hf.hash(2)).isEqualTo(0);
        assertThat(hf.hash(10)).isEqualTo(7);

    }
}
