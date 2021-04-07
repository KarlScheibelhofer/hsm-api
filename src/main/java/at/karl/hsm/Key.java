package at.karl.hsm;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Key extends PanacheEntity {

	public enum Algorithm {
		EC_P256("EC", 256, "secp256r1", "SHA256withECDSA"),
		EC_P384("EC", 384, "secp384r1", "SHA384withECDSA"),
		RSA_PSS_2048("RSA", 2048, null, "SHA256withRSAandMGF1"),
		;

		String type;
		int size;
		String parameterName;
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

}