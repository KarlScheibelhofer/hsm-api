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

import jakarta.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Key extends PanacheEntity {

	public static List<Key> findByName(String name) {
		return find("name", name).list();
	}

	public Key() { 
		// empty
	}
	
	public String name;

	public KeyAlgorithm algorithm;

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
			throw new HsmException("failed to restore java private key object from encoded key", e);
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
			throw new HsmException("failed to restore java public key object from encoded key", e);
		}
		return this.publicKey;
	}

}