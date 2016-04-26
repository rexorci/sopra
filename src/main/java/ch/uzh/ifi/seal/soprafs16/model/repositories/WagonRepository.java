package ch.uzh.ifi.seal.soprafs16.model.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.seal.soprafs16.model.Wagon;

@Repository("wagonRepository")
public interface WagonRepository extends CrudRepository<Wagon, Long> {
}
