package ch.uzh.ifi.seal.soprafs16.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.UUID;

import ch.uzh.ifi.seal.soprafs16.Application;
import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.constant.PhaseType;
import ch.uzh.ifi.seal.soprafs16.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.RoundCard;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CharacterRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.TurnRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonRepository;
import ch.uzh.ifi.seal.soprafs16.model.turns.NormalTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.ReverseTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.SpeedupTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;
import ch.uzh.ifi.seal.soprafs16.service.GameLogicService;

/**
 * Created by Nico on 13.04.2016.
 */


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
public class GameLogicServiceTest {
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
    @Autowired
    private TurnRepository turnRepo;
    @Autowired
    private GameLogicService gls;

    private Game tester;
    private Long gameId;

    @Before
    public void init() {
        tester = new Game();

        ArrayList<User> users = new ArrayList<>();
        GameDeck<RoundCard> roundCardDeck = new GameDeck<>();
        deckRepo.save(roundCardDeck);
        GameDeck<ActionCard> commonDeck = new GameDeck<>();
        deckRepo.save(roundCardDeck);

        User owner = new User();
        owner.setStatus(UserStatus.ONLINE);
        String token = UUID.randomUUID().toString();
        owner.setToken(token);
        owner.setItems(new ArrayList<>());
        owner.setName("GLSTest1Owner" + Math.random() * 999);
        owner.setUsername("GLSTest1Owner" + Math.random() * 999);
        owner = userRepo.save(owner);

        owner.setGame(tester);
        users.add(owner);

        tester.setStatus(GameStatus.PENDING);
        tester.setOwner(owner.getName());
        tester.setName("GameLogicServiceTest");

        gameRepo.save(tester);

        for(int i = 0; i < 4; i++){
            User user = new User();
            user.setName("a" + tester.getId() + i);
            user.setUsername("a" + tester.getId() + i);
            user.setGame(tester);
            user.setToken(UUID.randomUUID().toString());
            user.setStatus(UserStatus.ONLINE);
            userRepo.save(user);
            users.add(user);
        }
        gameRepo.save(tester);

        tester.setUsers(users);
        tester.setCurrentPhase(PhaseType.PLANNING);
        tester.setCurrentPlayer(0);
        tester.setRoundCardDeck(roundCardDeck);
        tester.setCurrentRound(0);
        tester.setCurrentTurn(0);
        tester.setStatus(GameStatus.RUNNING);
        tester.setCommonDeck(commonDeck);
        tester.setOwner(owner.getName());

        // Create RoundCards
        for(int i = 0; i < 5; i++) {
            Turn normal = new NormalTurn();
            Turn speedup = new SpeedupTurn();
            Turn reverse = new ReverseTurn();
            turnRepo.save(normal);
            turnRepo.save(speedup);
            turnRepo.save(reverse);

            ArrayList<Turn> turns = new ArrayList<>();

            turns.add(normal);
            turns.add(speedup);
            turns.add(reverse);

            RoundCard rc = new RoundCard();
            rc.setPattern(turns);
            rc.setDeck(roundCardDeck);
            cardRepo.save(rc);

            normal.setRoundCard(rc);
            speedup.setRoundCard(rc);
            reverse.setRoundCard(rc);

            roundCardDeck.add(rc);
            cardRepo.save(rc);
        }
        deckRepo.save(roundCardDeck);
        deckRepo.save(commonDeck);

        tester.setStatus(GameStatus.PENDING);
        gameRepo.save(tester);

        gameId = tester.getId();
    }

