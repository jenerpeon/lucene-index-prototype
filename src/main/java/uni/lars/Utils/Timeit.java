package uni.lars.Utils;

/**
 * Created by lars on 8/21/16.
 */
public class Timeit {
    public static double code(Runnable block) {
        long start = System.nanoTime();
        try {
            block.run();
        } finally {
            double res = ((System.nanoTime() - start)/ (1.0e9));

            System.out.println("Time taken(s): " + res);
            return res;
        }
    }
}
