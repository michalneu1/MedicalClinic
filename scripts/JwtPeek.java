import java.nio.charset.StandardCharsets;
import java.util.Base64;

// Prints the header and payload of a JWT issued by the clinic. Usage: java scripts/JwtPeek.java "$TOKEN"
public class JwtPeek {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("usage: java scripts/JwtPeek.java <token>");
            System.exit(1);
        }
        String[] parts = args[0].split("\\.");
        if (parts.length != 3) {
            System.err.println("not a JWT: expected 3 dot-separated parts, got " + parts.length);
            System.exit(1);
        }
        System.out.println("header:  " + decode(parts[0]));
        System.out.println("payload: " + decode(parts[1]));
    }

    private static String decode(String part) {
        return new String(Base64.getUrlDecoder().decode(part), StandardCharsets.UTF_8);
    }
}
