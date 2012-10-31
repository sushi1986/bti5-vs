import java.math.BigInteger;
import java.util.Random;


public class RandomTest {
	public static void main(String[] args) {
		for(int i = 0; i < 3; i++)
			System.out.println(new BigInteger(64, new Random()));
	}
}
