package cl.myconstruction.app.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {
    @Test
    void verify_ok_when_password_matches() {
        PasswordHasher.Hash hash = PasswordHasher.hash("Demo1234!");
        assertTrue(PasswordHasher.verify("Demo1234!", hash.hash(), hash.salt()));
    }

    @Test
    void verify_false_when_password_does_not_match() {
        PasswordHasher.Hash hash = PasswordHasher.hash("Demo1234!");
        assertFalse(PasswordHasher.verify("Otro", hash.hash(), hash.salt()));
    }
}

