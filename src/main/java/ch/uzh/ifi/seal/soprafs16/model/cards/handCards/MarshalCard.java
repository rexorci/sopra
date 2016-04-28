package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.MoveMarshalRequestDTO;

@Entity
@JsonTypeName("marshalCard")
public class MarshalCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public MoveMarshalRequestDTO generateMarshalRequest(Game game)
    {
        MoveMarshalRequestDTO mmrq = new MoveMarshalRequestDTO();
        mmrq.getMovableWagonsLvlIds().add(game.getMarshal().getWagonLevel().getWagonLevelBefore().getId());
        mmrq.getMovableWagonsLvlIds().add(game.getMarshal().getWagonLevel().getWagonLevelAfter().getId());


        mmrq.setSpielId(game.getId());
        game.getActions().add(mmrq);

        return mmrq;
    }

    public MoveMarshalRequestDTO generateActionRequest(Game game, User user)
    {
        return null;
    }

}
