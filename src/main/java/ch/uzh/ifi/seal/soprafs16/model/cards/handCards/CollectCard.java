package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.CollectItemRequestDTO;

@Entity
@JsonTypeName("collectCard")
public class CollectCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public CollectItemRequestDTO generateActionRequest(Game game, User user) {
        CollectItemRequestDTO crq = new CollectItemRequestDTO();
        crq.setHasBag(Boolean.FALSE);
        crq.setHasCase(Boolean.FALSE);
        crq.setHasGem(Boolean.FALSE);
        if(user.getWagonLevel().getItems().size()>0)
        {
            for (int i = 0; i < user.getWagonLevel().getItems().size(); i++) {
                if(user.getWagonLevel().getItems().get(i).getItemType() == ItemType.GEM)
                {
                    crq.setHasGem(Boolean.TRUE);
                }
                if (user.getWagonLevel().getItems().get(i).getItemType() == ItemType.BAG)
                {
                    crq.setHasBag(Boolean.TRUE);
                }
                if(user.getWagonLevel().getItems().get(i).getItemType() == ItemType.CASE)
                {
                    crq.setHasCase(Boolean.TRUE);
                }
            }
        }
        crq.setGameId(game.getId());
        crq.setUserId(user.getId());
        game.getActions().add(crq);
        return crq;

    }
}
