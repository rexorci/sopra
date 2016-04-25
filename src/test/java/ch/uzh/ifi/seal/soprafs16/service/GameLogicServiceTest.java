package ch.uzh.ifi.seal.soprafs16.service;

import org.hibernate.Hibernate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import ch.uzh.ifi.seal.soprafs16.Application;
import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.constant.PhaseType;
import ch.uzh.ifi.seal.soprafs16.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.UserAuthenticationWrapper;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
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
@Transactional
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

    @Value("${local.server.port}")
    private int port;

    private URL base;
    private RestTemplate template;
    private static int i = 0;

    @Before
    public void setUp()
            throws Exception {
        this.base = new URL("http://localhost:" + port + "/");
        this.template = new TestRestTemplate();
    }

    @Before
    public void init(){
        //region helper
        i++;
        User user1 = new User();
        user1.setName("name1_glServiceTest" + i);
        user1.setUsername("username1_glServiceTest" + i);
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);
        User user2 = new User();
        user2.setName("name2_glServiceTest" + i);
        user2.setUsername("username2_glServiceTest" + i);
        UserAuthenticationWrapper userAuthenticationWrapper2 = template.postForObject(base + "users", user2, UserAuthenticationWrapper.class);
        User user3 = new User();
        user3.setName("name3_glServiceTest" + i);
        user3.setUsername("username3_glServiceTest" + i);
        UserAuthenticationWrapper userAuthenticationWrapper3 = template.postForObject(base + "users", user3, UserAuthenticationWrapper.class);
        User user4 = new User();
        user4.setName("name4_glServiceTest" + i);
        user4.setUsername("username4_glServiceTest" + i);
        UserAuthenticationWrapper userAuthenticationWrapper4 = template.postForObject(base + "users", user4, UserAuthenticationWrapper.class);

        tester = new Game();
        tester.setName("game1_2_glServiceTest" + i);
        Long gameId1_2 = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.userToken, tester, Long.class);
        Long userIdGameJoined2 = template.postForObject(base + "games/" + gameId1_2 + "/users?token=" + userAuthenticationWrapper2.userToken, null, Long.class);
        Long userIdGameJoined3 = template.postForObject(base + "games/" + gameId1_2 + "/users?token=" + userAuthenticationWrapper3.userToken, null, Long.class);
        Long userIdGameJoined4 = template.postForObject(base + "games/" + gameId1_2 + "/users?token=" + userAuthenticationWrapper4.userToken, null, Long.class);

        String characterType1 = "Cheyenne";
        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper1.userToken)
                .queryParam("character", characterType1.toString());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, null, User.class);
        String characterType2 = "Ghost";
        UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper2.userToken)
                .queryParam("character", characterType2.toString());
        HttpEntity<User> userResponse2 = template.exchange(builder2.build().encode().toUri(), HttpMethod.PUT, null, User.class);
        String characterType3 = "Doc";
        UriComponentsBuilder builder3 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper3.userToken)
                .queryParam("character", characterType3.toString());
        HttpEntity<User> userResponse3 = template.exchange(builder3.build().encode().toUri(), HttpMethod.PUT, null, User.class);
        String characterType4 = "Tuco";
        UriComponentsBuilder builder4 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper4.userToken)
                .queryParam("character", characterType4.toString());
        HttpEntity<User> userResponse4 = template.exchange(builder4.build().encode().toUri(), HttpMethod.PUT, null, User.class);

        template.postForObject(base + "games/" + gameId1_2 + "/start?token=" + userAuthenticationWrapper1.userToken, null, Void.class);
        Game testerResponse = template.getForObject(base + "games/" + gameId1_2, Game.class);
        gameId = testerResponse.getId();

        tester = gameRepo.findOne(gameId);

        gameRepo.save(tester);

        tester.setCurrentPlayer(0);
        tester.setRoundStarter(0);
        GameDeck<ActionCard> roundCardDeck = (GameDeck<ActionCard>)deckRepo.findOne(tester.getRoundCardDeck().getId());
        Hibernate.initialize(roundCardDeck.getCards());
        roundCardDeck.getCards().clear();

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
        gameRepo.save(tester);
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
            for(int y = 0; y < 16; y++){
                gls.update(tester.getId());
            }
        }
    }

    @Test
    public void gls_TurnIsCorrect() {
       for(int i = 0; i < 5; i ++) {
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

           // Simulate responses
           simulateRound(tester);
       }
    }

    @Test
    public void gls_PhaseIsCorrect() {
        // simulate 16 ActionResponses (first round)
        for (int i = 0; i < 16; i++) {
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

  /*  @Test
    public void gls_GameFinishesCorrectly(){
        tester.setCurrentRound(4);
        tester.setCurrentPhase(PhaseType.EXECUTION);
        tester.setCurrentTurn(2);
        tester.getCommonDeck().getCards().add(new ActionCard() {
            @Override
            public ActionRequestDTO generateActionRequest(Game game, User user) {
                return null;
            }
        });
        gls.update(tester.getId());
        gameRepo.save(tester);
    }*/


    /*
    * HELPER FUNCTIONS
     */
    private void simulateRound(Game game){
        for(int i = 0; i < 16; i++){
            gls.update(game.getId());
        }
    }
}