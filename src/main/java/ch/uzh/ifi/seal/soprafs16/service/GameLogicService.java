package ch.uzh.ifi.seal.soprafs16.service;

import org.springframework.stereotype.Service;

import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;

/**
 * Created by rafael on 01/04/16.
 */

@Service("gameLogicService")
public class GameLogicService {

    /**
     *
     * @param game
     * @return
     */
    public User getNextPlayer(Game game){
        return game.getUsers().get((game.getCurrentPlayer() + 1) % game.getUsers().size());
    }
}
