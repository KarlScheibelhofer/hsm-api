package at.karl.hsm;

import java.time.LocalDateTime;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

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

	public Key add(Key goal) {
		if (goal.createdAt == null) {
			goal.createdAt = LocalDateTime.now();
		}
		goal.persist();
		return goal;
	}

	public boolean delete(long id) {
		Key g = Key.findById(id);
		if (g != null) {
			g.delete();
			return true;
		}
		return false;
	}

}
