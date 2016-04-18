package ch.uzh.ifi.seal.soprafs16.controller;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.seal.soprafs16.Application;
import ch.uzh.ifi.seal.soprafs16.GameConstants;
import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.constant.PhaseType;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.cards.Card;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.PivotablePoleCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.RoundCard;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CharacterRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonRepository;
import ch.uzh.ifi.seal.soprafs16.model.turns.ReverseTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.SpeedupTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;

/**
 * Created by Nico on 05.04.2016.
 */
@Service("glservice")
@Transactional
public class GameLogicService extends GenericService {

    Logger logger = LoggerFactory.getLogger(GameLogicService.class);
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
    @Autowired
    private CharacterRepository characterRepo;
    @Autowired
    private CardRepository cardRepo;
    @Autowired
    private DeckRepository deckRepo;
    //endregion

    public void update(Long id) {
        if (!gameRepo.exists(id)) return;
        Game game = gameRepo.findOne(id);
        Hibernate.initialize(game.getCommonDeck());
        Hibernate.initialize(game.getCurrentPhase());
        Hibernate.initialize(game.getCurrentPlayer());
        Hibernate.initialize(game.getActionCounter());
        Hibernate.initialize(game.getRoundStarter());
        Hibernate.initialize(game.getRoundCardDeck());
        Hibernate.initialize(game.getUsers());

        int currentPlayer = game.getCurrentPlayer();
        List<User> users = game.getUsers();

        if (game.getCurrentPhase() == PhaseType.PLANNING) {
            if (game.getActionCounter() > 0) {
                setNextTurn(game, game.getUsers().size());
                if (game.getCurrentTurn() < getCurrentTurns(game).size()) {
                    setNextPlayer(game, game.getUsers().size());
                }
            }

            if (game.getCurrentTurn() == getCurrentTurns(game).size()) {
                logger.debug("phase_planning_end");
                game.setCurrentPhase(PhaseType.EXECUTION);
            } else {
                processPlayerTurn(game, currentPlayer, users.size());
            }
        }
        if (game.getCurrentPhase() == PhaseType.EXECUTION) {
            logger.debug("phase_execution_start");

            if (game.getCommonDeck().size() > 0) {
                game.getCommonDeck().remove(0);
            } else {
                game.setCurrentRound(game.getCurrentRound() + 1);
                if (game.getCurrentRound().equals(GameConstants.ROUNDS)) {
                    finishGame(game);
                } else {
                    game.setRoundStarter(game.getRoundStarter() + 1);
                    game.setCurrentPlayer(game.getRoundStarter());
                    game.setCurrentTurn(0);
                    game.setCurrentPhase(PhaseType.PLANNING);
                }
            }
        }

        gameRepo.save(game);
    }

    private void processPlayerTurn(Game game, int currentPlayer, int playerCounter) {
        createDOPCRequestDTO(game);
        game.setActionCounter(game.getActionCounter() + 1);
    }

    private void setNextTurn(Game game, int playerCounter) {
        // Turn end
        if ((!(game.getCurrentTurnType() instanceof SpeedupTurn) && game.getActionCounter() % playerCounter == 0)
                || (game.getCurrentTurnType() instanceof SpeedupTurn && game.getActionCounter() == (game.getCurrentTurn() + 2) * playerCounter)) {
            logger.debug("Turn " + game.getCurrentTurnType() + " end");
            game.setCurrentTurn(game.getCurrentTurn() + 1);
            if (game.getCurrentTurn() < ((RoundCard)(game.getRoundCardDeck().get(
                    (game.getCurrentRound())))).getPattern().size()
                    && game.getCurrentTurnType() instanceof ReverseTurn) {
                game.setCurrentPlayer(game.getRoundStarter() + 1); // correction
            }
        }
    }

    private void finishGame(Game game) {
        game.setStatus(GameStatus.FINISHED);
        gameRepo.save(game);
    }

    private void setNextPlayer(Game game, int playerCounter) {
        Turn t = game.getCurrentTurnType();
        if (!(t instanceof SpeedupTurn) || game.getActionCounter() % 2 == 0) {
            if (t instanceof ReverseTurn) {
                game.setCurrentPlayer(mod(game.getCurrentPlayer() - 1, playerCounter));

            } else {
                game.setCurrentPlayer(mod(game.getCurrentPlayer() + 1, playerCounter));
            }
        }
    }

    private void createDOPCRequestDTO(Game game) {
        logger.debug("DOPCrequest created");
        Card c = new Card();
        c.setDeck(game.getCommonDeck());
        cardRepo.save(c);
        game.getCommonDeck().add(c);
        deckRepo.save(game.getCommonDeck());
    }

    private int mod(int a, int b) {
        int ret = a % b;
        return ret < 0 ? b + ret : ret;
    }

    private List<Turn> getCurrentTurns(Game game) {
        RoundCard rc = (RoundCard)game.getRoundCardDeck().get(game.getCurrentRound());
        ArrayList<Turn> turns = rc.getPattern();
        return turns;
    }
}