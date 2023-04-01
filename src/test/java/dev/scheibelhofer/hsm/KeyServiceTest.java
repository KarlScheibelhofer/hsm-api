package dev.scheibelhofer.hsm;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Collection;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.wildfly.common.Assert;


@TransactionalQuarkusTest
public class KeyServiceTest {

  @Inject
  public KeyService service;

  @BeforeEach
  public void cleanup() {
    service.deleteAll();
  }

  @Test
  public void testAdd() {
    Key newKeyTemplate = new Key();
    newKeyTemplate.name = "NewAddedKey";

    Key newKey1 = service.create(newKeyTemplate);

    Assertions.assertEquals(newKeyTemplate.name, newKey1.name);
    Assertions.assertNotNull(newKey1.id);
    Assertions.assertEquals(KeyAlgorithm.EC_P256, newKey1.algorithm);
  }

  @Test
  public void testGetById() {
    Key newKeyTemplate = new Key();
    newKeyTemplate.name = "NewAddedKey";
    Key newKey1 = service.create(newKeyTemplate);

    final Key k = service.getById(newKey1.id);

    Assertions.assertNotNull(k);
    Assertions.assertEquals(newKeyTemplate.name, k.name);

    // call a second time, now it will come from the cache
    final Key kc = service.getById(newKey1.id);

    Assertions.assertNotNull(kc);
    Assertions.assertEquals(newKeyTemplate.name, kc.name);
  }

  @Test
  public void testGetAll() {

    Key newKeyTemplate = new Key();
    newKeyTemplate.name = "Key-1";
    service.create(newKeyTemplate);

    Key newKeyTemplate2 = new Key();
    newKeyTemplate2.name = "Key-2";
    service.create(newKeyTemplate2);

    final Collection<Key> keys = service.getAll();
    Assertions.assertNotNull(keys);
    Assertions.assertEquals(2, keys.size());
  }

  @Test
  public void testGetByTitle() {
    Key newKeyTemplate = new Key();
    newKeyTemplate.name = "Key-Name";
    service.create(newKeyTemplate);

    final Collection<Key> ks = service.getByName(newKeyTemplate.name);
    Assertions.assertNotNull(ks);
    Assertions.assertEquals(1, ks.size());
    Assertions.assertEquals(newKeyTemplate.name, ks.iterator().next().name);
  }

  @Test
  public void testAddDelete() {
    final Collection<Key> initialKeys = service.getAll();

    Key newKeyTemplate = new Key();
    newKeyTemplate.name = "NewKey";

    Key newKey1 = service.create(newKeyTemplate);
    Assertions.assertEquals(newKeyTemplate.name, newKey1.name);
    Assertions.assertNotNull(newKey1.id);

    final Collection<Key> secondKeys = service.getAll();
    Assertions.assertEquals(initialKeys.size() + 1, secondKeys.size());

    Key newKey2 = service.getById(newKey1.id);
    Assertions.assertEquals(newKeyTemplate.name, newKey2.name);

    boolean deleted = service.delete(newKey1.id);
    Assert.assertTrue(deleted);
    Key newK3 = service.getById(newKey1.id);
    Assertions.assertNull(newK3);

    // long maxId = initialKeys.stream().mapToLong(g -> g.id).max().getAsLong();
    // boolean deleteSuccess = service.delete(maxId + 1);
    boolean deleteSuccess = service.delete(newKey1.id);
    Assertions.assertFalse(deleteSuccess);
  }

  @Test
  public void testGenerate() {
    Key newKeyTemplate = new Key();
    newKeyTemplate.name = "NewGeneratedKey";

    // test with all available key algorithms
    for (KeyAlgorithm keyAlgoritm : KeyAlgorithm.values()) {
      newKeyTemplate.algorithm = keyAlgoritm;
      Key newKey1 = service.create(newKeyTemplate);
  
      Assertions.assertEquals(newKeyTemplate.name, newKey1.name);
      Assertions.assertNotNull(newKey1.id);
      Assertions.assertEquals(keyAlgoritm, newKey1.algorithm);
      Assertions.assertNotNull(newKey1.encodedPrivateKey);
      Assertions.assertNotNull(newKey1.encodedPublicKey);
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"EC_P256", "EC_P384", "EC_P521", "RSA_PSS_2048", "EC_ED25519", "EC_ED448"})
  public void testSignData(String keyAlgorithm) throws Exception {
    Key keyTemplate = new Key();
    keyTemplate.algorithm = KeyAlgorithm.valueOf(keyAlgorithm);
    keyTemplate.name = "TestSignatureKey";

    Key signatureKey = service.create(keyTemplate);
    
    byte[] data = "This is my data!".getBytes(StandardCharsets.UTF_8);
    Signature signature = service.signData(signatureKey.id, data);
    
    PublicKey publicKey = signatureKey.getPublicKey();
    java.security.Signature sigService = java.security.Signature.getInstance(signatureKey.algorithm.preferredAlgorithm);
    sigService.initVerify(publicKey);
    switch (signatureKey.algorithm.preferredAlgorithm) {
      case "RSASSA-PSS" -> sigService.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
    }    
    sigService.update(data);
    Assertions.assertTrue(sigService.verify(signature.signatureValue));
  }

}