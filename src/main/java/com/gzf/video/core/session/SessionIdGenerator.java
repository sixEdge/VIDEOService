package com.gzf.video.core.session;

import javax.management.RuntimeErrorException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SessionIdGenerator {

    private static final int SESSION_ID_LENGTH = 16;

    private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";

    private final Queue<SecureRandom> secureRandoms = new ConcurrentLinkedQueue<>();


    public String generateSessionId() {

        byte random[] = new byte[16];

        // Render the result as a String of hexadecimal digits
        // Start with enough space for sessionIdLength and medium route size
        StringBuilder buffer = new StringBuilder(2 * SESSION_ID_LENGTH + 20);

        int resultLenBytes = 0;

        while (resultLenBytes < SESSION_ID_LENGTH) {
            getRandomBytes(random);
            for (int j = 0;
                 j < random.length && resultLenBytes < SESSION_ID_LENGTH;
                 j++) {
                byte b1 = (byte) ((random[j] & 0xf0) >> 4);
                byte b2 = (byte) (random[j] & 0x0f);
                if (b1 < 10)
                    buffer.append((char) ('0' + b1));
                else
                    buffer.append((char) ('A' + (b1 - 10)));
                if (b2 < 10)
                    buffer.append((char) ('0' + b2));
                else
                    buffer.append((char) ('A' + (b2 - 10)));
                resultLenBytes++;
            }
        }

        return buffer.toString();
    }

    private void getRandomBytes(final byte bytes[]) {
        SecureRandom random = secureRandoms.poll();
        if (random == null) {
            random = createSecureRandom();
        }
        random.nextBytes(bytes);
        secureRandoms.add(random);
    }

    private SecureRandom createSecureRandom() {
        try {
            return SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeErrorException(new Error(e));
        }
    }
}
