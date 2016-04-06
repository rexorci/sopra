package ch.uzh.ifi.seal.soprafs16.service;

import org.springframework.stereotype.Service;

import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.Wagon;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonRepository;

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

    public void modifyUser(User userOld, User userNew, WagonLevel wagonLevelOld, Game game, WagonLevelRepository wagonLevelRepo, GameRepository gameRepo) {

        userOld.setName(userNew.getName());
        userOld.setUsername(userNew.getUsername());
        userOld.setStatus(userNew.getStatus());

        if (wagonLevelOld.getId() != userNew.getWagonLevelIdNew()) {
            WagonLevel wagonLevelNew = wagonLevelRepo.findOne(userNew.getWagonLevelIdNew());
            wagonLevelOld.getUsers().remove(userOld);
            gameRepo.save(game);//this save is mandatory!

            wagonLevelNew.getUsers().add(userOld);
            userNew.setWagonLevel(wagonLevelNew);
        }
        gameRepo.save(game);
    }
}
