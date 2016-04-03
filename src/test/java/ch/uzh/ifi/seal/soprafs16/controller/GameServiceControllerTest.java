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

        //tests listGames
        List<Game> gamesBefore = template.getForObject(base + "/games", List.class);
        Assert.assertEquals(0, gamesBefore.size());

        //tests addGame
        Game game = new Game();
        game.setName("game1");

        String result = template.postForObject(base + "games?token=" + token, game, String.class);

        Assert.assertEquals("/games/" + 1, result);

        //tests listGames
        List<Game> gamesAfter = template.getForObject(base + "/games", List.class);
        Assert.assertEquals(1, gamesAfter.size());


        //tests getGame
        Game gameResponse = template.getForObject(base + "/games/" + 1, Game.class);
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
        ResponseEntity<User> responseUser1 = template.exchange(base + "/users/", HttpMethod.POST, httpEntityUser1, User.class);

        ResponseEntity<User> userResponseEntity1 = template.getForEntity(base + "/users/" + responseUser1.getBody().getId(), User.class);
        User userResponse1 = userResponseEntity1.getBody();
        String token1 = userResponse1.getToken();
        //endregion
        //region create second User
        User user2 = new User();
        user2.setName("name2");
        user2.setUsername("user2");

        HttpEntity<User> httpEntityUser2 = new HttpEntity<User>(user2);

        ResponseEntity<User> responseUser2 = template.exchange(base + "/users/", HttpMethod.POST, httpEntityUser2, User.class);

        ResponseEntity<User> userResponseEntity2 = template.getForEntity(base + "/users/" + responseUser2.getBody().getId(), User.class);
        User userResponse2 = userResponseEntity2.getBody();
        String token2 = userResponse2.getToken();
        //endregion
        //region createGame
        Game game = new Game();
        game.setName("game3");

        template.postForObject(base + "games?token=" + token1, game, String.class);
        List<Game> games = template.getForObject(base + "/games", List.class);

        int id = games.size();
        //endregion

        //tests addPlayer
        String resultAddPlayer = template.postForObject(base + "/games/{gameId}/players?token=" + token2, game, String.class, id);
        Assert.assertEquals("/games/" + id + "/player/" + 1, resultAddPlayer);

        //tests addPlayer with wrong token
        String resultAddPlayerNull = template.postForObject(base + "/games/{gameId}/players?token=" + "wrongToken", game, String.class, id);
        Assert.assertNull(resultAddPlayerNull);

        //tests listPlayers
        List<User> players = template.getForObject(base + "/games/{gameId}/players", List.class, id);
        Assert.assertEquals(2, players.size());

        //tests listPlayers with wrong GameId
        List<User> playersNull = template.getForObject(base + "/games/{gameId}/players", List.class, (long)0.1);
        Assert.assertNull(playersNull);



        /*
            public List<User> listPlayers(@PathVariable Long gameId) {
        logger.debug("listPlayers");

        Game game = gameRepo.findOne(gameId);
        if (game != null) {
            return game.getUsers();
        }

        return null;
    }
         */
    }
}