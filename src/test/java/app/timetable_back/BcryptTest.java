package app.timetable_back;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptTest {

    @Test
    public void testPasswordMatch() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Твой хеш с bcrypt-generator.com
        String hashFromGenerator = "$2a$12$9ac0kvKxGDUIvlS1VRLQledAcU3OYn1jeHdOxOoe5OR/Ne8YyvUDe";

        // Проверяем, совпадает ли пароль с хешем
        boolean matches = encoder.matches("1234", hashFromGenerator);

        System.out.println("Password '1234' matches hash: " + matches);
        assertTrue(matches, "Пароль 1234 должен совпадать с хешем");
    }
}
