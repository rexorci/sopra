package ch.uzh.ifi.seal.soprafs16.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import ch.uzh.ifi.seal.soprafs16.Application;
import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
public class GameInitServiceTest {

    @Autowired
    GameInitService gameInitService;

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
    //endregion

    @Test
    public void initGameTest() {
        Game game = new Game();
        game.setName("game1");
        User owner = new User();
        owner.setName("owner1");
        owner.setId((long) 7);
        game = gameInitService.startGame(game, owner, gameRepo, wagonRepo, wagonLevelRepo, itemRepo, marshalRepo);
        Assert.assertEquals((long)owner.getId(), (long)game.getCurrentPlayer());
        Assert.assertEquals(GameStatus.PENDING, game.getStatus());
        Assert.assertTrue(game.getWagons().size() == 3 || game.getWagons().size() == 4);
        Assert.assertEquals((long)1, (long)(game.getMarshal().getWagonLevel().getWagon().getId()));

    }
}
