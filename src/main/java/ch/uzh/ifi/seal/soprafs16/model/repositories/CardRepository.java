package ch.uzh.ifi.seal.soprafs16.model.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.seal.soprafs16.model.cards.Card;

@Repository("cardRepository")
public interface CardRepository extends CrudRepository<Card, Long> {
}
