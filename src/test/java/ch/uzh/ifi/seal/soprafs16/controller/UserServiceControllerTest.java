package ch.uzh.ifi.seal.soprafs16.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

import ch.uzh.ifi.seal.soprafs16.Application;

//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({ "server.port=0" })
public class UserServiceControllerTest {

    @Value("${local.server.port}")
    private int          port;

    private URL          base;
    private RestTemplate template;

    @Before
    public void setUp()
            throws Exception {
        this.base = new URL("http://localhost:" + port + "/");
        this.template = new TestRestTemplate();
    }

    @Test
    public void testCreateUserSuccess() {
//        List<User> usersBefore = template.getForObject(base + "/users", List.class);
//        Assert.assertEquals(0, usersBefore.size());
//
//        User request = new User();
//        request.setName("Mike Meyers");
//        request.setUsername("mm");
//
//        HttpEntity<User> httpEntity = new HttpEntity<User>(request);
//
//        ResponseEntity<User> response = template.exchange(base + "/users/", HttpMethod.POST, httpEntity, User.class);
//        Assert.assertSame(1L, response.getBody().getId());
//
//        List<User> usersAfter = template.getForObject(base + "/users", List.class);
//        Assert.assertEquals(1, usersAfter.size());
//
//        ResponseEntity<User> userResponseEntity = template.getForEntity(base + "/users/" + response.getBody().getId(), User.class);
//        User userResponse = userResponseEntity.getBody();
//        Assert.assertEquals(request.getName(), userResponse.getName());
//        Assert.assertEquals(request.getUsername(), userResponse.getUsername());
    }

}
