package ch.uzh.ifi.seal.soprafs16.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.seal.soprafs16.GameConstants;
import ch.uzh.ifi.seal.soprafs16.constant.CharacterType;
import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonRepository;
import ch.uzh.ifi.seal.soprafs16.service.GameService;

@RestController
public class GameServiceController extends GenericService {

    Logger logger = LoggerFactory.getLogger(GameServiceController.class);

    //region Repositories
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private GameRepository gameRepo;
    @Autowired
    private WagonRepository wagonRepo;
    @Autowired
    private WagonLevelRepository wagonLevelRepo;
    @Autowired
    private ItemRepository itemRepo;
    @Autowired
    private MarshalRepository marshalRepo;
    //endregion

    private final String CONTEXT = "/games";

    //games - GET
    @RequestMapping(value = CONTEXT)
    @ResponseStatus(HttpStatus.OK)
    public List<Game> listGames() {
        logger.debug("listGames");
        List<Game> result = new ArrayList<>();
        gameRepo.findAll().forEach(result::add);
        return result;
    }

    //games - GET
    @RequestMapping(value = CONTEXT, params = {"status"})
    @ResponseStatus(HttpStatus.OK)
    public List<Game> listGamesFiltered(@RequestParam("status") String statusFilter) {
        logger.debug("listGamesFiltered");
        List<Game> result = new ArrayList<>();
        for (Game game : gameRepo.findAll()) {
            if (game.getStatus().toString().equals(statusFilter)) {
                result.add(game);
            }
        }
        return result;
    }

    //games - POST
    @RequestMapping(value = CONTEXT, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Long addGame(@RequestBody Game game, @RequestParam("token") String userToken) {
        logger.debug("addGame: " + game);

        User owner = userRepo.findByToken(userToken);

        if (owner != null) {
            game.setStatus(GameStatus.PENDING);
            owner.setGame(game);
            game.setUsers(new ArrayList<User>());
            game.getUsers().add(owner);
            game.setOwner(owner.getName());
            gameRepo.save(game);

            return game.getId();
        } else {
            return null;
        }
    }

    //games/{game-id} - GET
    @RequestMapping(value = CONTEXT + "/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    public Game getGame(@PathVariable Long gameId) {
        logger.debug("getGame: " + gameId);

        Game game = gameRepo.findOne(gameId);

        return game;
    }

    //games/{game-id} - DELETE
    @RequestMapping(value = CONTEXT + "/{gameId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public Long deleteGame(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        logger.debug("deleteGame: " + gameId);
        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findByToken(userToken);
        if (game != null && user != null) {
            String ownerString = game.getOwner();
            if (user.getName().equals(game.getOwner())) {
                for (User u : game.getUsers()) {
                    u.setGame(null);
                    userRepo.save(u);
                }
                gameRepo.delete(game);
                return gameId;
            } else {
                logger.debug("deleteGame: game " + gameId + " - user is not owner of game");
                return null;
            }
        } else {
            logger.debug("deleteGame: game " + gameId + " - user or game is null");
            return null;
        }
    }

    //games/{game-id}/start - POST
    @RequestMapping(value = CONTEXT + "/{gameId}/start", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void startGame(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        logger.debug("startGame: " + gameId);

        Game game = gameRepo.findOne(gameId);
        User owner = userRepo.findByToken(userToken);

        if (owner != null && game != null && game.getOwner().equals(owner.getName())) {
            GameService chs = new GameService();
            chs.startGame(game, owner, wagonRepo, wagonLevelRepo, marshalRepo);
        }
    }

    //games/{game-id}/stop - POST
    @RequestMapping(value = CONTEXT + "/{gameId}/stop", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void stopGame(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        logger.debug("stopGame: " + gameId);

        Game game = gameRepo.findOne(gameId);
        User owner = userRepo.findByToken(userToken);

        if (owner != null && game != null && game.getOwner().equals(owner.getUsername())) {
        }
    }

    //games/{game-id}/users - POST
    @RequestMapping(value = CONTEXT + "/{gameId}/users", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Long addPlayer(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        logger.debug("addUser: " + userToken);

        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findByToken(userToken);
        if (game != null && user != null && game.getUsers().size() < GameConstants.MAX_PLAYERS && game.getStatus() == GameStatus.PENDING) {
            game.getUsers().add(user);
            user.setGame(game);
            logger.debug("Game: " + game.getName() + " - user added: " + user.getUsername());
            gameRepo.save(game);
            userRepo.save(user);
            return (long) (game.getUsers().size() - 1);
        } else {
            logger.error("Error adding user with token: " + userToken);
            return null;
        }
    }

    //games/{game-id}/users - PUT
    @RequestMapping(value = CONTEXT + "/{gameId}/users", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public User modifyUserCharacter(@PathVariable Long gameId, @RequestParam("token") String userToken, @RequestParam("character") String character) {
        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findByToken(userToken);
        if (user != null && game != null) {
            try {
                CharacterType characterType = CharacterType.valueOf(character);
                for (User u : game.getUsers()) {
                    if (u.getCharacterType() != null && u.getId() != user.getId()) {
                        if (u.getCharacterType().equals(characterType)) {
                            return null;
                        }
                    }
                }
                user.setCharacterType(characterType);
                return user;
            } catch (IllegalArgumentException iae) {
                return null;
            }
        } else {
            return null;
        }
    }

    //games/{game-id}/users - DELETE
    @RequestMapping(value = CONTEXT + "/{gameId}/users", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public Long removePlayer(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        logger.debug("addUser: " + userToken);

        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findByToken(userToken);
        if (game != null && user != null) {
            if (game.getOwner().equals(user.getName())) {
                for (User u : game.getUsers()) {
                    u.setGame(null);
                    userRepo.save(u);
                }
                gameRepo.delete(game);
            } else {
                user.setGame(null);
                gameRepo.save(game);
                userRepo.save(user);
            }
            return gameId;
        } else {
            logger.error("Error removing user with token: " + userToken);
            return null;
        }
    }

    //games/{game-id}/switchLevel - POST
    @RequestMapping(value = CONTEXT + "/{gameId}/switchLevel", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Game prototypeSwitchLevel(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findByToken(userToken);
        if (game != null && user != null && game.getStatus() == GameStatus.RUNNING) {
            WagonLevel wagonLevelNew;
            if (user.getWagonLevel().getLevelType().equals(LevelType.BOTTOM)) {
                wagonLevelNew = user.getWagonLevel().getWagon().getTopLevel();
            } else {
                wagonLevelNew = user.getWagonLevel().getWagon().getBottomLevel();
            }

            user.getWagonLevel().getUsers().remove(user);
            gameRepo.save(game);//this save is mandatory!

            wagonLevelNew.getUsers().add(user);
            user.setWagonLevel(wagonLevelNew);
            gameRepo.save(game);
            userRepo.save(user);
            return game;
        } else {
            logger.error("Error switching level");
            return null;
        }
    }

    //games/{gameId}/action - GET
    //games/{gameId}/action - POST

}