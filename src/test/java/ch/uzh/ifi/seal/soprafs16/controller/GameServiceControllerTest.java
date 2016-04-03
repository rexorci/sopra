package ch.uzh.ifi.seal.soprafs16.controller;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Assert.assertSame(1L, responseUser.getBody().getId());

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

        HttpEntity<Game> httpEntity = new HttpEntity<Game>(game);
        String result = template.postForObject(base + "games?token=" + token, game, String.class);

        Assert.assertEquals("/games/" + 1, result);

        //tests listGames
        List<Game> gamesAfter = template.getForObject(base + "/games", List.class);
        Assert.assertEquals(1, gamesAfter.size());

        //tests getGame
        Game gameResponse = template.getForObject(base + "/games/" + 1, Game.class);
        Assert.assertEquals(game.getName(), gameResponse.getName());
    }
}