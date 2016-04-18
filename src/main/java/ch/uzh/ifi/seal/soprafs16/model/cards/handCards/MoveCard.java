package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import java.io.Serializable;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.MoveRequestDTO;

@Entity
public class MoveCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public MoveRequestDTO generateActionRequest(Game game, User user)
    {
        MoveRequestDTO mrq = new MoveRequestDTO();
        mrq.getMovableWagonsLvlIds().add(user.getWagonLevel().getWagonLevelBefore().getId());
        mrq.getMovableWagonsLvlIds().add(user.getWagonLevel().getWagonLevelAfter().getId());

        mrq.setGameId(game.getId());
        mrq.setUserId(user.getId());
        game.getActions().add(mrq);
        return mrq;
    }

}
