//package ch.uzh.ifi.seal.soprafs16.controller;
//
//
//import org.hibernate.Hibernate;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.IntegrationTest;
//import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.boot.test.TestRestTemplate;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;
//
//import java.net.URL;
//
//import ch.uzh.ifi.seal.soprafs16.Application;
//import ch.uzh.ifi.seal.soprafs16.model.Game;
//import ch.uzh.ifi.seal.soprafs16.model.User;
//import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.CollectCard;
//import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
//import ch.uzh.ifi.seal.soprafs16.model.repositories.CharacterRepository;
//import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
//import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
//import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
//import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
//import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
//import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
//import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonRepository;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = Application.class)
//@WebAppConfiguration
//@IntegrationTest({"server.port=0"})
//@Transactional
///**
// * Created by Timon Willi on 22.04.2016.
// */
//public class ActionServiceControllerTest {
//
//    @Autowired
//    private UserRepository userRepo;
//    @Autowired
//    private GameRepository gameRepo;
//    @Autowired
//    private WagonRepository wagonRepo;
//    @Autowired
//    private WagonLevelRepository wagonLevelRepository;
//    @Autowired
//    private ItemRepository itemRepo;
//    @Autowired
//    private MarshalRepository marshalRepo;
//    @Autowired
//    private CharacterRepository characterRepo;
//    @Autowired
//    private CardRepository cardRepo;
//    @Autowired
//    private DeckRepository deckRepo;
//
//        @Value("${local.server.port}")
//        private int port;
//
//        private URL base;
//        private RestTemplate template;
//
//        @Before
//        public void setUp()
//                throws Exception {
//            this.base = new URL("http://localhost:" + port + "/");
//            this.template = new TestRestTemplate();
//        }
//
//        @Test
//        public void processRequest_CollectItemIsCorrect() {
//
//            User user1 = new User();
//            String token1 = template.postForObject(base + "users", user1, String.class);
//            Assert.assertNotNull(token1);
//            user1.setWagonLevel();
//
//            Game game1 = new Game();
//            Long gameId1 = template.postForObject(base+"games?token="+token1, game1, Long.class);
//            Assert.assertNotNull(gameId1);
//            CollectCard clc = new CollectCard();
//            clc.generateActionRequest(game1, user1);
//            assertEquals(1, game1.getActions().size());
//
//
//
//
//    }
//
//
//}

