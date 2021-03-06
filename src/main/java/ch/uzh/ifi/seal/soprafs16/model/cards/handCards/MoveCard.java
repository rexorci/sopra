package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.MoveRequestDTO;


@Entity
@JsonTypeName("moveCard")
public class MoveCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public MoveRequestDTO generateActionRequest(Game game, User user)
    {
        MoveRequestDTO mrq = new MoveRequestDTO();
        List<Long> movable = new ArrayList<Long>();
        mrq.setMovableWagonsLvlIds(movable);

        if(user.getWagonLevel().getLevelType() == LevelType.TOP)
        {
            getMovableBeforeR(user, movable, user.getWagonLevel());
            getMovableAfterR(user, movable, user.getWagonLevel());
            int size = movable.size();
            if (size > 3) {
                for (int i = 0; i < 3; i++) {
                    mrq.getMovableWagonsLvlIds().add(movable.get(i));
                }
            }
            else
            {
                for(int i = 0; i<size; i++)
                {
                    mrq.getMovableWagonsLvlIds().add(movable.get(i));
                }
            }

        }

        if(user.getWagonLevel().getLevelType() == LevelType.BOTTOM)
        {
            if(user.getWagonLevel().getWagonLevelBefore() != null) {
                mrq.getMovableWagonsLvlIds().add(user.getWagonLevel().getWagonLevelBefore().getId());
            }
            if(user.getWagonLevel().getWagonLevelAfter() != null) {
                mrq.getMovableWagonsLvlIds().add(user.getWagonLevel().getWagonLevelAfter().getId());
            }

        }


        mrq.setSpielId(game.getId());
        mrq.setUserId(user.getId());
        game.getActions().add(mrq);
        return mrq;
    }

    @Override
    public ActionRequestDTO generateMarshalRequest(Game game) {
        return null;
    }

    public void getMovableBeforeR(User user, List<Long> movable, WagonLevel wagonLevel)
    {
        if( wagonLevel.getWagonLevelBefore() != null)
        {
                movable.add(wagonLevel.getWagonLevelBefore().getId());
                getMovableBeforeR(user, movable, wagonLevel.getWagonLevelBefore());
        }
    }

    public void getMovableAfterR(User user, List<Long> movable, WagonLevel wagonLevel)
    {

            if( wagonLevel.getWagonLevelBefore() != null)
            {
                movable.add(wagonLevel.getWagonLevelBefore().getId());
                getMovableAfterR(user, movable, wagonLevel.getWagonLevelBefore());
            }
    }


    public void getMovableBeforeB(User user, List<Long> movable, WagonLevel wagonLevel)
    {

            if( wagonLevel.getWagonLevelBefore() != null)
            {
                movable.add(wagonLevel.getWagonLevelBefore().getId());
                getMovableBeforeB(user, movable, wagonLevel.getWagonLevelBefore());
            }


    }

    public void getMovableAfterB(User user, List<Long> movable, WagonLevel wagonLevel)
    {

            if( wagonLevel.getWagonLevelBefore() != null)
            {
                movable.add(wagonLevel.getWagonLevelBefore().getId());
                getMovableAfterB(user, movable, wagonLevel.getWagonLevelBefore());
            }

    }
}
