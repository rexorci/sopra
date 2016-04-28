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

import java.net.URL;

import ch.uzh.ifi.seal.soprafs16.Application;
import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.Marshal;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.UserAuthenticationWrapper;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.CollectItemResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.DrawCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.MoveMarshalResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.MoveResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.PlayCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.PunchResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.ShootResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.characters.Character;
import ch.uzh.ifi.seal.soprafs16.model.characters.Ghost;
import ch.uzh.ifi.seal.soprafs16.model.characters.Tuco;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Nico on 20.04.2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
@Transactional
public class ActionResponseServiceTest {
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
    private ActionResponseService ars;

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
        user1.setName("name1_startGameTest" + i);
        user1.setUsername("username1_startGameTest" + i);
        UserAuthenticationWrapper userAuthenticationWrapper1 = template.postForObject(base + "users", user1, UserAuthenticationWrapper.class);
        User user2 = new User();
        user2.setName("name2_startGameTest" + i);
        user2.setUsername("username9_startGameTest" + i);
        UserAuthenticationWrapper userAuthenticationWrapper2 = template.postForObject(base + "users", user2, UserAuthenticationWrapper.class);

        tester = new Game();
        tester.setName("game1_2_startGameTest" + i);
        Long gameId1_2 = template.postForObject(base + "games?token=" + userAuthenticationWrapper1.getUserToken(), tester, Long.class);
        Long userIdGameJoined9 = template.postForObject(base + "games/" + gameId1_2 + "/users?token=" + userAuthenticationWrapper2.getUserToken(), null, Long.class);

        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper1.getUserToken());
        HttpEntity<Character> characterRequest1 = new HttpEntity<>(new Ghost());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, characterRequest1, User.class);

        UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", userAuthenticationWrapper2.getUserToken());
        HttpEntity<Character> characterRequest2 = new HttpEntity<>(new Tuco());
        HttpEntity<User> userResponse2 = template.exchange(builder2.build().encode().toUri(), HttpMethod.PUT, characterRequest2, User.class);


        template.postForObject(base + "games/" + gameId1_2 + "/start?token=" + userAuthenticationWrapper1.getUserToken(), null, Void.class);
        Game testerResponse = template.getForObject(base + "games/" + gameId1_2, Game.class);
        gameId = testerResponse.getId();
    }

    @Test
    public void processResponse_DrawCardIsCorrect(){
        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findOne(game.getUsers().get(0).getId());

        PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHiddenDeck().getId());
        PlayerDeck<HandCard> handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHandDeck().getId());
        // Assert correct playerDeck sizes
        assertEquals(4, hiddenDeck.size());
        assertEquals(6, handDeck.size());

        DrawCardResponseDTO dcr = new DrawCardResponseDTO();
        dcr.setUserID(user.getId());
        dcr.setSpielId(gameId);
        ars.processResponse(dcr);

        hiddenDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHiddenDeck().getId());
        handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHandDeck().getId());

        assertEquals(1, hiddenDeck.size());
        assertEquals(9, handDeck.size());
    }


    @Test
    public void processResponse_DrawCardIsCorrectWhenEmpty(){
        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findOne(game.getUsers().get(0).getId());

        PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHiddenDeck().getId());
        PlayerDeck<HandCard> handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHandDeck().getId());
        // Assert correct playerDeck sizes
        assertEquals(4, hiddenDeck.size());
        assertEquals(6, handDeck.size());

        DrawCardResponseDTO dcr = new DrawCardResponseDTO();
        dcr.setUserID(user.getId());
        dcr.setSpielId(gameId);
        ars.processResponse(dcr);
        ars.processResponse(dcr);

        hiddenDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHiddenDeck().getId());
        handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHandDeck().getId());

        assertEquals(0, hiddenDeck.size());
        assertEquals(10, handDeck.size());
    }

    @Test
    public void processResponse_playCardIsCorrect(){
        Game game = gameRepo.findOne(gameId);

        Hibernate.initialize(game.getUsers());
        User user = userRepo.findOne(game.getUsers().get(0).getId());

        PlayerDeck<HandCard> handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHandDeck().getId());
        ActionCard ac = (ActionCard)cardRepo.findOne(handDeck.get(0).getId());

        PlayCardResponseDTO pcr = new PlayCardResponseDTO();
        pcr.setUserID(user.getId());
        pcr.setSpielId(game.getId());
        pcr.setPlayedCard(ac);

        ars.processResponse(pcr);

        handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHandDeck().getId());
        GameDeck<ActionCard> commonDeck = (GameDeck<ActionCard>)deckRepo.findOne(game.getCommonDeck().getId());

        assertFalse(handDeck.getCards().contains(ac));
        assertEquals(ac.getId(), ((ActionCard)commonDeck.getCards().get(commonDeck.size()-1)).getId());
    }

    @Test
    public void processResponse_moveIsCorrect(){
        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findOne(game.getUsers().get(0).getId());

        WagonLevel wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());

        MoveResponseDTO mr = new MoveResponseDTO();
        mr.setUserID(user.getId());
        mr.setSpielId(game.getId());
        mr.setWagonLevelID(wl.getWagonLevelBefore().getId());

        ars.processResponse(mr);

        wl = wagonLevelRepo.findOne(wl.getId());
        WagonLevel newWl = wagonLevelRepo.findOne(wl.getWagonLevelBefore().getId());
        assertTrue(newWl.removeUserById(user.getId()));
        assertFalse(wl.removeUserById(user.getId()));
    }

    @Test
    public void processResponse_collectItemIsCorrect(){
        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findOne(game.getUsers().get(0).getId());

        WagonLevel wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());

        CollectItemResponseDTO cir = new CollectItemResponseDTO();
        cir.setUserID(user.getId());
        cir.setSpielId(game.getId());
        cir.setCollectedItemType(wl.getItems().get(0).getItemType());

        int userItemCount = user.getItems().size();
        int wlItemCount = wl.getItems().size();

        ars.processResponse(cir);

        wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());
        user = userRepo.findOne(user.getId());

        assertEquals(userItemCount + 1, user.getItems().size());
        assertEquals(wlItemCount - 1, wl.getItems().size());
    }

    @Test
    public void processResponse_PunchIsCorrect(){
        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findOne(game.getUsers().get(0).getId());

        User victim = userRepo.findOne(game.getUsers().get(1).getId());
        WagonLevel wl = wagonLevelRepo.findOne(victim.getWagonLevel().getId());
        WagonLevel newWl = wagonLevelRepo.findOne(victim.getWagonLevel().getWagonLevelBefore().getId());

        Hibernate.initialize(victim.getItems());
        Hibernate.initialize(wl.getItems());
        int vicItemCount = victim.getItems().size();
        int wlItemCount = wl.getItems().size();

        PunchResponseDTO pr = new PunchResponseDTO();
        pr.setVictimID(victim.getId());
        pr.setItemType(ItemType.BAG);
        pr.setWagonLevelID(newWl.getId());
        pr.setSpielId(gameId);
        pr.setUserID(user.getId());

        ars.processResponse(pr);

        wl = wagonLevelRepo.findOne(wl.getId());
        newWl = wagonLevelRepo.findOne(newWl.getId());
        victim = userRepo.findOne(game.getUsers().get(1).getId());

        assertEquals(vicItemCount - 1, victim.getItems().size());
        assertEquals(wlItemCount + 1, wl.getItems().size());
        assertEquals(newWl.getId(), victim.getWagonLevel().getId());
        assertTrue(newWl.removeUserById(victim.getId()));
    }

    @Test
    public void processResponse_ShootIsCorrect(){
        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findOne(game.getUsers().get(0).getId());

        User victim = userRepo.findOne(game.getUsers().get(1).getId());

        Hibernate.initialize(user.getBulletsDeck());
        Hibernate.initialize(victim.getHiddenDeck());
        int bulletCounter = user.getBulletsDeck().size();
        int victimHiddenDeckSize = victim.getHiddenDeck().size();
        BulletCard bc = (BulletCard)user.getBulletsDeck().getCards().get(user.getBulletsDeck().size() - 1);

        ShootResponseDTO sr = new ShootResponseDTO();
        sr.setSpielId(game.getId());
        sr.setUserID(user.getId());
        sr.setVictimID(victim.getId());

        ars.processResponse(sr);

        user = userRepo.findOne(game.getUsers().get(0).getId());
        victim = userRepo.findOne(game.getUsers().get(1).getId());
        bc = (BulletCard)cardRepo.findOne(bc.getId());

        assertEquals(bulletCounter - 1, user.getBulletsDeck().size());
        assertEquals(victimHiddenDeckSize + 1, victim.getHiddenDeck().getCards().size());
        assertEquals(victim.getHiddenDeck().getId(), bc.getDeck().getId());
    }

    @Test
    public void processResponse_MoveMarshalIsCorrect(){
        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findOne(game.getUsers().get(0).getId());

        Marshal marshal = marshalRepo.findOne(game.getMarshal().getId());
        WagonLevel wl = wagonLevelRepo.findOne(marshal.getWagonLevel().getId());
        WagonLevel newWl = wagonLevelRepo.findOne(marshal.getWagonLevel().getWagonLevelAfter().getId());

        MoveMarshalResponseDTO mmr = new MoveMarshalResponseDTO();
        mmr.setSpielId(game.getId());
        mmr.setUserID(user.getId());
        mmr.setWagonLevelID(newWl.getId());

        ars.processResponse(mmr);

        marshal = marshalRepo.findOne(game.getMarshal().getId());
        wl = wagonLevelRepo.findOne(wl.getId());
        newWl = wagonLevelRepo.findOne(newWl.getId());

        assertNull(wl.getMarshal());
        assertEquals(marshal.getId(), newWl.getMarshal().getId());
        assertEquals(newWl.getId(), marshal.getWagonLevel().getId());
    }

    @Test
    public void changeLevel_isCorrect(){
        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findOne(game.getUsers().get(0).getId());

        WagonLevel wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());
        WagonLevel newWl = wagonLevelRepo.findOne(wl.getWagon().getTopLevel().getId());

        ars.changeLevel(user);

        wl = wagonLevelRepo.findOne(wl.getId());
        newWl = wagonLevelRepo.findOne(newWl.getId());
        user = userRepo.findOne(user.getId());

        assertEquals(newWl.getId(), user.getWagonLevel().getId());
        assertFalse(wl.removeUserById(user.getId()));
        assertTrue(newWl.removeUserById(user.getId()));
    }

