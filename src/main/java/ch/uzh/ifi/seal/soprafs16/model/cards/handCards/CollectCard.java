package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import java.io.Serializable;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.CollectItemRequestDTO;

@Entity
public class CollectCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CollectItemRequestDTO generateCollectItemRequest(Game game, User user)
    {
        CollectItemRequestDTO crq = new CollectItemRequestDTO();

        for(int i = 0; i<user.getWagonLevel().getItems().size(); i++)
        crq.getCollectableItemIds().add(user.getWagonLevel().getItems().get(i).getId());
        crq.setGameId(game.getId());
        return crq;
    }

}
