package ch.uzh.ifi.seal.soprafs16.model.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

/**
 * Created by Schnudeldudel on 23.04.2016.
 */

@Repository("actionRepository")
public interface ActionRepository extends CrudRepository<ActionRequestDTO, Long> {

}
