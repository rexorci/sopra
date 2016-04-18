package ch.uzh.ifi.seal.soprafs16.model.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;

/**
 * Created by Nico on 17.04.2016.
 */

@Repository("turnRepository")
public interface TurnRepository extends CrudRepository<Turn, Long> {
}
