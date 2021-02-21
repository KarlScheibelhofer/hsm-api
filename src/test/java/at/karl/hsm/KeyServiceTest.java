package at.karl.hsm;

import java.util.Collection;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    
    Key newKey1 = service.add(newKeyTemplate);

    Assertions.assertEquals(newKeyTemplate.name, newKey1.name);
    Assertions.assertNotNull(newKey1.id);
    Assertions.assertNull( newKey1.algorithm);
   }

  @Test
  public void testGetById() {
    Key newKeyTemplate = new Key();
    newKeyTemplate.name = "NewAddedKey";
    Key newKey1 = service.add(newKeyTemplate);

    final Key k = service.getById(newKey1.id);

    Assertions.assertNotNull(k);
    Assertions.assertEquals(newKeyTemplate.name, k.name);
  }
  
  @Test
  public void testGetAll() {
    
    Key newKeyTemplate = new Key();
    newKeyTemplate.name = "Key-1";
    service.add(newKeyTemplate);

    Key newKeyTemplate2 = new Key();
    newKeyTemplate2.name = "Key-2";
    service.add(newKeyTemplate2);
    
    final Collection<Key> keys = service.getAll();
    Assertions.assertNotNull(keys);
    Assertions.assertEquals(2, keys.size());
  }
  
  @Test
  public void testGetByTitle() {
    Key newKeyTemplate = new Key();
    newKeyTemplate.name = "Key-Name";
    service.add(newKeyTemplate);

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
    
    Key newKey1 = service.add(newKeyTemplate);
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

}