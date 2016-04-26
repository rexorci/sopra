package ch.uzh.ifi.seal.soprafs16.model.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;

@Repository("wagonLevelRepository")
public interface WagonLevelRepository extends CrudRepository<WagonLevel, Long> {
}
