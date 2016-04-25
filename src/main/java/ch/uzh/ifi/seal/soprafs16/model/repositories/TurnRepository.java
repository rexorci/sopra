package ch.uzh.ifi.seal.soprafs16.model.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;

@Repository("turnRepository")
public interface TurnRepository extends CrudRepository<Turn, Long> {
}