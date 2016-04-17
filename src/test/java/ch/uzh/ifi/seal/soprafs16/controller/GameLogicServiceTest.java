package ch.uzh.ifi.seal.soprafs16.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import ch.uzh.ifi.seal.soprafs16.Application;
import ch.uzh.ifi.seal.soprafs16.constant.PhaseType;
import ch.uzh.ifi.seal.soprafs16.model.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.RoundCard;
import ch.uzh.ifi.seal.soprafs16.model.Turn;
import ch.uzh.ifi.seal.soprafs16.model.User;

/**
 * Created by Nico on 13.04.2016.
 */


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
public class GameLogicServiceTest {
    @Value("${local.server.port}")
    private int port;
    private Long gameId;

    private Game tester;
    private URL base;
    private RestTemplate template;

    @Before
    public void setUp()
            throws Exception {
        this.base = new URL("http://localhost:" + port + "/");
        this.template = new TestRestTemplate();
    }

    @Before
    public void init() {
        tester = new Game();
        gameId = template.postForObject(base + "games", tester, Long.class);

        List<User> users = new LinkedList<>();
        GameDeck<RoundCard> roundCardDeck = new GameDeck<>();

        users.add(new User());
        users.add(new User());
        users.add(new User());
        users.add(new User());

        Turn t[] = {new Turn(Turn.Type.NORMAL), new Turn(Turn.Type.SPEEDUP), new Turn(Turn.Type.REVERSE)};

        RoundCard rc = new RoundCard();
        rc.setPattern(t);

        roundCardDeck.add(rc);
        roundCardDeck.add(rc);
        roundCardDeck.add(rc);
        roundCardDeck.add(rc);
        roundCardDeck.add(rc);

        tester.setUsers(users);
        tester.setCurrentPhase(PhaseType.PLANNING);
        tester.setCurrentPlayer(0);
        tester.setRoundCardDeck(roundCardDeck);
        tester.setCurrentRound(0);
        tester.setCurrentTurn(0);
        tester.setCommonDeck(new GameDeck<>());
    }

    @Test
    public void gls_nextPlayerIsCorrect() {
        GameLogicService gls = new GameLogicService(tester);// 4 responses for Normal Turn

        gls.update();
        template.put(base + "games/" + gameId, tester);
        Game serverGame = template.getForObject(base + "games/" + gameId, Game.class);
        assertEquals(0, (long) serverGame.getCurrentPlayer());
        gls.update();
        template.put(base + "games/" + gameId, tester);
        assertEquals(1, (long) tester.getCurrentPlayer());
        gls.update();
        template.put(base + "games/" + gameId, tester);
        assertEquals(2, (long) tester.getCurrentPlayer());
        gls.update();
        template.put(base + "games/" + gameId, tester);
        assertEquals(3, (long) tester.getCurrentPlayer());

        // 8 responses for Speed-Up-Turn
        gls.update();
        assertEquals(0, (long) tester.getCurrentPlayer());
        gls.update();
        assertEquals(0, (long) tester.getCurrentPlayer());
        gls.update();
        assertEquals(1, (long) tester.getCurrentPlayer());
        gls.update();
        assertEquals(1, (long) tester.getCurrentPlayer());
        gls.update();
        assertEquals(2, (long) tester.getCurrentPlayer());
        gls.update();
        assertEquals(2, (long) tester.getCurrentPlayer());
        gls.update();
        assertEquals(3, (long) tester.getCurrentPlayer());
        gls.update();
        assertEquals(3, (long) tester.getCurrentPlayer());
        gls.update();
        // eighth response triggers turn change
        assertEquals(0, (long) tester.getCurrentPlayer());
        gls.update();
        assertEquals(3, (long) tester.getCurrentPlayer());
        gls.update();
        assertEquals(2, (long) tester.getCurrentPlayer());
        gls.update();
        // 4th response triggers turn change
        assertEquals(1, (long) tester.getCurrentPlayer());
        
    }

    @Test
    public void gls_TurnIsCorrect() {
        GameLogicService gls = new GameLogicService(tester);
        assertEquals(0, (long) tester.getCurrentTurn());
        // initial update call
        gls.update();

        // 4 responses for Normal Turn
        gls.update();
        assertEquals(0, (long) tester.getCurrentTurn());
        gls.update();
        assertEquals(0, (long) tester.getCurrentTurn());
        gls.update();
        assertEquals(0, (long) tester.getCurrentTurn());
        gls.update();
        // 4th response triggers turn change
        assertEquals(1, (long) tester.getCurrentTurn());

        // 8 responses for Speed-Up-Turn
        gls.update();
        assertEquals(1, (long) tester.getCurrentTurn());
        gls.update();
        assertEquals(1, (long) tester.getCurrentTurn());
        gls.update();
        assertEquals(1, (long) tester.getCurrentTurn());
        gls.update();
        assertEquals(1, (long) tester.getCurrentTurn());
        gls.update();
        assertEquals(1, (long) tester.getCurrentTurn());
        gls.update();
        assertEquals(1, (long) tester.getCurrentTurn());
        gls.update();
        assertEquals(1, (long) tester.getCurrentTurn());
        gls.update();
        // eighth response triggers turn change
        assertEquals(2, (long) tester.getCurrentTurn());


        // 4 Responses for Normal-Turn
        gls.update();
        assertEquals(2, (long) tester.getCurrentTurn());
        gls.update();
        assertEquals(2, (long) tester.getCurrentTurn());
        gls.update();
        assertEquals(2, (long) tester.getCurrentTurn());
        gls.update();
        // 4th response triggers turn change
        //assertEquals(0, (long) tester.getCurrentTurn());
    }

    @Test
    public void gls_PhaseIsCorrect() {
        GameLogicService gls = new GameLogicService(tester);
        // initial call to update
        gls.update();

        // simulate 16 ActionResponses (first round)
        for (int i = 0; i < 16; i++) {
            assertEquals(PhaseType.PLANNING, tester.getCurrentPhase());
            gls.update();
        }
        assertEquals(PhaseType.EXECUTION, tester.getCurrentPhase());
    }
    @Test
    public void gls_RoundIsCorrect() {
        GameLogicService gls = new GameLogicService(tester);
        // initial call to update
        gls.update();
        assertEquals(0, (long)tester.getCurrentRound());
        // simulate 16 ActionResponses (first round)
        for (int i = 0; i < 16; i++) {
            gls.update();
        }

        while(tester.getCommonDeck().size() > 0){
            assertEquals(0, (long)tester.getCurrentRound());
            gls.update();
        }
        // last update call for ActionCard - Actions
        gls.update();
        assertEquals(1, (long)tester.getCurrentRound());
    }
}
