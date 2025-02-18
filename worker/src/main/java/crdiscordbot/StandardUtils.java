package crdiscordbot;

/**
 * Utility class providing standard operations.
 */
public class StandardUtils {

    /**
     * Adds two Integer values if both are non-null.
     *
     * @param x the first Integer value
     * @param y the second Integer value
     * @return the sum of x and y if both are non-null, otherwise returns 0
     */
    public static int addStatic(Integer x, Integer y) {
        if (null != x && null != y) {
            return x + y;
        } else {
            return 0;
        }
    }

}