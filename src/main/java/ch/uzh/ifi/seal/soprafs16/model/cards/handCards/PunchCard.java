package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.PunchRequestDTO;

@Entity
@JsonTypeName("punchCard")
public class PunchCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public PunchRequestDTO generateActionRequest(Game game, User user){
        PunchRequestDTO prq = new PunchRequestDTO();
        List<User> userList = new ArrayList<User>();

        for(int i = 0; i<user.getWagonLevel().getUsers().size(); i++ ) {
            userList.add(user.getWagonLevel().getUsers().get(i));
        }
        if(userList.size() > 2)
        {
            for(int i = 0; i < userList.size(); i++)
            {
                if(userList.get(i).getCharacterType()== ("Belle") || userList.get(i).getId() == user.getId())
                {
                    userList.remove(i);
                }
            }
        }
        for(int i = 0; i<userList.size(); i++)
        {
            prq.getHasBag().add(i,Boolean.FALSE);
            prq.getHasCase().add(i, Boolean.FALSE);
            prq.getHasGem().add(i, Boolean.FALSE);
            prq.getPunchableUserIds().add(userList.get(i).getId());

            if(userList.get(i).getItems().size() > 0)
            {
                for(int j = 0; j < userList.get(i).getItems().size(); j++)
                {
                    if(userList.get(i).getItems().get(j).getItemType() == ItemType.GEM)
                    {
                    prq.getHasGem().set(i, Boolean.TRUE);
                    }

                    if(userList.get(i).getItems().get(j).getItemType() == ItemType.BAG)
                    {
                        prq.getHasBag().set(i, Boolean.TRUE);
                    }

                    if(userList.get(i).getItems().get(j).getItemType() == ItemType.CASE) {
                        prq.getHasCase().set(i, Boolean.TRUE);
                    }
                }
            }
        }
        if(user.getWagonLevel().getWagonLevelBefore() != null)
        {
            prq.getMovable().add(user.getWagonLevel().getWagonLevelBefore().getId());
        }
        if(user.getWagonLevel().getWagonLevelAfter() != null)
        {
            prq.getMovable().add(user.getWagonLevel().getWagonLevelAfter().getId());
        }

        prq.setSpielId(game.getId());
        prq.setUserId(user.getId());
        game.getActions().add(prq);
        return prq;

    }

    @Override
    public ActionRequestDTO generateMarshalRequest(Game game) {
        return null;
    }

}
