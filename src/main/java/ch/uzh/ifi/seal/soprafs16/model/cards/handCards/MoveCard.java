package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;

@Entity
@JsonTypeName("moveCard")
public class MoveCard extends ActionCard implements Serializable {

    private static final long serialVersionUID = 1L;

    public void getMovableBeforeR(User user, List<Long> movable, WagonLevel wagonLevel) {
        if (wagonLevel.getWagonLevelBefore() != null) {
            movable.add(wagonLevel.getWagonLevelBefore().getId());
            getMovableBeforeR(user, movable, wagonLevel.getWagonLevelBefore());
        }
    }

    public void getMovableAfterR(User user, List<Long> movable, WagonLevel wagonLevel) {
        if (wagonLevel.getWagonLevelBefore() != null) {
            movable.add(wagonLevel.getWagonLevelBefore().getId());
            getMovableAfterR(user, movable, wagonLevel.getWagonLevelBefore());
        }
    }


    public void getMovableBeforeB(User user, List<Long> movable, WagonLevel wagonLevel) {
        if (wagonLevel.getWagonLevelBefore() != null) {
            movable.add(wagonLevel.getWagonLevelBefore().getId());
            getMovableBeforeB(user, movable, wagonLevel.getWagonLevelBefore());
        }
    }

    public void getMovableAfterB(User user, List<Long> movable, WagonLevel wagonLevel) {

        if (wagonLevel.getWagonLevelBefore() != null) {
            movable.add(wagonLevel.getWagonLevelBefore().getId());
            getMovableAfterB(user, movable, wagonLevel.getWagonLevelBefore());
        }
    }
}
