package ch.uzh.ifi.seal.soprafs16.controller;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import ch.uzh.ifi.seal.soprafs16.Application;
import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.constant.PhaseType;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.StationCard;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})


/**
 * Created by Christoph on 02.04.2016.
 */
public class GameServiceControllerTest {
    @Value("${local.server.port}")
    private int port;

    private URL base;
    private RestTemplate template;

    @Before
    public void setUp()
            throws Exception {
        this.base = new URL("http://localhost:" + port + "/");
        this.template = new TestRestTemplate();
    }

    @Test
    public void testLobbyLogic() throws Exception {
        //region create User - UserServiceController.login()
        User user1 = new User();
        user1.setName("name1_lobbyTest");
        user1.setUsername("username1_lobbyTest");
        String token1 = template.postForObject(base + "users", user1, String.class);
        Assert.assertNotNull(token1);
        //endregion
        //region create Game - GameServiceController.addGame()
        Game game1 = new Game();
        game1.setName("game1_lobbyTest");
        Long gameId1 = template.postForObject(base + "games?token=" + token1, game1, Long.class);
        Assert.assertNotNull(gameId1);
        //endregion
        //region all pending Games - GameServiceController.listGames() with filter
        String filter = "status=PENDING";
        template.getForObject(base + "games?" + filter, List.class);
        //endregion
        //region User joins Game  - GameServiceController.addPlayer
        User user2 = new User();
        user2.setName("name2_lobbyTest");
        user2.setUsername("username2_lobbyTest");
        String token2 = template.postForObject(base + "users", user2, String.class);
        Long userIdGameJoined = template.postForObject(base + "games/" + gameId1 + "/users?token=" + token2, null, Long.class);
        Assert.assertNotNull(userIdGameJoined);
        //endregion
        //region get UserId of User - UserServiceController.getUserId
        User user6 = new User();
        user6.setName("name6_lobbyTest");
        user6.setUsername("username6_lobbyTest");
        String token6 = template.postForObject(base + "users", user6, String.class);
        Long userId = template.getForObject(base + "users?token=" + token6, Long.class);
        Assert.assertNotNull(userId);
        //endregion
        //region modify character of User - GameServiceController.modifyUserCharacter
        //region helper
        User user7 = new User();
        user7.setName("name7_lobbyTest");
        user7.setUsername("username7_lobbyTest");
        String token7 = template.postForObject(base + "users", user7, String.class);
        Game game7 = new Game();
        game7.setName("game7_lobbyTest");
        Long gameId7 = template.postForObject(base + "games?token=" + token7, game7, Long.class);
        //endregion
        String characterType7 = "Cheyenne";
        UriComponentsBuilder builder7 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1 + "/users")
                .queryParam("token", token7)
                .queryParam("character", characterType7.toString());
        HttpEntity<User> userResponse7 = template.exchange(builder7.build().encode().toUri(), HttpMethod.PUT, null, User.class);
        Assert.assertEquals(characterType7, userResponse7.getBody().getCharacterType());
        //endregion

    }

    @Test
    public void testStartGame() {
        //region helper
        User user1 = new User();
        user1.setName("name1_startGameTest");
        user1.setUsername("username1_startGameTest");
        String token1 = template.postForObject(base + "users", user1, String.class);
        User user2 = new User();
        user2.setName("name2_startGameTest");
        user2.setUsername("username2_startGameTest");
        String token2 = template.postForObject(base + "users", user2, String.class);

        Game game1_2 = new Game();
        game1_2.setName("game1_2_startGameTest");
        Long gameId1_2 = template.postForObject(base + "games?token=" + token1, game1_2, Long.class);
        Long userIdGameJoined9 = template.postForObject(base + "games/" + gameId1_2 + "/users?token=" + token2, null, Long.class);

        String characterType1 = "Cheyenne";
        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", token1)
                .queryParam("character", characterType1.toString());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, null, User.class);
        String characterType2 = "Doc";
        UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", token2)
                .queryParam("character", characterType2.toString());
        HttpEntity<User> userResponse2 = template.exchange(builder2.build().encode().toUri(), HttpMethod.PUT, null, User.class);
        //endregion
        template.postForObject(base + "games/" + gameId1_2 + "/start?token=" + token1, null, Void.class);
        Game game1_2Response = template.getForObject(base + "games/" + gameId1_2, Game.class);
        //region Assertions
        //is the the current player (startplayer) placed in the last wagon
        WagonLevel lastWagonLevel = game1_2Response.getWagons().get((game1_2Response.getWagons().size() - 1)).getBottomLevel();
        Assert.assertTrue(containsUserId(lastWagonLevel.getUsers(), game1_2Response.getUsers().get(game1_2Response.getCurrentPlayer()).getId()));
        for (User u : game1_2Response.getUsers()) {
            //does every Player have a 250$ bag
            Assert.assertEquals(250, u.getItems().get(0).getValue());
            //does the bulletsdeck contain 6 bullets
            Assert.assertEquals(6, u.getBulletsDeck().getCards().size());
            //does the handdeck contain 6 cards (or 7 if Doc)
            int drawCardsAmount = u.getCharacterType().equals("Doc") ? 7 : 6;
            Assert.assertEquals(drawCardsAmount, u.getHandDeck().getCards().size());
            //does the hiddendeck contains 4 cards (3 for doc)
            int hiddenCardsAmount = u.getCharacterType().equals("Doc") ? 3 : 4;
            Assert.assertEquals(hiddenCardsAmount, u.getHiddenDeck().getCards().size());
        }
        //does the roundcarddeck contain 5 cards
        Assert.assertEquals(5, game1_2Response.getRoundCardDeck().getCards().size());
        //is the last roundcard a stationcard? TODO

        //does the neutralbulletdeck contain 13 cards (marshalbullets)
        Assert.assertEquals(13, game1_2Response.getNeutralBulletsDeck().getCards().size());
        //does the commondeck exist?
        Assert.assertNotNull(game1_2Response.getCommonDeck());
        //assert gamevariables
        Assert.assertEquals((Integer) 0, game1_2Response.getCurrentRound());
        Assert.assertEquals((Integer) 0, game1_2Response.getCurrentTurn());
        Assert.assertNotNull(game1_2Response.getRoundPattern());
        Assert.assertEquals(PhaseType.PLANNING, game1_2Response.getCurrentPhase());
        //endregion
    }

    @Test
    public void testDeleteFunctions() {
        //region set up
        User user1 = new User();
        user1.setName("name1_deleteTest");
        user1.setUsername("username1_deleteTest");
        String token1 = template.postForObject(base + "users", user1, String.class);
        User user2 = new User();
        user2.setName("name2_deleteTest");
        user2.setUsername("username2_deleteTest");
        String token2 = template.postForObject(base + "users", user2, String.class);
        User user3 = new User();
        user3.setName("name3_deleteTest");
        user3.setUsername("username3_deleteTest");
        String token3 = template.postForObject(base + "users", user3, String.class);

        Game game = new Game();
        game.setName("gameDeleteTest");
        Long gameId = template.postForObject(base + "games?token=" + token1, game, Long.class);
        Long userIdGameJoined9 = template.postForObject(base + "games/" + gameId + "/users?token=" + token2, null, Long.class);

        String characterType1 = "Cheyenne";
        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId + "/users")
                .queryParam("token", token1)
                .queryParam("character", characterType1.toString());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, null, User.class);
        String characterType2 = "Doc";
        UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId + "/users")
                .queryParam("token", token2)
                .queryParam("character", characterType2.toString());
        HttpEntity<User> userResponse2 = template.exchange(builder2.build().encode().toUri(), HttpMethod.PUT, null, User.class);
        String characterType3 = "Tuco";
        UriComponentsBuilder builder3 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId + "/users")
                .queryParam("token", token3)
                .queryParam("character", characterType3.toString());
        HttpEntity<User> userResponse3 = template.exchange(builder3.build().encode().toUri(), HttpMethod.PUT, null, User.class);

        template.postForObject(base + "games/" + gameId + "/start?token=" + token1, null, Void.class);
        //endregion
        //region remove a user that is not the owner
        HttpEntity<User> httpEntityUser3 = new HttpEntity<User>(user3);
        ResponseEntity<Long> httpResponse3 = template.exchange(base + "games/" + gameId + "/users?token=" + token3, HttpMethod.DELETE, httpEntityUser3, Long.class);
        Game gameRemoveNotOwner = template.getForObject(base + "games/" + gameId, Game.class);
        Assert.assertTrue(!containsUserName(gameRemoveNotOwner.getUsers(), user3.getUsername()));
        Assert.assertTrue(containsUserName(gameRemoveNotOwner.getUsers(), user1.getUsername()));
        Assert.assertTrue(containsUserName(gameRemoveNotOwner.getUsers(), user2.getUsername()));
        //endregion
        //region remove a user that is the owner. check if owner is correctly "handed over"
        HttpEntity<User> httpEntityUser1 = new HttpEntity<User>(user1);
        ResponseEntity<Long> httpResponse1 = template.exchange(base + "games/" + gameId + "/users?token=" + token1, HttpMethod.DELETE, httpEntityUser1, Long.class);
        Game gameRemoveOwner = template.getForObject(base + "games/" + gameId, Game.class);
        Assert.assertTrue(gameRemoveOwner.getOwner().equals(user2.getName()));
        //endregion
        //region remove the last user and trigger game deletion
        List<LinkedHashMap> allGamesBefore = template.getForObject(base + "games", List.class);
        Assert.assertTrue(containsGameId(allGamesBefore, gameId));
        HttpEntity<User> httpEntityUser2 = new HttpEntity<User>(user2);
        ResponseEntity<Long> httpResponse2 = template.exchange(base + "games/" + gameId + "/users?token=" + token2, HttpMethod.DELETE, httpEntityUser2, Long.class);
        List<LinkedHashMap> allGamesAfter = template.getForObject(base + "games", List.class);
        Assert.assertTrue(!containsGameId(allGamesAfter, gameId));

        // Assert.assertEquals(gameId, httpResponse2.getBody());
        //endregion
    }

    @Test
    public void testStartDemoGame() {
        //region helper
        User user1 = new User();
        user1.setName("name1_startDemoGameTest");
        user1.setUsername("username1_startDemoGameTest");
        String token1 = template.postForObject(base + "users", user1, String.class);
        User user2 = new User();
        user2.setName("name2_startDemoGameTest");
        user2.setUsername("username2_startDemoGameTest");
        String token2 = template.postForObject(base + "users", user2, String.class);

        Game game1_2 = new Game();
        game1_2.setName("game1_2_startDemoGameTest");
        Long gameId1_2 = template.postForObject(base + "games?token=" + token1, game1_2, Long.class);
        Long userIdGameJoined9 = template.postForObject(base + "games/" + gameId1_2 + "/users?token=" + token2, null, Long.class);

        String characterType1 = "Cheyenne";
        UriComponentsBuilder builder1 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", token1)
                .queryParam("character", characterType1.toString());
        HttpEntity<User> userResponse1 = template.exchange(builder1.build().encode().toUri(), HttpMethod.PUT, null, User.class);
        String characterType2 = "Doc";
        UriComponentsBuilder builder2 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1_2 + "/users")
                .queryParam("token", token2)
                .queryParam("character", characterType2.toString());
        HttpEntity<User> userResponse2 = template.exchange(builder2.build().encode().toUri(), HttpMethod.PUT, null, User.class);
        //endregion
        template.postForObject(base + "games/" + gameId1_2 + "/startDemo?token=" + token1, null, Void.class);
        Game game1_2Response = template.getForObject(base + "games/" + gameId1_2, Game.class);
        //region Assertions
        Assert.assertEquals(StationCard.class, game1_2Response.getRoundCardDeck().getCards().get(game1_2Response.getCurrentRound()).getClass().getSuperclass());

       Assert.assertNotNull(game1_2Response.getWagons().get(1).getBottomLevel().getMarshal());
        Assert.assertEquals(user1.getUsername(), game1_2Response.getWagons().get(0).getBottomLevel().getUsers().get(0).getUsername());
        Assert.assertEquals(user2.getUsername(), game1_2Response.getWagons().get(3).getTopLevel().getUsers().get(0).getUsername());
        //endregion
    }

    //region helper methods
    private static boolean containsUserName(List<User> list, String username) {
        for (User user : list) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsUserId(List<User> list, Long userId) {
        for (User user : list) {
            if (user.getId() == userId) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsGameId(List<LinkedHashMap> list, Long gameId) {
        for (LinkedHashMap game : list) {
            String idValue = game.get("id").toString();
            if (idValue.equals(gameId.toString())) {
                return true;
            }
        }
        return false;
    }
    //endregion
}
