package dev.scheibelhofer.hsm;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Key extends PanacheEntity {

	public enum Algorithm {
		EC_P256("EC", 256, "secp256r1", "SHA256withECDSA"),
		EC_P384("EC", 384, "secp384r1", "SHA384withECDSA"),
		EC_P521("EC", 521, "secp521r1", "SHA512withECDSA"),
		RSA_PSS_2048("RSA", 2048, null, "RSASSA-PSS"),
		EC_ED25519("Ed25519", 255, "Ed25519", "EdDSA"), // requires Java 17
		EC_ED448("Ed448", 448, "Ed448", "EdDSA"), // requires Java 17
		;

		/* the java key algorithm name, e.g. EC, RSA, Ed25519,... */
		String type;

		/* the key size in bit */
		int size;

		/* the java name for the key generation parameters, used for EC curves */
		String parameterName;

		/* the standard Java algorithm name for signature or encryption */ 
		String preferredAlgorithm;

		private Algorithm(String type, int size, String paramterName, String preferredAlgorithm) {
			this.type = type;
			this.size = size;
			this.parameterName = paramterName;
			this.preferredAlgorithm = preferredAlgorithm;
		}

	}

	public static List<Key> findByName(String name) {
		return find("name", name).list();
	}

	public Key() { }
	
	public String name;

	public Algorithm algorithm;

	public LocalDateTime createdAt;
	
	/** PKCS#8 encoded private key */
	public byte[] encodedPrivateKey;

	/** X.509 encoded public key */
	public byte[] encodedPublicKey;

	transient private PrivateKey privateKey;

	transient private PublicKey publicKey;

	PrivateKey getPrivateKey() {
		if (this.privateKey != null) {
			return this.privateKey;
		}
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(this.encodedPrivateKey);
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance(algorithm.type);
			this.privateKey = keyFactory.generatePrivate(keySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException("failed to restore java private key object from encoded key", e);
		}
		return this.privateKey;
	}

	PublicKey getPublicKey() {
		if (this.publicKey != null) {
			return this.publicKey;
		}
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(this.encodedPublicKey);
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance(algorithm.type);
			this.publicKey = keyFactory.generatePublic(keySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException("failed to restore java public key object from encoded key", e);
		}
		return this.publicKey;
	}

}