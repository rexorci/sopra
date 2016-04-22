package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import java.io.Serializable;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

@Entity
public class MarshalCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public ActionRequestDTO generateActionRequest(Game game, User user) {
        return null;
    }
}
