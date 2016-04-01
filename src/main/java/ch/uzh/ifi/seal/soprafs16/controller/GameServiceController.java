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
import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.Move;
import ch.uzh.ifi.seal.soprafs16.model.Train;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.Wagon;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;

@RestController
public class GameServiceController extends GenericService {

	Logger logger = LoggerFactory.getLogger(GameServiceController.class);

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private GameRepository gameRepo;

	private final String CONTEXT = "/games";

	/*
	 * Context: /game
	 */

	@RequestMapping(value = CONTEXT)
	@ResponseStatus(HttpStatus.OK)
	public List<Game> listGames() {
		logger.debug("listGames");
		List<Game> result = new ArrayList<>();
//		gameRepo.findAll().forEach(result::add);

        for (Game game : gameRepo.findAll()){
            result.add(game);
        }



		return result;
	}

	@RequestMapping(value = CONTEXT, method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public String addGame(@RequestBody Game game, @RequestParam("token") String userToken) {
		logger.debug("addGame: " + game);

		// @Column(nullable = false)
		// private String name;
		//
		// @Column(nullable = false)
		// private String owner;
		//
		// @Column
		// private GameStatus status;
		//
		// @Column
		// private Integer currentPlayer;
		// game.setName("timonsGame");

		// game.setStatus(GameStatus.PENDING);

		User owner = userRepo.findByToken(userToken);

		// game.setCurrentPlayer(0);

		if (owner != null) {
            game.setName("timonsGame");
            game.setStatus(GameStatus.PENDING);

            owner.setGame(game);

			// TODO Mapping into Game
			game.setOwner(owner.getName());
            game.setCurrentPlayer(0);
			game = gameRepo.save(game);

			List<User> players = new ArrayList<User>();
			players.add(owner);
			game.setUsers(players);

			return CONTEXT + "/" + game.getId();
		} else {
			return "no owner found";
		}

		// return null;
	}

	/*
	 * Context: /game/{game-id}
	 */
	@RequestMapping(value = CONTEXT + "/{gameId}")
	@ResponseStatus(HttpStatus.OK)
	public Game getGame(@PathVariable Long gameId) {
		logger.debug("getGame: " + gameId);

		Game game = gameRepo.findOne(gameId);

		return game;
	}

	@RequestMapping(value = CONTEXT + "/{gameId}/start", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void startGame(@PathVariable Long gameId, @RequestParam("token") String userToken) {
		logger.debug("startGame: " + gameId);

		Game game = gameRepo.findOne(gameId);
		User owner = userRepo.findByToken(userToken);

		if (owner != null && game != null && game.getOwner().equals(owner.getUsername())) {
			// TODO: Start game
		}
	}

	@RequestMapping(value = CONTEXT + "/{gameId}/stop", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void stopGame(@PathVariable Long gameId, @RequestParam("token") String userToken) {
		logger.debug("stopGame: " + gameId);

		Game game = gameRepo.findOne(gameId);
		User owner = userRepo.findByToken(userToken);

		if (owner != null && game != null && game.getOwner().equals(owner.getUsername())) {
			// TODO: Stop game
		}
	}

	/*
	 * Context: /game/{game-id}/move
	 */
	// @RequestMapping(value = CONTEXT + "/{gameId}/move")
	// @ResponseStatus(HttpStatus.OK)
	// public List<Move> listMoves(@PathVariable Long gameId) {
	// logger.debug("listMoves");
	//
	// Game game = gameRepo.findOne(gameId);
	// if (game != null) {
	// return game.getMoves();
	// }
	//
	// return null;
	// }

	@RequestMapping(value = CONTEXT + "/{gameId}/move", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void addMove(@RequestBody Move move) {
		logger.debug("addMove: " + move);
		// TODO Mapping into Move + execution of move
	}

	@RequestMapping(value = CONTEXT + "/{gameId}/move/{moveId}")
	@ResponseStatus(HttpStatus.OK)
	public Move getMove(@PathVariable Long gameId, @PathVariable Integer moveId) {
		logger.debug("getMove: " + gameId);

		Game game = gameRepo.findOne(gameId);
		if (game != null) {
			return game.getMoves().get(moveId);
		}

		return null;
	}

	/*
	 * Context: /game/{game-id}/players
	 */
	@RequestMapping(value = CONTEXT + "/{gameId}/players")
	@ResponseStatus(HttpStatus.OK)
	public List<User> listPlayers(@PathVariable Long gameId) {
		logger.debug("listPlayers");

		Game game = gameRepo.findOne(gameId);
		if (game != null) {
			return game.getUsers();
		}

		return null;
	}

	@RequestMapping(value = CONTEXT + "/{gameId}/players", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public String addPlayer(@PathVariable Long gameId, @RequestParam("token") String userToken) {
		logger.debug("addPlayer: " + userToken);

		Game game = gameRepo.findOne(gameId);
		User player = userRepo.findByToken(userToken);

		if (game != null && player != null && game.getUsers().size() < GameConstants.MAX_PLAYERS) {
			game.getUsers().add(player);
			logger.debug("Game: " + game.getName() + " - player added: " + player.getUsername());
			return CONTEXT + "/" + gameId + "/player/" + (game.getUsers().size() - 1);
		} else {
			logger.error("Error adding player with token: " + userToken);
		}
		return null;
	}

	@RequestMapping(value = CONTEXT + "/{gameId}/players/{playerId}")
	@ResponseStatus(HttpStatus.OK)
	public User getPlayer(@PathVariable Long gameId, @PathVariable Integer playerId) {
		logger.debug("getPlayer: " + gameId);

		Game game = gameRepo.findOne(gameId);

		return game.getUsers().get(playerId);
	}

	/*
	 * Context: /game/{game-id}/train
	 */
	@RequestMapping(value = CONTEXT + "/{gameId}/train")
	@ResponseStatus(HttpStatus.OK)
	public List<Wagon> listWagons(@PathVariable Long gameId) {
		// logger.debug("listPlayers");

		Game game = gameRepo.findOne(gameId);
		if (game != null) {
			Train train = game.getTrain();

			return train.getWagons();
		}

		return null;
	}

	/*
	 * Context: /game/{game-id}/train/wagons/2
	 */
}