    @Test
    public void gls_nextPlayerIsCorrect() {
        for(int i = 0; i < 5; i++) {
            // 4 responses for Normal Turn
            tester = gameRepo.findOne(gameId);
            assertEquals((0+i)%4, (long) tester.getCurrentPlayer());
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            assertEquals((1+i)%4, (long) tester.getCurrentPlayer());
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            assertEquals((2+i)%4, (long) tester.getCurrentPlayer());
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            assertEquals((3+i)%4, (long) tester.getCurrentPlayer());

            // 8 responses for Speed-Up-Turn
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            assertEquals((0+i)%4, (long) tester.getCurrentPlayer());
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            assertEquals((0+i)%4, (long) tester.getCurrentPlayer());
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            assertEquals((1+i)%4, (long) tester.getCurrentPlayer());
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            assertEquals((1+i)%4, (long) tester.getCurrentPlayer());
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            assertEquals((2+i)%4, (long) tester.getCurrentPlayer());
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            assertEquals((2+i)%4, (long) tester.getCurrentPlayer());
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            assertEquals((3+i)%4, (long) tester.getCurrentPlayer());
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            assertEquals((3+i)%4, (long) tester.getCurrentPlayer());
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            // eighth response triggers turn change
            assertEquals((0+i)%4, (long) tester.getCurrentPlayer());
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            assertEquals((3+i)%4, (long) tester.getCurrentPlayer());
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            assertEquals((2+i)%4, (long) tester.getCurrentPlayer());
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            assertEquals((1+i)%4, (long) tester.getCurrentPlayer());
            tester = gameRepo.findOne(gameId);
            gls.update(tester.getId());
            tester = gameRepo.findOne(gameId);
            for(int y = 0; y < 16; y++){
                gls.update(tester.getId());
            }
        }
    }

    @Test
    public void gls_TurnIsCorrect() {
        tester = gameRepo.findOne(gameId);
        assertEquals(0, (long) tester.getCurrentTurn());

        // 4 responses for Normal Turn
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        assertEquals(0, (long) tester.getCurrentTurn());
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        assertEquals(0, (long) tester.getCurrentTurn());
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        assertEquals(0, (long) tester.getCurrentTurn());
        gls.update(tester.getId());
        // 4th response triggers turn change
        tester = gameRepo.findOne(gameId);
        assertEquals(1, (long) tester.getCurrentTurn());

        // 8 responses for Speed-Up-Turn
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        assertEquals(1, (long) tester.getCurrentTurn());
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        assertEquals(1, (long) tester.getCurrentTurn());
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        assertEquals(1, (long) tester.getCurrentTurn());
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        assertEquals(1, (long) tester.getCurrentTurn());
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        assertEquals(1, (long) tester.getCurrentTurn());
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        assertEquals(1, (long) tester.getCurrentTurn());
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        assertEquals(1, (long) tester.getCurrentTurn());
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        // eighth response triggers turn change
        assertEquals(2, (long) tester.getCurrentTurn());


        // 4 Responses for Normal-Turn
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        assertEquals(2, (long) tester.getCurrentTurn());
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        assertEquals(2, (long) tester.getCurrentTurn());
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        assertEquals(2, (long) tester.getCurrentTurn());
        gls.update(tester.getId());
        // 4th response triggers turn change
        //assertEquals(0, (long) tester.getCurrentTurnType());
    }

    @Test
    public void gls_PhaseIsCorrect() {
        // initial call to update
        gls.update(tester.getId());
        tester = gameRepo.findOne(gameId);
        assertEquals(PhaseType.PLANNING, tester.getCurrentPhase());
        // simulate 16 ActionResponses (first round)
        for (int i = 0; i < 15; i++) {
            tester = gameRepo.findOne(gameId);
            assertEquals(PhaseType.PLANNING, tester.getCurrentPhase());
            gls.update(tester.getId());
        }
        tester = gameRepo.findOne(gameId);
        assertEquals(PhaseType.EXECUTION, tester.getCurrentPhase());
    }

    @Test
    public void gls_RoundIsCorrect() {
        // initial call to update
        tester = gameRepo.findOne(gameId);
        assertEquals(0, (long)tester.getCurrentRound());
        // simulate 16 ActionResponses (first round)
        for(int y = 0; y < 5; y++) {
            for (int i = 0; i < 16; i++) {
                gls.update(tester.getId());
            }

            // ActionResponses for ActionCards
            for (int i = 0; i < 16; i++) {
                tester = gameRepo.findOne(gameId);
                assertEquals(y, (long) tester.getCurrentRound());
                gls.update(tester.getId());
                tester = gameRepo.findOne(gameId);
            }
            // last update call for ActionCard - Actions
        }
    }

    @Test
    public void gls_GameFinishesCorrectly(){
        tester.setCurrentRound(4);
        tester.setCurrentPhase(PhaseType.EXECUTION);
        tester.setCurrentTurn(2);
        gls.update(tester.getId());
        gameRepo.save(tester);
    }


    /*
    * HELPER FUNCTIONS
     */
    private void simulateRound(Game game){
        for(int i = 0; i < 16; i++){
            gls.update(game.getId());
        }
    }
}