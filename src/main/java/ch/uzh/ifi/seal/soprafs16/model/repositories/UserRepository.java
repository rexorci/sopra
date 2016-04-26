package ch.uzh.ifi.seal.soprafs16.model.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.seal.soprafs16.model.User;

@Repository("userRepository")
public interface UserRepository extends CrudRepository<User, Long> {
	User findByName(String name);
	User findByUsername(String username);
	User findByToken(String token);
}
