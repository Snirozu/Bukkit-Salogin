package snirozu.salogin;

import com.password4j.HashChecker;
import com.password4j.Password;

public class Hash {
    public static Boolean compare(CharSequence plainTextPassword, String hash) {
        HashChecker checker = Password.check(plainTextPassword, hash);
        switch (Salogin.instance.getConfig().getString("hashing-algorithm")) {
            case "bcrypt":
                return checker.withBcrypt();
            case "argon2":
                return checker.withArgon2();
            case "compressedpbkdf2":
                return checker.withCompressedPBKDF2();
            case "md":
                return checker.withMessageDigest();
            case "pbkdf2":
                return checker.withPBKDF2();
            case "scrypt":
                return checker.withScrypt();
        }
        return null;
    }
}
