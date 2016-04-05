package ch.uzh.ifi.seal.soprafs16.service;

import org.springframework.stereotype.Service;

import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;

/**
 * Created by Christoph on 05/04/16.
 */

@Service("gameLogicService")
public class GameLogicService {

    /**
     * @param game
     * @return
     */
    public User getNextPlayer(Game game) {
        return game.getUsers().get((game.getCurrentPlayer() + 1) % game.getUsers().size());
    }

    public void modifyNotifierUser(User oldUser, User newUser) {
        if (newUser.getGame() == null) {
            if (oldUser.getGame() != null) {
                oldUser.getGame().getUsers().remove(oldUser);
            }
        } else {
            if (oldUser.getGame() != null) {
                if (oldUser.getGame().getId() != newUser.getGame().getId()) {
                    oldUser.getGame().getUsers().remove(oldUser);
                    newUser.getGame().getUsers().add(newUser);
                }
            }else{
                newUser.getGame().getUsers().add(newUser);
            }
        }

        if (newUser.getWagonLevel() == null) {
            if (oldUser.getWagonLevel() != null) {
                oldUser.getWagonLevel().getUsers().remove(oldUser);
            }
        } else {
            if (oldUser.getWagonLevel() != null) {
                if (oldUser.getWagonLevel().getId() != newUser.getWagonLevel().getId()) {
                    oldUser.getWagonLevel().getUsers().remove(oldUser);
                    newUser.getWagonLevel().getUsers().add(newUser);
                }
            }else{
                newUser.getGame().getUsers().add(newUser);
            }
        }
    }
}
