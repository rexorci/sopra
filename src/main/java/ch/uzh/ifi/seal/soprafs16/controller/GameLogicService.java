package ch.uzh.ifi.seal.soprafs16.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.smartcardio.Card;

import ch.uzh.ifi.seal.soprafs16.GameConstants;
import ch.uzh.ifi.seal.soprafs16.constant.PhaseType;
import ch.uzh.ifi.seal.soprafs16.model.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.Turn;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.service.ActionResponseListener;

/**
 * Created by Nico on 05.04.2016.
 */
public class GameLogicService {
    Game game;
    int actionCounter = 0;
    int roundStarter = 0;

    Logger logger = LoggerFactory.getLogger(GameLogicService.class);

    public GameLogicService(Game game) {
        this.game = game;
    }

    public void update() {
        int currentPlayer = game.getCurrentPlayer();
        List<User> users = game.getUsers();

        logger.debug("Game " + game.getId() + " - turn_" + game.getCurrentRound() + "." + game.getCurrentTurn() +
        " - " + game.getCurrentPhase().toString());
        logger.debug("actionCounter: " + actionCounter);

        if (game.getCurrentPhase() == PhaseType.PLANNING) {
            if (actionCounter > 0) {
                setNextTurn(users.size());
                if(game.getCurrentTurn() < game.getRoundCardDeck().get(game.getCurrentRound()).getPattern().length) {
                    setNextPlayer(users.size());
                }
            }

            if (game.getCurrentTurn() == game.getRoundCardDeck().get(game.getCurrentRound()).getPattern().length) {
                logger.debug("phase_planning_end");
                game.setCurrentPhase(PhaseType.EXECUTION);
            } else {
                processPlayerTurn(currentPlayer, users.size());
            }
        }
        if (game.getCurrentPhase() == PhaseType.EXECUTION) {
            logger.debug("phase_execution_start");

            if(game.getCommonDeck().size() > 0) {
                game.getCommonDeck().remove(0).createActionRequest();
            }
            else {
                game.setCurrentRound(game.getCurrentRound() + 1);
                if (game.getCurrentRound() == GameConstants.ROUNDS) {
                    finishGame();
                } else {
                    game.setCurrentPlayer(++roundStarter);
                    game.setCurrentTurn(0);
                    game.setCurrentPhase(PhaseType.PLANNING);
                }
            }
        }
    }

    private void processPlayerTurn(int currentPlayer, int playerCounter) {
        createDOPCRequestDTO();
        actionCounter++;
    }

    private void setNextTurn(int playerCounter) {
        // Turn end
        if ((game.getCurrentTurnType() != Turn.Type.SPEEDUP && actionCounter % playerCounter == 0)
                || (game.getCurrentTurnType() == Turn.Type.SPEEDUP && actionCounter == (game.getCurrentTurn() + 2) * playerCounter)) {
            logger.debug("Turn " + game.getCurrentTurn() + " end");
            game.setCurrentTurn(game.getCurrentTurn() + 1);
            if(game.getCurrentTurn() < game.getRoundCardDeck().get(
                    game.getCurrentRound()).getPattern().length
                    && game.getCurrentTurnType() == Turn.Type.REVERSE) {
                game.setCurrentPlayer(roundStarter + 1); // correction
            }
        }
    }

    private void finishGame() {
    }

    private void processCommonDeck() {
        // TODO: Discuss interface vs abstract class
        // List<ActionCard> commonDeck = game.getCommonDeck();
        // ActionCard ac = commonDeck.remove(0);
        //
    }

    private void setNextPlayer(int playerCounter) {
        if (game.getCurrentTurnType() != Turn.Type.SPEEDUP || actionCounter % 2 == 0) {
            if(game.getCurrentTurnType() == Turn.Type.REVERSE){
                game.setCurrentPlayer(mod(game.getCurrentPlayer() - 1, playerCounter));

            }
            else {
                game.setCurrentPlayer(mod(game.getCurrentPlayer() + 1, playerCounter));
            }
        }
    }

    private void createDOPCRequestDTO() {
        logger.debug("DOPCrequest created");
        game.getCommonDeck().add(new ActionCard() {
            @Override
            public void createActionRequest() {
                logger.debug("action_request_created");
            }
        });
    }

    private int mod(int a, int b){
        int ret = a % b;
        return ret < 0 ? b + ret : ret;
    }
}