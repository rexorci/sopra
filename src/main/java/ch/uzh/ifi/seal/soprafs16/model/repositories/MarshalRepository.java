package ch.uzh.ifi.seal.soprafs16.model.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.seal.soprafs16.model.Marshal;

@Repository("marshalRepository")
public interface MarshalRepository extends CrudRepository<Marshal, Long> {
}
