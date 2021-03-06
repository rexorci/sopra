package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

@Entity
@JsonTypeName("changeLevelCard")
public class ChangeLevelCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public ActionRequestDTO generateActionRequest(Game game, User user) {
        return null;
    }

    @Override
    public ActionRequestDTO generateMarshalRequest(Game game) {
        return null;
    }
}
