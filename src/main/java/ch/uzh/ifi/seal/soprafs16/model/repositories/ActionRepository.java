package ch.uzh.ifi.seal.soprafs16.model.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.seal.soprafs16.model.Action;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

@Repository("actionRequestRepository")
public interface ActionRepository extends CrudRepository<ActionRequestDTO, Long> {
}
