package ch.uzh.ifi.seal.soprafs16.model.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.seal.soprafs16.model.Marshal;
import ch.uzh.ifi.seal.soprafs16.model.characters.*;

@Repository("characterRepository")
public interface CharacterRepository extends CrudRepository<ch.uzh.ifi.seal.soprafs16.model.characters.Character, Long> {
}
