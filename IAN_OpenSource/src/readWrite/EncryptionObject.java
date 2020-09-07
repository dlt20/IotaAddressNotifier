package readWrite;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionObject {

	// Class bundles and provides enrcpytion and hash based functions
	// Mainly AES-based en/decoding
	
	private String staticAesKeyFragment;
	private String externalAesKeyFragment;
	private String sharedSecret;

	public EncryptionObject(String staticAesKeyFragment, String externalAesKeyFragment) {

		// Set and combine the AES Key parts
		this.staticAesKeyFragment = staticAesKeyFragment;
		this.externalAesKeyFragment = externalAesKeyFragment;
		this.sharedSecret = this.staticAesKeyFragment + this.externalAesKeyFragment;
		
	}

	// AES - Encode message via AES using combined AES key parts
	public String encodeAES(String message) {

		String output = "Encryption failed, see error for details.";

		// Try to encode the String handed to the method
		try {
			byte[] key = (this.sharedSecret).getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			byte[] encrypted = cipher.doFinal(message.getBytes());
			output = new String(Base64.getEncoder().encode(encrypted));
					
		} catch (Exception e) {
			e.printStackTrace();
		}

		return output;
	}

	// AES - Decode message via AES using combined AES key parts
	public String decodeAES(String message) {

		String output = "Decryption failed, see error for details.";

		// Try to decode the String handed to the method
		try {
			byte[] key = (this.sharedSecret).getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
		
			byte[] decoded = Base64.getDecoder().decode(message);
			Cipher cipher2 = Cipher.getInstance("AES");
			cipher2.init(Cipher.DECRYPT_MODE, secretKeySpec);
			byte[] cipherData2 = cipher2.doFinal(decoded);
			output = new String(cipherData2);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

}
