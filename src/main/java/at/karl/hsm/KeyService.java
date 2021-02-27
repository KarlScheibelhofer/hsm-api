package at.karl.hsm;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECGenParameterSpec;
import java.time.LocalDateTime;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Transactional
public class KeyService {

	public Key getById(long id) {
		return Key.findById(id);
	}

	public Collection<Key> getAll() {
		return Key.listAll();
	}

	public Collection<Key> getByName(String name) {
		return Key.findByName(name);
	}

	public Key create(Key k) {
		if (k.encodedPrivateKey == null && k.encodedPublicKey == null) {
			generate(k);
		}
		if (k.createdAt == null) {
			k.createdAt = LocalDateTime.now();
		}
		k.persist();
		return k;
	}

	public boolean delete(long id) {
		Key k = Key.findById(id);
		if (k != null) {
			k.delete();
			return true;
		}
		return false;
	}

	public long deleteAll() {
		return Key.deleteAll();
	}

	private void generate(Key newKeyTemplate) {
		try {
			if (newKeyTemplate.algorithm == null) {
				newKeyTemplate.algorithm = Key.Algorithm.EC_P256;
			}
			KeyPairGenerator kpg = KeyPairGenerator.getInstance(newKeyTemplate.algorithm.type);
			switch (newKeyTemplate.algorithm.type) {
				case "EC":
					ECGenParameterSpec param = new ECGenParameterSpec(newKeyTemplate.algorithm.parameterName);
					kpg.initialize(param);
					break;
				case "RSA":
					kpg.initialize(newKeyTemplate.algorithm.size);
				default:
			}
			KeyPair kp = kpg.genKeyPair();
			newKeyTemplate.encodedPrivateKey = kp.getPrivate().getEncoded();
			newKeyTemplate.encodedPublicKey = kp.getPublic().getEncoded();
		} catch (Exception e) {
			throw new KeyException(Response.Status.BAD_REQUEST, "invalid algorithm", e);
		}
	}

}
