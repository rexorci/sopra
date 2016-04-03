package ch.uzh.ifi.seal.soprafs16.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.logging.Level;

import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.Item;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.Wagon;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonRepository;

/**
 * Created by chris on 02/04/16.
 */

@Service("gameInitService")
public class GameInitService {

    /**
     * Initializes the train with its wagons/wagonlevels
     *
     * @param game
     * @return
     */
    public Game initGame(Game game, User owner,GameRepository gameRepo, WagonRepository wagonRepo,WagonLevelRepository wagonLevelRepo,ItemRepository itemRepo) {
        game.setStatus(GameStatus.PENDING);

        owner.setGame(game);
        game.setCurrentPlayer(0);
        game.setUsers(new ArrayList<User>());
        game.getUsers().add(owner);
        game.setOwner(owner.getName());
        gameRepo.save(game);

        game.setWagons(new ArrayList<Wagon>());

        for (int i = 0; i < 4; i++) {
            Wagon wagon = new Wagon();
            wagon.setGame(game);
            game.getWagons().add(wagon);
            wagonRepo.save(wagon);

            WagonLevel topLevel = new WagonLevel();
            topLevel.setLevelType(LevelType.TOP);
            topLevel.setItems(new ArrayList<Item>());
            wagon.setTopLevel(topLevel);
            wagonLevelRepo.save(topLevel);

            WagonLevel botLevel = new WagonLevel();
            botLevel.setLevelType(LevelType.BOTTOM);
            botLevel.setItems(new ArrayList<Item>());
            wagon.setBottomLevel(botLevel);
            wagonLevelRepo.save(botLevel);

            //testing purpose
            Item testItem = new Item();
            testItem.setItemType(ItemType.BAG);
            testItem.setValue(450);
            botLevel.getItems().add(testItem);
            itemRepo.save(testItem);
        }

        return game;
    }
}