/*    @Test
    public void processResponse_Punch_CheyenneCollectsItem(){
        Game game = gameRepo.findOne(gameId);
        User user = userRepo.findOne(game.getUsers().get(0).getId());

        User victim = userRepo.findOne(game.getUsers().get(1).getId());
        WagonLevel wl = wagonLevelRepo.findOne(victim.getWagonLevel().getId());
        WagonLevel newWl = wagonLevelRepo.findOne(victim.getWagonLevel().getWagonLevelBefore().getId());

        Hibernate.initialize(victim.getItems());
        Hibernate.initialize(wl.getItems());
        int vicItemCount = victim.getItems().size();
        int wlItemCount = wl.getItems().size();

        PunchResponseDTO pr = new PunchResponseDTO();
        pr.setVictimID(victim.getId());
        pr.setItemType(ItemType.BAG);
        pr.setWagonLevelID(newWl.getId());
        pr.setSpielId(gameId);
        pr.setUserID(user.getId());

        ars.processResponse(pr);

        wl = wagonLevelRepo.findOne(wl.getId());
        newWl = wagonLevelRepo.findOne(newWl.getId());
        victim = userRepo.findOne(game.getUsers().get(1).getId());

        assertEquals(vicItemCount - 1, victim.getItems().size());
        assertEquals(wlItemCount + 1, wl.getItems().size());
        assertEquals(newWl.getId(), victim.getWagonLevel().getId());
        assertTrue(newWl.removeUserById(victim.getId()));
    }*/
}
