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
import ch.uzh.ifi.seal.soprafs16.constant.CharacterType;
import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.Wagon;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;


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

//    @Test
//    public void testCreateGames() throws Exception {
//        //region create User so that an owner can be assigned to game
//        User user = new User();
//        user.setName("Mike Meyers");
//        user.setUsername("mm");
//
//        HttpEntity<User> httpEntityUser = new HttpEntity<User>(user);
//
//        ResponseEntity<User> responseUser = template.exchange(base + "/users/", HttpMethod.POST, httpEntityUser, User.class);
//
//        ResponseEntity<User> userResponseEntity = template.getForEntity(base + "/users/" + responseUser.getBody().getId(), User.class);
//        User userResponse = userResponseEntity.getBody();
//        String token = userResponse.getToken();
//        //endregion
//
//        List<Game> gamesBefore = template.getForObject(base + "/games", List.class);
//
//        //tests addGame
//        Game game = new Game();
//        game.setName("game1");
//
//        String result = template.postForObject(base + "games?token=" + token, game, String.class);
//
//        int expectedId = gamesBefore.size() + 1;
//        Assert.assertEquals("/games/" + expectedId, result);
//
//        //tests listGames
//        List<Game> gamesAfter = template.getForObject(base + "/games", List.class);
//        Assert.assertEquals(gamesBefore.size() + 1, gamesAfter.size());
//
//
//        //tests getGame
//        Game gameResponse = template.getForObject(base + "/games/" + expectedId, Game.class);
//        Assert.assertEquals(game.getName(), gameResponse.getName());
//
//        //tests addGame with wrong token (owner)
//        Game game2 = new Game();
//        game2.setName("game2");
//
//        String result2 = template.postForObject(base + "games?token=" + "nA", game2, String.class);
//
//        Assert.assertEquals("no owner found", result2);
//    }

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
        //region User leaves Game - GameServiceController.removePlayer
        //region helper
        User user3 = new User();
        user3.setName("name3_lobbyTest");
        user3.setUsername("username3_lobbyTest");
        String token3 = template.postForObject(base + "users", user3, String.class);
        User user4 = new User();
        user4.setName("name4_lobbyTest");
        user4.setUsername("username4_lobbyTest");
        String token4 = template.postForObject(base + "users", user4, String.class);
        Game game3_4 = new Game();
        game3_4.setName("game3_4_lobbyTest");
        Long gameId3_4 = template.postForObject(base + "games?token=" + token4, game3_4, Long.class);
        //endregion
        //as a not-owner
        HttpEntity<User> httpEntityUser3 = new HttpEntity<User>(user3);
        ResponseEntity<Long> httpResponse3 = template.exchange(base + "games/" + gameId3_4 + "/users?token=" + token3, HttpMethod.DELETE, httpEntityUser3, Long.class);
        Game game3 = template.getForObject(base + "games/" + gameId3_4, Game.class);
        Assert.assertTrue(!containsUserName(game3.getUsers(), user3.getUsername()));
        Assert.assertTrue(containsUserName(game3.getUsers(), user4.getUsername()));
        //as an owner
        HttpEntity<User> httpEntityUser4 = new HttpEntity<User>(user4);
        ResponseEntity<Long> httpResponse4 = template.exchange(base + "games/" + gameId3_4 + "/users?token=" + token4, HttpMethod.DELETE, httpEntityUser4, Long.class);
        List<LinkedHashMap> games4 = template.getForObject(base + "games", List.class);
        Assert.assertTrue(!containsGameId(games4, gameId3_4));
        Assert.assertTrue(containsGameId(games4, gameId1));
        //endregion
        //region delete game - GameServiceController.deleteGame
        //region helper
        User user5 = new User();
        user5.setName("name5_lobbyTest");
        user5.setUsername("username5_lobbyTest");
        String token5 = template.postForObject(base + "users", user5, String.class);
        Game game5 = new Game();
        game5.setName("game5_lobbyTest");
        Long gameId5 = template.postForObject(base + "games?token=" + token5, game5, Long.class);
        //endregion
        HttpEntity<User> httpEntityUser5 = new HttpEntity<User>(user5);
        ResponseEntity<Long> httpResponse5 = template.exchange(base + "games/" + gameId5 + "?token=" + token5, HttpMethod.DELETE, httpEntityUser5, Long.class);
        Assert.assertEquals(gameId5, httpResponse5.getBody());
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
        CharacterType characterType7 = CharacterType.CHEYENNE;
        UriComponentsBuilder builder7 = UriComponentsBuilder.fromHttpUrl(base + "games/" + gameId1 + "/users")
                .queryParam("token", token7)
                .queryParam("character", characterType7.toString());
        HttpEntity<User> userResponse7 = template.exchange(builder7.build().encode().toUri(), HttpMethod.PUT, null, User.class);
        Assert.assertEquals(characterType7, userResponse7.getBody().getCharacterType());
        //endregion
        //region start game - GameServiceController.startGame
        //region helper
        User user8 = new User();
        user8.setName("name8_lobbyTest");
        user8.setUsername("username8_lobbyTest");
        String token8 = template.postForObject(base + "users", user8, String.class);
        User user9 = new User();
        user9.setName("name9_lobbyTest");
        user9.setUsername("username9_lobbyTest");
        String token9 = template.postForObject(base + "users", user9, String.class);

        Game game8_9 = new Game();
        game8_9.setName("game8_9_lobbyTest");
        Long gameId8_9 = template.postForObject(base + "games?token=" + token8, game8_9, Long.class);
        Long userIdGameJoined9 = template.postForObject(base + "games/" + gameId8_9 + "/users?token=" + token9, null, Long.class);
        //endregion
        template.postForObject(base + "games/" + gameId8_9 + "/start?token=" + token8, null, Void.class);
        Game game8_9Response = template.getForObject(base + "games/" + gameId8_9, Game.class);
        Assert.assertEquals(GameStatus.RUNNING, game8_9Response.getStatus());
        //endregion
    }

    private static boolean containsUserName(List<User> list, String username) {
        for (User user : list) {
            if (user.getUsername().equals(username)) {
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
//
//    @Test
//    public void testCreatePlayers() throws Exception {
//        //region create User so that an owner can be assigned to game
//        User user1 = new User();
//        user1.setName("name1");
//        user1.setUsername("owner");
//
//        HttpEntity<User> httpEntityUser1 = new HttpEntity<User>(user1);
//        ResponseEntity<User> responseUser1 = template.exchange(base + "users/", HttpMethod.POST, httpEntityUser1, User.class);
//
//        ResponseEntity<User> userResponseEntity1 = template.getForEntity(base + "users/" + responseUser1.getBody().getId(), User.class);
//        User userResponse1 = userResponseEntity1.getBody();
//        String token1 = userResponse1.getToken();
//        //endregion
//        //region create second User
//        User user2 = new User();
//        user2.setName("name2");
//        user2.setUsername("user2");
//
//        HttpEntity<User> httpEntityUser2 = new HttpEntity<User>(user2);
//
//        ResponseEntity<User> responseUser2 = template.exchange(base + "users/", HttpMethod.POST, httpEntityUser2, User.class);
//
//        ResponseEntity<User> userResponseEntity2 = template.getForEntity(base + "users/" + responseUser2.getBody().getId(), User.class);
//        User userResponse2 = userResponseEntity2.getBody();
//        String token2 = userResponse2.getToken();
//        //endregion
//        //region createGame
//        Game game = new Game();
//        game.setName("game3");
//
//        template.postForObject(base + "games?token=" + token1, game, String.class);
//        List<Game> games = template.getForObject(base + "games", List.class);
//
//        int id = games.size();
//        //endregion
//
//        //tests addPlayer
//        String resultAddPlayer = template.postForObject(base + "games/{gameId}/users?token=" + token2, game, String.class, id);
//        Assert.assertEquals("/games/" + id + "/user/" + 1, resultAddPlayer);
//
//        //tests addPlayer with wrong token
//        String resultAddPlayerNull = template.postForObject(base + "games/{gameId}/users?token=" + "wrongToken", game, String.class, id);
//        Assert.assertNull(resultAddPlayerNull);
//
////        //tests listPlayers
////        List<User> players = template.getForObject(base + "games/{gameId}/players", List.class, id);
////        Assert.assertEquals(2, players.size());
////
////        //tests listPlayers with wrong GameId
////        List<User> playersNull = template.getForObject(base + "games/{gameId}/players", List.class, (long) 0.1);
////        Assert.assertNull(playersNull);
////
////        //tests getPlayer
////        int playerId = players.size() - 1;
////        User player = template.getForObject(base + "games/{gameId}/players/{playerId}", User.class, id, playerId);
////        Assert.assertEquals(player.getToken(),token2);
//    }
//
//    @Test
//    public void testWagonLevels() throws Exception {
//        //region create User so that an owner can be assigned to game
//        User user1 = new User();
//        user1.setName("name1_testWagonLevels");
//        user1.setUsername("owner_testWagonLevels");
//
//        HttpEntity<User> httpEntityUser1 = new HttpEntity<User>(user1);
//        ResponseEntity<User> responseUser1 = template.exchange(base + "users/", HttpMethod.POST, httpEntityUser1, User.class);
//
//        ResponseEntity<User> userResponseEntity1 = template.getForEntity(base + "users/" + responseUser1.getBody().getId(), User.class);
//        User userResponse1 = userResponseEntity1.getBody();
//        String token1 = userResponse1.getToken();
//        //endregion
//        //region create second User
//        User user2 = new User();
//        user2.setName("name2_testWagonLevels");
//        user2.setUsername("user2_testWagonLevels");
//
//        HttpEntity<User> httpEntityUser2 = new HttpEntity<User>(user2);
//
//        ResponseEntity<User> responseUser2 = template.exchange(base + "users/", HttpMethod.POST, httpEntityUser2, User.class);
//
//        ResponseEntity<User> userResponseEntity2 = template.getForEntity(base + "users/" + responseUser2.getBody().getId(), User.class);
//        User userResponse2 = userResponseEntity2.getBody();
//        String token2 = userResponse2.getToken();
//        //endregion
//        //region createGame
//        Game game = new Game();
//        game.setName("game_testWagonLevels");
//
//        template.postForObject(base + "games?token=" + token1, game, String.class);
//        List<Game> games = template.getForObject(base + "games", List.class);
//
//        int gameId = games.size();
//        //endregion
//
//        //tests addUser to toplevel/bottomlevel
//        int wagonId1 = 3;
//        int wagonId2 = 1;
//        template.postForObject(base + "games/{gameId}/wagons/{wagonId}/topLevel/users?token=" + token1, null, String.class, gameId, wagonId1);
//        template.postForObject(base + "games/{gameId}/wagons/{wagonId}/bottomLevel/users?token=" + token2, null, String.class, gameId, wagonId2);
//
//        game = template.getForObject(base + "/games/" + gameId, Game.class);
//        Assert.assertTrue(containsUserName(game.getWagons().get(wagonId1 - 1).getTopLevel().getUsers(), user1.getUsername()));
//        Assert.assertTrue(containsUserName(game.getWagons().get(wagonId2 - 1).getBottomLevel().getUsers(), user2.getUsername()));
//
//        //tests modifyUser on topLevel (switch from TopLevel to BotLevel
////        int userIdTop1 = game.getWagons().get(wagonId1 - 1).getTopLevel().getUsers().size();
////        User user1Modified = game.getWagons().get(wagonId1 - 1).getTopLevel().getUsers().get(userIdTop1 - 1);
////        user1Modified.setWagonLevelIdNew(game.getWagons().get(wagonId1 - 1).getBottomLevel().getId());
////
////        template.put(base + "games/{gameId}/wagons/{wagonId}/topLevel/users/{userId}", user1Modified, gameId, wagonId1, userIdTop1);
////        game = template.getForObject(base + "/games/" + gameId, Game.class);
////        Assert.assertTrue(containsUserName(game.getWagons().get(wagonId1 - 1).getBottomLevel().getUsers(), user1.getUsername()));
//
//        //tests modifyUser on topLevel (move User 2 wagons forward)
//        int userIdTop1 = game.getWagons().get(wagonId1 - 1).getTopLevel().getUsers().size();
//        User user1Modified = game.getWagons().get(wagonId1 - 1).getTopLevel().getUsers().get(userIdTop1 - 1);
//        user1Modified.setWagonLevelIdNew(game.getWagons().get(wagonId1 - 1 - 2).getTopLevel().getId());
//
//        template.put(base + "games/{gameId}/wagons/{wagonId}/topLevel/users/{userId}", user1Modified, gameId, wagonId1, userIdTop1);
//        game = template.getForObject(base + "/games/" + gameId, Game.class);
//        Assert.assertTrue(containsUserName(game.getWagons().get(wagonId1 - 1 - 2).getTopLevel().getUsers(), user1.getUsername()));
//
////        int userIdBot = game.getWagons().get(wagonId2 - 1).getBottomLevel().getUsers().size();
////        Wagon   wagonMod2 = game.getWagons().get(wagonId2-1);
////        WagonLevel wagonLevelMod2 = game.getWagons().get(wagonId2-1).getTopLevel();
////        wagonLevelMod2.setWagon(wagonMod2);
////        User user2Modified = game.getWagons().get(wagonId2 - 1).getBottomLevel().getUsers().get(userIdBot - 1);
////        user2Modified.setWagonLevel(wagonLevelMod2);
////
////        template.put(base + "games/{gameId}/wagons/{wagonId}/bottomLevel/users/{userId}", user2Modified, gameId, wagonId2, userIdBot);
////        game = template.getForObject(base + "/games/" + gameId, Game.class);
////        Assert.assertTrue(containsUserName(game.getWagons().get(wagonId2 - 1).getTopLevel().getUsers(), user2.getUsername()));
//    }
//


//    @Test
//    public void testWagons() throws Exception {
//        //region create User so that an owner can be assigned to game
//        User user1 = new User();
//        user1.setName("name_WagonTest");
//        user1.setUsername("owner_WagonTest");
//
//        HttpEntity<User> httpEntityUser1 = new HttpEntity<User>(user1);
//        ResponseEntity<User> responseUser1 = template.exchange(base + "users/", HttpMethod.POST, httpEntityUser1, User.class);
//
//        ResponseEntity<User> userResponseEntity1 = template.getForEntity(base + "users/" + responseUser1.getBody().getId(), User.class);
//        User userResponse1 = userResponseEntity1.getBody();
//        String token1 = userResponse1.getToken();
//        //endregion
//
//        //region createGame
//        Game game = new Game();
//        game.setName("game_WagonTest");
//
//        template.postForObject(base + "games?token=" + token1, game, String.class);
//        List<Game> games = template.getForObject(base + "games", List.class);
//        int id = games.size();
//        //endregion
//
//        //tests that game has 4 wagons
//        List<Wagon> resultWagons = template.getForObject(base + "games/{gameId}/wagons",List.class,(long)id);
//        Assert.assertEquals(4, resultWagons.size());
//
//        //tests that game is null for wrong gameId
//        List<Wagon> resultWagonsNull = template.getForObject(base + "games/{gameId}/wagons",List.class,(long)0.1);
//        Assert.assertNull(resultWagonsNull);
//    }

//    @Test
//    public void testWagonLevels() throws Exception {
//        //region create User so that an owner can be assigned to game
//        User user1 = new User();
//        user1.setName("name_wagonLevelsTest");
//        user1.setUsername("owner_wagonLevelsTest");
//
//        HttpEntity<User> httpEntityUser1 = new HttpEntity<User>(user1);
//        ResponseEntity<User> responseUser1 = template.exchange(base + "users/", HttpMethod.POST, httpEntityUser1, User.class);
//
//        ResponseEntity<User> userResponseEntity1 = template.getForEntity(base + "users/" + responseUser1.getBody().getId(), User.class);
//        User userResponse1 = userResponseEntity1.getBody();
//        String token1 = userResponse1.getToken();
//        //endregion
//
//        //region createGame
//        Game game = new Game();
//        game.setName("game_wagonLevelsTest");
//
//        template.postForObject(base + "games?token=" + token1, game, String.class);
//        List<Game> games = template.getForObject(base + "games", List.class);
//        int id = games.size();
//        //endregion
//
//        //tests that top and bottomLevel exist
//        WagonLevel resultWagonLevelTop = template.getForObject(base + "games/{gameId}/wagons/{wagonId}/topLevel",WagonLevel.class,(long)id,2);
//        WagonLevel resultWagonLevelBot = template.getForObject(base + "games/{gameId}/wagons/{wagonId}/bottomLevel",WagonLevel.class,(long)id,2);
//        Assert.assertNotNull(resultWagonLevelTop);
//        Assert.assertNotNull(resultWagonLevelBot);
//
//        //tests that top and bottomLevel are null because of wrong gameId
//        WagonLevel resultWagonLevelGameNullTop = template.getForObject(base + "games/{gameId}/wagons/{wagonId}/topLevel",WagonLevel.class,(long)0.1,2);
//        WagonLevel resultWagonLevelGameNullBot = template.getForObject(base + "games/{gameId}/wagons/{wagonId}/bottomLevel",WagonLevel.class,(long)0.1,2);
//        Assert.assertNull(resultWagonLevelGameNullTop);
//        Assert.assertNull(resultWagonLevelGameNullBot);
//
//        //tests that top and bottomLevel are null because of wrong wagonId
//        WagonLevel resultWagonLevelWagonNullTop = template.getForObject(base + "games/{gameId}/wagons/{wagonId}/topLevel",WagonLevel.class,(long)id,20);
//        WagonLevel resultWagonLevelWagonNullBot = template.getForObject(base + "games/{gameId}/wagons/{wagonId}/bottomLevel",WagonLevel.class,(long)id,20);
//        Assert.assertNull(resultWagonLevelWagonNullTop);
//        Assert.assertNull(resultWagonLevelWagonNullBot);
//    }

}
