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
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.Item;
import ch.uzh.ifi.seal.soprafs16.model.Marshal;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.Wagon;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonRepository;
import ch.uzh.ifi.seal.soprafs16.service.ControllerHelperService;
import ch.uzh.ifi.seal.soprafs16.service.GameInitService;
import ch.uzh.ifi.seal.soprafs16.service.GameLogicService;

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

	/*
     * Context: /games
	 */

    //region game
    @RequestMapping(value = CONTEXT)
    @ResponseStatus(HttpStatus.OK)
    public List<Game> listGames() {
        logger.debug("listGames");
        List<Game> result = new ArrayList<>();
        gameRepo.findAll().forEach(result::add);
        return result;
    }

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

            //GameInitService gameInitService = new GameInitService();
            // gameInitService.startGame(game, owner, gameRepo, wagonRepo, wagonLevelRepo, itemRepo, marshalRepo);

            return game.getId();
        } else {
            return null;
        }
    }

    /*
     * Context: /games/{game-id}
     */
    @RequestMapping(value = CONTEXT + "/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    public Game getGame(@PathVariable Long gameId) {
        logger.debug("getGame: " + gameId);

        Game game = gameRepo.findOne(gameId);

        return game;
    }
    //endregion

    /*
    * Context: /games/{game-id}
    */
    @RequestMapping(value = CONTEXT + "/{gameId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public Long deleteGame(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        logger.debug("deleteGame: " + gameId);
        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findByToken(userToken);
        if (game != null && user != null) {
            String ownerString = game.getOwner();
            if (user.getName().equals(game.getOwner())) {
//                ControllerHelperService chs = new ControllerHelperService();
//                chs.deleteGame(game, userRepo, wagonRepo, marshalRepo);

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

    //region start/stop game
    @RequestMapping(value = CONTEXT + "/{gameId}/start", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void startGame(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        logger.debug("startGame: " + gameId);

        Game game = gameRepo.findOne(gameId);
        User owner = userRepo.findByToken(userToken);

        if (owner != null && game != null && game.getOwner().equals(owner.getName())) {
            ControllerHelperService chs = new ControllerHelperService();
            chs.startGame(game, owner, wagonRepo, wagonLevelRepo, marshalRepo);
        }
    }

    @RequestMapping(value = CONTEXT + "/{gameId}/stop", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void stopGame(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        logger.debug("stopGame: " + gameId);

        Game game = gameRepo.findOne(gameId);
        User owner = userRepo.findByToken(userToken);

        if (owner != null && game != null && game.getOwner().equals(owner.getUsername())) {
        }
    }
    //endregion

    //region /{gameId}/move ff
    /*
     * Context: /game/{game-id}/move
	 */
//    @RequestMapping(value = CONTEXT + "/{gameId}/move")
//    @ResponseStatus(HttpStatus.OK)
//    public List<Move> listMoves(@PathVariable Long gameId) {
//        logger.debug("listMoves");
//
//        Game game = gameRepo.findOne(gameId);
//        if (game != null) {
//            return game.getMoves();
//        }
//
//        return null;
//    }
//
//    @RequestMapping(value = CONTEXT + "/{gameId}/move", method = RequestMethod.POST)
//    @ResponseStatus(HttpStatus.OK)
//    public void addMove(@RequestBody Move move) {
//        logger.debug("addMove: " + move);
//        // TODO Mapping into Move + execution of move
//    }
//
//    @RequestMapping(value = CONTEXT + "/{gameId}/move/{moveId}")
//    @ResponseStatus(HttpStatus.OK)
//    public Move getMove(@PathVariable Long gameId, @PathVariable Integer moveId) {
//        logger.debug("getMove: " + gameId);
//
//        Game game = gameRepo.findOne(gameId);
//        if (game != null) {
//            return game.getMoves().get(moveId);
//        }
//
//        return null;
//    }
//endregion

//    //region /{gameId}/player ff
//    /*
//     * Context: /games/{game-id}/players
//	 */
//    @RequestMapping(value = CONTEXT + "/{gameId}/users")
//    @ResponseStatus(HttpStatus.OK)
//    public List<User> listPlayers(@PathVariable Long gameId) {
//        logger.debug("listPlayers");
//
//        Game game = gameRepo.findOne(gameId);
//        if (game != null) {
//            return game.getUsers();
//        }
//
//        return null;
//    }

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
            //return CONTEXT + "/" + gameId + "/user/" + (game.getUsers().size() - 1);
            return (long) (game.getUsers().size() - 1);
        } else {
            logger.error("Error adding user with token: " + userToken);
            return null;
        }
    }

    @RequestMapping(value = CONTEXT + "/{gameId}/users", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public Long removePlayer(@PathVariable Long gameId, @RequestParam("token") String userToken) {
        logger.debug("addUser: " + userToken);

        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findByToken(userToken);
        if (game != null && user != null) {
            if (game.getOwner().equals(user.getName())) {
                //user is Owner of game
//                ControllerHelperService chs = new ControllerHelperService();
//                chs.deleteGame(game, userRepo, wagonRepo, marshalRepo);
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
//        logger.debug("modify user: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/topLevel/users/" + userId);
//        Game game = gameRepo.findOne(gameId);
//        Wagon wagon = wagonRepo.findOne(wagonId);
//        WagonLevel wagonLevel = wagonLevelRepo.findOne(wagon.getTopLevel().getId());
//        User userOld = userRepo.findOne(userId);
//
//        GameLogicService gls = new GameLogicService();
//        gls.modifyUser(userOld, user, wagonLevel, game, wagonLevelRepo, gameRepo);


        // return "success";

    }

//    @RequestMapping(value = CONTEXT + "/{gameId}/users/{userId}")
//    @ResponseStatus(HttpStatus.OK)
//    public User getUser(@PathVariable Long gameId, @PathVariable Integer userId) {
//        logger.debug("getUser: " + gameId);
//
//        Game game = gameRepo.findOne(gameId);
//
//        return game.getUsers().get(userId);
//    }
    //endregion

    //region /{gameId}/wagons
//    @RequestMapping(value = CONTEXT + "/{gameId}/wagons")
//    @ResponseStatus(HttpStatus.OK)
//    public List<Wagon> listWagons(@PathVariable Long gameId) {
//        logger.debug("listWagons");
//
//        Game game = gameRepo.findOne(gameId);
//        if (game != null) {
//            return game.getWagons();
//        }
//
//        return null;
//    }
    //endregion

//    //region /{gameId}/wagons/{wagonId}/topLevel
//    @RequestMapping(value = CONTEXT + "/{gameId}/wagons/{wagonId}/topLevel")
//    @ResponseStatus(HttpStatus.OK)
//    public WagonLevel getWagonTopLevel(@PathVariable Long gameId, @PathVariable Integer wagonId) {
//        logger.debug("list topLevel of wagon " + wagonId);
//        Game game = gameRepo.findOne(gameId);
//        if (game != null) {
//            Wagon wagon = game.getWagons().get(wagonId);
//            if (wagon != null) {
//                return game.getWagons().get(wagonId).getTopLevel();
//            } else {
//                logger.error("wagon not found");
//                return null;
//            }
//        } else {
//            logger.error("game is null");
//            return null;
//        }
//    }
//    //endregion

//    //region /{gameId}/wagons/{wagonId}/bottomLevel
//    @RequestMapping(value = CONTEXT + "/{gameId}/wagons/{wagonId}/bottomLevel")
//    @ResponseStatus(HttpStatus.OK)
//    public WagonLevel getWagonBottomLevel(@PathVariable Long gameId, @PathVariable Integer wagonId) {
//        logger.debug("list bottomLevel of wagon " + wagonId);
//        Game game = gameRepo.findOne(gameId);
//        if (game != null) {
//            Wagon wagon = game.getWagons().get(wagonId);
//            if (wagon != null) {
//                return game.getWagons().get(wagonId).getBottomLevel();
//            } else {
//                logger.error("wagon not found");
//                return null;
//            }
//        } else {
//            logger.error("game is null");
//            return null;
//        }
//    }
//    //endregion


    @RequestMapping(value = CONTEXT + "/{gameId}/wagons/{wagonId}/topLevel/users", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public String addUserTopLevel(@PathVariable Long gameId, @PathVariable Integer wagonId, @RequestParam("token") String userToken) {
        logger.debug("add User to: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/topLevel/users");

        Game game = gameRepo.findOne(gameId);
        if (game != null) {
            Wagon wagon = game.getWagons().get(wagonId - 1);
            if (wagon != null) {
                User user = userRepo.findByToken(userToken);
                if (user != null) {
                    wagon.getTopLevel().getUsers().add(user);
                    user.setWagonLevel(wagon.getTopLevel());
                    // wagonLevelRepo.save(wagon.getTopLevel());
                    userRepo.save(user);
                    return "User successfully added to: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/topLevel/users";
                } else {
                    return "no existing user found";
                }
            } else {
                return "no wagon found";
            }
        } else {
            return "no game found";
        }
    }

    @RequestMapping(value = CONTEXT + "/{gameId}/wagons/{wagonId}/bottomLevel/users", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public String addUserBottomLevel(@PathVariable Long gameId, @PathVariable Integer wagonId, @RequestParam("token") String userToken) {
        logger.debug("add User to: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/bottomLevel/users");

        Game game = gameRepo.findOne(gameId);
        if (game != null) {
            Wagon wagon = game.getWagons().get(wagonId - 1);
            if (wagon != null) {
                User user = userRepo.findByToken(userToken);
                if (user != null) {
                    wagon.getBottomLevel().getUsers().add(user);
                    user.setWagonLevel(wagon.getBottomLevel());
                    // wagonLevelRepo.save(wagon.getTopLevel());
                    userRepo.save(user);
                    return "User successfully added to: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/bottomLevel/users";
                } else {
                    return "no existing user found";
                }
            } else {
                return "no wagon found";
            }
        } else {
            return "no game found";
        }
    }

    @RequestMapping(value = CONTEXT + "/{gameId}/wagons/{wagonId}/topLevel/users/{userId}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public String modifyUserTopLevel(@RequestBody User user, @PathVariable Long gameId, @PathVariable Long wagonId, @PathVariable Long userId) {
        logger.debug("modify user: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/topLevel/users/" + userId);
        Game game = gameRepo.findOne(gameId);
        Wagon wagon = wagonRepo.findOne(wagonId);
        WagonLevel wagonLevel = wagonLevelRepo.findOne(wagon.getTopLevel().getId());
        User userOld = userRepo.findOne(userId);

        GameLogicService gls = new GameLogicService();
        gls.modifyUser(userOld, user, wagonLevel, game, wagonLevelRepo, gameRepo);
//        WagonLevel wagonLevelNew = wagonLevelRepo.findOne(user.getWagonLevelIdNew());
//        wagonLevel.getUsers().remove(user1);
//        gameRepo.save(game);
//        wagonLevelNew.getUsers().add(user1);
//        user1.setWagonLevel(wagonLevelNew);
//
//        gameRepo.save(game);

        return "User successfully modified on: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/topLevel/users/" + userId;

    }

    @RequestMapping(value = CONTEXT + "/{gameId}/wagons/{wagonId}/bottomLevel/users/{userId}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public String modifyUserBottomLevel(@RequestBody User user, @PathVariable Long gameId, @PathVariable Integer wagonId, @PathVariable Integer userId) {
        logger.debug("modify user: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/bottomLevel/users/" + userId);
        Game game = gameRepo.findOne(gameId);
        if (game != null) {
            Wagon wagon = game.getWagons().get(wagonId - 1);
            if (wagon != null) {
                User userExisting = wagon.getBottomLevel().getUsers().get(userId - 1);
                if (userExisting != null) {
                    wagon.getBottomLevel().getUsers().set(userId - 1, user);
                    userExisting = user;
                    userRepo.save(user);
                    userRepo.save(userExisting);
                    return "User successfully modified on: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/bottomLevel/users/" + userId;
                } else {
                    return "no existing user found";
                }
            } else {
                return "no wagon found";
            }
        } else {
            return "no game found";
        }
    }

    @RequestMapping(value = CONTEXT + "/{gameId}/wagons/{wagonId}/bottomLevel/marshal", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public String addMarshalBottomLevel(@RequestBody Marshal marshal, @PathVariable Long gameId, @PathVariable Integer wagonId) {
        logger.debug("add Marshal to: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/bottomLevel");

        Game game = gameRepo.findOne(gameId);
        if (game != null) {
            Wagon wagon = game.getWagons().get(wagonId - 1);
            if (wagon != null) {
                if (marshal != null) {
                    wagon.getBottomLevel().setMarshal(marshal);
                    marshal.setWagonLevel(wagon.getBottomLevel());
                    game.setMarshal(marshal);
                    marshal.setGame(game);
                    marshalRepo.save(marshal);
                    return "Marshal successfully added to: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/bottomLevel";
                } else {
                    return "marshal is null";
                }
            } else {
                return "no wagon found";
            }
        } else {
            return "no game found";
        }
    }

    @RequestMapping(value = CONTEXT + "/{gameId}/wagons/{wagonId}/topLevel/items", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public String addItemTopLevel(@RequestBody Item item, @PathVariable Long gameId, @PathVariable Integer wagonId) {
        logger.debug("add item to: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/topLevel/items");

        Game game = gameRepo.findOne(gameId);
        if (game != null) {
            Wagon wagon = game.getWagons().get(wagonId - 1);
            if (wagon != null) {
                if (item != null) {
                    wagon.getTopLevel().getItems().add(item);
                    item.setWagonLevel(wagon.getTopLevel());
                    // wagonLevelRepo.save(wagon.getTopLevel());
                    itemRepo.save(item);
                    return "Item successfully added to: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/topLevel/items";
                } else {
                    return "item is null";
                }
            } else {
                return "no wagon found";
            }
        } else {
            return "no game found";
        }
    }

    @RequestMapping(value = CONTEXT + "/{gameId}/wagons/{wagonId}/bottomLevel/items", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public String addItemBottomLevel(@RequestBody Item item, @PathVariable Long gameId, @PathVariable Integer wagonId) {
        logger.debug("add item to: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/bottomLevel/items");

        Game game = gameRepo.findOne(gameId);
        if (game != null) {
            Wagon wagon = game.getWagons().get(wagonId - 1);
            if (wagon != null) {
                if (item != null) {
                    wagon.getBottomLevel().getItems().add(item);
                    item.setWagonLevel(wagon.getBottomLevel());
                    // wagonLevelRepo.save(wagon.getTopLevel());
                    itemRepo.save(item);
                    return "Item successfully added to: " + CONTEXT + "/" + gameId + "/wagons/" + wagonId + "/bottomLevel/items";
                } else {
                    return "item is null";
                }
            } else {
                return "no wagon found";
            }
        } else {
            return "no game found";
        }
    }

    @RequestMapping(value = CONTEXT + "/{gameId}/users/{userId}/items", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public String addItemUser(@RequestBody Item item, @PathVariable Long gameId, @PathVariable Integer userId) {
        logger.debug("add Item to: " + CONTEXT + "/" + gameId + "/users/" + userId + "/items");

        Game game = gameRepo.findOne(gameId);
        if (game != null) {
            User user = game.getUsers().get(userId - 1);
            if (userId != null) {
                if (item != null) {
                    user.getItems().add(item);
                    item.setUser(user);
                    itemRepo.save(item);
                    return "Item successfully added to: " + CONTEXT + "/" + gameId + "/users/" + userId + "/items";
                } else {
                    return "item is null";
                }
            } else {
                return "no user found";
            }
        } else {
            return "no game found";
        }
    }
}