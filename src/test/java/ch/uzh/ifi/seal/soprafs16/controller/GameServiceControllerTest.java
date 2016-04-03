package ch.uzh.ifi.seal.soprafs16.controller;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import ch.uzh.ifi.seal.soprafs16.Application;
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

    @Test
    public void testCreateGames() throws Exception {
        //region create User so that an owner can be assigned to game
        User user = new User();
        user.setName("Mike Meyers");
        user.setUsername("mm");

        HttpEntity<User> httpEntityUser = new HttpEntity<User>(user);

        ResponseEntity<User> responseUser = template.exchange(base + "/users/", HttpMethod.POST, httpEntityUser, User.class);

        ResponseEntity<User> userResponseEntity = template.getForEntity(base + "/users/" + responseUser.getBody().getId(), User.class);
        User userResponse = userResponseEntity.getBody();
        String token = userResponse.getToken();
        //endregion

        List<Game> gamesBefore = template.getForObject(base + "/games", List.class);

        //tests addGame
        Game game = new Game();
        game.setName("game1");

        String result = template.postForObject(base + "games?token=" + token, game, String.class);

        int expectedId = gamesBefore.size() + 1;
        Assert.assertEquals("/games/" + expectedId, result);

        //tests listGames
        List<Game> gamesAfter = template.getForObject(base + "/games", List.class);
        Assert.assertEquals(gamesBefore.size() + 1, gamesAfter.size());


        //tests getGame
        Game gameResponse = template.getForObject(base + "/games/" + expectedId, Game.class);
        Assert.assertEquals(game.getName(), gameResponse.getName());

        //tests addGame with wrong token (owner)
        Game game2 = new Game();
        game2.setName("game2");

        String result2 = template.postForObject(base + "games?token=" + "nA", game2, String.class);

        Assert.assertEquals("no owner found", result2);
    }

    @Test
    public void testCreatePlayers() throws Exception {
        //region create User so that an owner can be assigned to game
        User user1 = new User();
        user1.setName("name1");
        user1.setUsername("owner");

        HttpEntity<User> httpEntityUser1 = new HttpEntity<User>(user1);
        ResponseEntity<User> responseUser1 = template.exchange(base + "users/", HttpMethod.POST, httpEntityUser1, User.class);

        ResponseEntity<User> userResponseEntity1 = template.getForEntity(base + "users/" + responseUser1.getBody().getId(), User.class);
        User userResponse1 = userResponseEntity1.getBody();
        String token1 = userResponse1.getToken();
        //endregion
        //region create second User
        User user2 = new User();
        user2.setName("name2");
        user2.setUsername("user2");

        HttpEntity<User> httpEntityUser2 = new HttpEntity<User>(user2);

        ResponseEntity<User> responseUser2 = template.exchange(base + "users/", HttpMethod.POST, httpEntityUser2, User.class);

        ResponseEntity<User> userResponseEntity2 = template.getForEntity(base + "users/" + responseUser2.getBody().getId(), User.class);
        User userResponse2 = userResponseEntity2.getBody();
        String token2 = userResponse2.getToken();
        //endregion
        //region createGame
        Game game = new Game();
        game.setName("game3");

        template.postForObject(base + "games?token=" + token1, game, String.class);
        List<Game> games = template.getForObject(base + "games", List.class);

        int id = games.size();
        //endregion

        //tests addPlayer
        String resultAddPlayer = template.postForObject(base + "games/{gameId}/players?token=" + token2, game, String.class, id);
        Assert.assertEquals("/games/" + id + "/player/" + 1, resultAddPlayer);

        //tests addPlayer with wrong token
        String resultAddPlayerNull = template.postForObject(base + "games/{gameId}/players?token=" + "wrongToken", game, String.class, id);
        Assert.assertNull(resultAddPlayerNull);

        //tests listPlayers
        List<User> players = template.getForObject(base + "games/{gameId}/players", List.class, id);
        Assert.assertEquals(2, players.size());

        //tests listPlayers with wrong GameId
        List<User> playersNull = template.getForObject(base + "games/{gameId}/players", List.class, (long) 0.1);
        Assert.assertNull(playersNull);

        //tests getPlayer
        int playerId = players.size() - 1;
        User player = template.getForObject(base + "games/{gameId}/players/{playerId}", User.class, id, playerId);
        Assert.assertEquals(player.getToken(),token2);
    }

    @Test
    public void testWagons() throws Exception {
        //region create User so that an owner can be assigned to game
        User user1 = new User();
        user1.setName("name_WagonTest");
        user1.setUsername("owner_WagonTest");

        HttpEntity<User> httpEntityUser1 = new HttpEntity<User>(user1);
        ResponseEntity<User> responseUser1 = template.exchange(base + "users/", HttpMethod.POST, httpEntityUser1, User.class);

        ResponseEntity<User> userResponseEntity1 = template.getForEntity(base + "users/" + responseUser1.getBody().getId(), User.class);
        User userResponse1 = userResponseEntity1.getBody();
        String token1 = userResponse1.getToken();
        //endregion

        //region createGame
        Game game = new Game();
        game.setName("game_WagonTest");

        template.postForObject(base + "games?token=" + token1, game, String.class);
        List<Game> games = template.getForObject(base + "games", List.class);
        int id = games.size();
        //endregion

        //tests that game has 4 wagons
        List<Wagon> resultWagons = template.getForObject(base + "games/{gameId}/wagons",List.class,(long)id);
        Assert.assertEquals(4, resultWagons.size());

        //tests that game is null for wrong gameId
        List<Wagon> resultWagonsNull = template.getForObject(base + "games/{gameId}/wagons",List.class,(long)0.1);
        Assert.assertNull(resultWagonsNull);
    }

    @Test
    public void testWagonLevels() throws Exception {
        //region create User so that an owner can be assigned to game
        User user1 = new User();
        user1.setName("name_wagonLevelsTest");
        user1.setUsername("owner_wagonLevelsTest");

        HttpEntity<User> httpEntityUser1 = new HttpEntity<User>(user1);
        ResponseEntity<User> responseUser1 = template.exchange(base + "users/", HttpMethod.POST, httpEntityUser1, User.class);

        ResponseEntity<User> userResponseEntity1 = template.getForEntity(base + "users/" + responseUser1.getBody().getId(), User.class);
        User userResponse1 = userResponseEntity1.getBody();
        String token1 = userResponse1.getToken();
        //endregion

        //region createGame
        Game game = new Game();
        game.setName("game_wagonLevelsTest");

        template.postForObject(base + "games?token=" + token1, game, String.class);
        List<Game> games = template.getForObject(base + "games", List.class);
        int id = games.size();
        //endregion

        //tests that top and bottomLevel exist
        WagonLevel resultWagonLevelTop = template.getForObject(base + "games/{gameId}/wagons/{wagonId}/topLevel",WagonLevel.class,(long)id,2);
        WagonLevel resultWagonLevelBot = template.getForObject(base + "games/{gameId}/wagons/{wagonId}/bottomLevel",WagonLevel.class,(long)id,2);
        Assert.assertNotNull(resultWagonLevelTop);
        Assert.assertNotNull(resultWagonLevelBot);

        //tests that top and bottomLevel are null because of wrong gameId
        WagonLevel resultWagonLevelGameNullTop = template.getForObject(base + "games/{gameId}/wagons/{wagonId}/topLevel",WagonLevel.class,(long)0.1,2);
        WagonLevel resultWagonLevelGameNullBot = template.getForObject(base + "games/{gameId}/wagons/{wagonId}/bottomLevel",WagonLevel.class,(long)0.1,2);
        Assert.assertNull(resultWagonLevelGameNullTop);
        Assert.assertNull(resultWagonLevelGameNullBot);

        //tests that top and bottomLevel are null because of wrong wagonId
        WagonLevel resultWagonLevelWagonNullTop = template.getForObject(base + "games/{gameId}/wagons/{wagonId}/topLevel",WagonLevel.class,(long)id,20);
        WagonLevel resultWagonLevelWagonNullBot = template.getForObject(base + "games/{gameId}/wagons/{wagonId}/bottomLevel",WagonLevel.class,(long)id,20);
        Assert.assertNull(resultWagonLevelWagonNullTop);
        Assert.assertNull(resultWagonLevelWagonNullBot);
    }

}
