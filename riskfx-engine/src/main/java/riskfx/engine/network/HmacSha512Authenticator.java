package riskfx.engine.network;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class HmacSha512Authenticator {

	public static record Challenge(byte[] nonce, byte[] salt, byte[] actualDigest) {}
	
	/**
	   * We do not expire nonce values, because they are long-lived we use a larger value (rather than
	   * say a more minimal 8 bytes).
	   * https://security.stackexchange.com/questions/1952/how-long-should-a-random-nonce-be
	   */
	  private static final int LARGE_NONCE_LENGTH = 64;

	  /**
	   * Salt length is chosen to match hash output size. Reference:
	   * https://crackstation.net/hashing-security.htm#salt
	   */
	  private static final int HASH_OUTPUT_SIZE = 64;
	  
	
	public static Challenge newChallenge(final String password) throws GeneralSecurityException {
		final byte[] nonce = encode(newRandomBytes(LARGE_NONCE_LENGTH));
		final byte[] salt = encode(newRandomBytes(HASH_OUTPUT_SIZE));
		final byte[] digest = digest(password, nonce, salt);
		
		return new Challenge(nonce, salt, digest);
	}
	
	
	
	private static byte[] newRandomBytes(final int numberOfBytes) {
		// It is sufficient to use the default non-strong secure PRNG for the platform because the
	    // secrets we are protecting
	    // are short lived.
	    //
	    // https://stackoverflow.com/questions/27622625/securerandom-with-nativeprng-vs-sha1prng
	    final SecureRandom secureRandom = new SecureRandom();

	    final byte[] bytes = new byte[numberOfBytes];
	    secureRandom.nextBytes(bytes);
	    return bytes;
	}



	private static byte[] encode(byte[] bytes) {
		return Base64.getEncoder().encode(bytes);
	}



	public static byte[] digest(String password, byte[] nonce, byte[] salt) throws GeneralSecurityException {

		final Mac mac = Mac.getInstance("HmacSHA512");
		mac.init(newSecretKey(password, salt));
		return mac.doFinal(nonce);
	}

	private static Key newSecretKey(String password, byte[] salt) throws GeneralSecurityException {
		// 20,000 iterations was empirically determined to provide good performance on circa 2014
	    // hardware for
	    // PBKDF2WithHmacSHA512.
	    //
	    // https://security.stackexchange.com/questions/3959/recommended-of-iterations-when-using-pkbdf2-sha256
	    final int iterationCount = 20_000;

	    // Per RFC 4868, the key length for HmacSHA512 should be between 512 bits (L) and 1,024 bits
	    // (B). However, some
	    // references insist that, in the context of current computing power to break crypto, 128 bits
	    // should be
	    // sufficient.
	    //
	    // https://security.stackexchange.com/a/96176/136686
	    final int keyLengthInBits = 512;

	    final SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
	    final KeySpec keySpec =
	        new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLengthInBits);
	    return secretKeyFactory.generateSecret(keySpec);
	}


}
