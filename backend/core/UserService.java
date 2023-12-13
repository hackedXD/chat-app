package core;

import util.JSON;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

public class UserService {

    private static User head; // doubly linked circular linked list
    private static Key privateKey;
    private static Mac mac;

    static {
        try {
            privateKey = KeyGenerator.getInstance("HmacSHA256").generateKey();

            mac = Mac.getInstance("HmacSHA256");
            mac.init(privateKey);
        } catch (Exception ignored) {}
    }

    public static User addUser(String name, String email, String hashedPassword) {
        User newUser = new User(name, email, hashedPassword);

        if (head == null) {
            head = newUser;
            head.next = head;
            head.prev = head;
        } else {
            head.next.prev = newUser;
            newUser.next = head.next;
            newUser.prev = head;
            head.next = newUser;
            head = newUser;
        }

        return newUser;
    }

    public static User findUser(String username) {
        return findUserAround(username, head);
    }

    public static User findUserAround(String username, User main) {
        if (main == null) {
            return null;
        }

        if (main.getUsername().equals(username)) {
            return main;
        }

        User right = main.next;
        User left = main.prev;

        while (right != left) {
            if (right.getUsername().equals(username)) {
                return right;
            }

            if (left.getUsername().equals(username)) {
                return left;
            }

            right = right.next;
            left = left.prev;
        }

        if (right == null) return null;
        if (!right.getUsername().equals(username)) return null;

        return right;
    }



    public static Map<String, Object> verifyJWT(String jwt) {
        String[] parts = jwt.split("\\.");

        if (parts.length != 3) {
            return null;
        }

        byte[] signature = Base64.getUrlDecoder().decode(parts[2]);
        byte[] selfComputedSignature = mac.doFinal((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));

        if (!Arrays.equals(signature, selfComputedSignature)) {
            return null;
        }

        byte[] payload = Base64.getUrlDecoder().decode(parts[1]);

        return JSON.parseJSON(new String(payload));
    }

    public static String generateJWT(User user) {
        try {
            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();


            String header = encoder.encodeToString(
                    JSON.createJSON(
                        Map.of(
                                "alg", "HS256",
                                "typ", "JWT"
                        )
                    ).getBytes(StandardCharsets.UTF_8)
            );

            String payload = encoder.encodeToString(
                    JSON.createJSON(
                            Map.of(
                                    "sub", user.getUsername(),
                                    "iat", System.currentTimeMillis(),
                                    "exp", System.currentTimeMillis() + 1000 * 60 * 60 * 3
                            )
                    ).getBytes(StandardCharsets.UTF_8)
            );

            String signature = encoder.encodeToString(
                    mac.doFinal((header + "." + payload).getBytes(StandardCharsets.UTF_8))
            );

            return header + "." + payload + "." + signature;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
