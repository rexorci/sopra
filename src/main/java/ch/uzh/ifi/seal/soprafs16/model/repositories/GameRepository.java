package ch.uzh.ifi.seal.soprafs16.model.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.seal.soprafs16.model.Game;

@Repository("gameRepository")
public interface  GameRepository extends CrudRepository<Game, Long> {

}
