package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.PunchRequestDTO;

@Entity
public class PunchCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public PunchRequestDTO generatePunchRequest(Game game, User user){
        PunchRequestDTO prq = new PunchRequestDTO();
        prq.setGameId(game.getId());
        List<User> userList = new ArrayList<User>();

        for(int i = 0; i<user.getWagonLevel().getUsers().size(); i++ )
        userList.add(user.getWagonLevel().getUsers().get(i));
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
            prq.getPunchableUserIds().add(userList.get(i).getId());
        }
        return prq;

    }

}
