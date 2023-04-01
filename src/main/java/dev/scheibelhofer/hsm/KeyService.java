package dev.scheibelhofer.hsm;

import static io.quarkiverse.loggingjson.providers.KeyValueStructuredArgument.kv;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

import org.eclipse.microprofile.config.inject.ConfigProperty;

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
		Log.infof("getById", kv("stats", Map.of("code", "0", "duration", Double.toString((t1 - t0)/1e6))));
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
				newKeyTemplate.algorithm = KeyAlgorithm.EC_P256;
			}
			KeyPairGenerator kpg = KeyPairGenerator.getInstance(newKeyTemplate.algorithm.type);
			switch (newKeyTemplate.algorithm.type) {
				case "EC":
					ECGenParameterSpec param = new ECGenParameterSpec(newKeyTemplate.algorithm.parameterName);
					kpg.initialize(param);
					break;
				case "RSA":
					kpg.initialize(newKeyTemplate.algorithm.size);
					break;
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
		try {
			Key key = getById(id);
			String signatureAlgorithm = key.algorithm.preferredAlgorithm;
			java.security.Signature sigService = java.security.Signature.getInstance(signatureAlgorithm);
			sigService.initSign(key.getPrivateKey());
			if ("RSASSA-PSS".equals(signatureAlgorithm)) {
				sigService.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
			}
			sigService.update(data);
			byte[] signatureValue = sigService.sign();
			return new Signature(id, signatureAlgorithm, signatureValue);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidAlgorithmParameterException e) {
			throw new HsmException("failed to create signature: " + e.getMessage(), e);
		}
    }

}
