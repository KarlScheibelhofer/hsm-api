package at.karl.hsm;

import java.util.Collection;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;

@TransactionalQuarkusTest
@Disabled("because of https://github.com/quarkusio/quarkus/issues/1724")
public class GoalServiceTest {

  @Inject
  public KeyService service;

  @Test
  public void testGetById() {
    final Key k = service.getById(1);
    Assertions.assertNotNull(k);
    Assertions.assertEquals("Sports", k.name);
  }
  
  @Test
  public void testGetAll() {
    final Collection<Key> ks = service.getAll();
    Assertions.assertNotNull(ks);
    Assertions.assertEquals(3, ks.size());
  }
  
  @Test
  public void testGetByTitle() {
    final Collection<Key> ks = service.getByName("Sports");
    Assertions.assertNotNull(ks);
    Assertions.assertEquals(1, ks.size());
    Assertions.assertEquals("Sports", ks.iterator().next().name);
  }
  
  @Test
  public void testAddDelete() {
    final Collection<Key> initialGs = service.getAll();
    
    Key newK = new Key();
    newK.name = "NewGoal";
    
    Key newK1 = service.add(newK);
    Assertions.assertEquals(newK.name, newK1.name);
    Assertions.assertNotNull(newK1.id);
    
    final Collection<Key> secondKs = service.getAll();
    Assertions.assertEquals(initialGs.size() + 1, secondKs.size());
    
    Key newK2 = service.getById(newK1.id);
    Assertions.assertEquals(newK.name, newK2.name);
    
    boolean deleted = service.delete(newK1.id);
    Assert.assertTrue(deleted);
    Key newK3 = service.getById(newK1.id);
    Assertions.assertNull(newK3);

    long maxId = initialGs.stream().mapToLong(g -> g.id).max().getAsLong();
    boolean deleteSuccess = service.delete(maxId + 1);
    Assertions.assertFalse(deleteSuccess);
  }

}