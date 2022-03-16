package at.karl.hsm;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.EdDSAParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import static io.quarkiverse.loggingjson.providers.KeyValueStructuredArgument.*;
import io.quarkus.logging.Log;

@ApplicationScoped
@Transactional
public class KeyService {

	@ConfigProperty(name = "ham-api.keys.log-id", defaultValue = "key-service")
	String logId;

	public Key getById(long id) {
		long t0 = System.nanoTime();
		Key k = Key.findById(id);
		long t1 = System.nanoTime();
		Log.infof("getById", kv("stats", Map.of("code", "0", "duration", Double.valueOf((t1 - t0)/1e6).toString())));
		return k;
	}

	public Collection<Key> getAll() {
		Log.infof("getAll", kv("logId", logId));
		return Key.listAll();
	}

	public Collection<Key> getByName(String name) {
		return Key.findByName(name);
	}

	public Key create(Key k) {
		Log.info(logId + " - create");
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
		} catch (GeneralSecurityException e) {
			throw new BadRequestException("invalid algorithm", e);
		}
	}

	public Signature signData(long id, byte[] data) {
        // false means we provide the hash value, for Ed25519 use SHA-512
        // EdDSAParameterSpec spec = new EdDSAParameterSpec(false);
		try {
			Key key = getById(id);
			String signatureAlgorithm = key.algorithm.preferredAlgorithm;
			java.security.Signature sigService = java.security.Signature.getInstance(signatureAlgorithm);
			sigService.initSign(key.getPrivateKey());
			switch (signatureAlgorithm) {
				case "RSASSA-PSS" -> sigService.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
			}
			sigService.update(data);
			byte[] signatureValue = sigService.sign();
			Signature signature = new Signature(id, signatureAlgorithm, signatureValue);
			return signature;
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidAlgorithmParameterException e) {
			throw new RuntimeException("failed to create signature: " + e.getMessage(), e);
		}
    }

	public Signature signHash(long id, String hashAlgorithm, byte[] hash) {
		try {
			Key key = getById(id);
			String rawSignatureAlgorithm;
			String signatureAlgorithm;
			switch(key.algorithm.type) {
				case "EC"      : rawSignatureAlgorithm = "NONEwithECDSA"; signatureAlgorithm = hashAlgorithm.replace("-","") + "withECDSA"; break;
				case "Ed25519" : rawSignatureAlgorithm = "Ed25519"; signatureAlgorithm = "Ed25519"; break;
				case "Ed448"   : rawSignatureAlgorithm = "Ed448"; signatureAlgorithm = "Ed448"; break;
				default        : throw new RuntimeException("algorithm " + key.algorithm.type + " only supports singing data, not hashes");
			};
			java.security.Signature sigService = java.security.Signature.getInstance(rawSignatureAlgorithm);
			sigService.initSign(key.getPrivateKey());
			switch (key.algorithm.type) {
				case "Ed25519", "Ed448" -> sigService.setParameter(new EdDSAParameterSpec(false));
			}
			sigService.update(hash);
			byte[] signatureValue = sigService.sign();
			Signature signature = new Signature(id, signatureAlgorithm, signatureValue);
			return signature;
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidAlgorithmParameterException e) {
			throw new RuntimeException("failed to create signature: " + e.getMessage(), e);
		}
    }

}
