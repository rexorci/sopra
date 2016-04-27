package ch.uzh.ifi.seal.soprafs16.model.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;

/**
 * Created by Nico on 27.04.2016.
 */
@Repository("actionResponseRepository")
public interface ActionResponseRepository extends CrudRepository<ActionResponseDTO, Long> {
}
