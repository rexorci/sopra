package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.characters.Django;

@Entity
@JsonTypeName("shootCard")
public class ShootCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public void getShootableUsersBeforeR(User user, List<User> shootable, WagonLevel wagonLevel) {
        if (wagonLevel.getWagonLevelBefore() != null) {
            int size = wagonLevel.getWagonLevelBefore().getUsers().size();
            for (int i = 0; i < size; i++) {
                shootable.add(wagonLevel.getWagonLevelBefore().getUsers().get(i));
            }
            if (shootable.isEmpty() || user.getCharacter().getClass().equals(Django.class)) {
                getShootableUsersBeforeR(user, shootable, wagonLevel.getWagonLevelBefore());

            }
        }
    }

    public void getShootableUsersAfterR(User user, List<User> shootable, WagonLevel wagonLevel) {
        if (wagonLevel.getWagonLevelAfter() != null) {
            int size = wagonLevel.getWagonLevelAfter().getUsers().size();
            for (int i = 0; i < size; i++) {
                shootable.add(wagonLevel.getWagonLevelAfter().getUsers().get(i));
            }
            if (shootable.isEmpty() || user.getCharacter().getClass().equals(Django.class)) {
                getShootableUsersAfterR(user, shootable, wagonLevel.getWagonLevelAfter());
            }
        }
    }

    public void getShootableUsersBeforeB(User user, List<User> shootable, WagonLevel wagonLevel) {
        if (wagonLevel.getWagonLevelBefore() != null) {
            int size = wagonLevel.getWagonLevelBefore().getUsers().size();
            for (int i = 0; i < size; i++) {
                shootable.add(wagonLevel.getWagonLevelBefore().getUsers().get(i));
            }
            if (user.getCharacter().getClass().equals(Django.class) && shootable.isEmpty()) {
                getShootableUsersBeforeB(user, shootable, wagonLevel.getWagonLevelBefore());
            }
        }
    }

    public void getShootableUsersAfterB(User user, List<User> shootable, WagonLevel wagonLevel) {
        if (wagonLevel.getWagonLevelAfter() != null) {
            int size = wagonLevel.getWagonLevelAfter().getUsers().size();
            for (int i = 0; i < size; i++) {
                shootable.add(wagonLevel.getWagonLevelAfter().getUsers().get(i));
            }
            if (shootable.isEmpty() && user.getCharacter().getClass().equals(Django.class)) {
                getShootableUsersAfterB(user, shootable, wagonLevel.getWagonLevelAfter());
            }
        }
    }
}