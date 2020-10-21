package at.karl.hsm;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Key extends PanacheEntity {

	public static List<Key> findByName(String name) {
		return PanacheEntity.find("name", name).list();
	}

	public Key() { }
	
	public String name;
	/** format <algorithm>-<size>, e.g. RSA-2048, EC-P256 */
	public String algorithm;
	public LocalDateTime createdAt;
	/** PKCS#8 encoded private key */
	public byte[] encodedKey;

}