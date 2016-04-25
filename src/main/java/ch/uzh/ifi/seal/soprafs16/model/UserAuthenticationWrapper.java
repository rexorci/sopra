package ch.uzh.ifi.seal.soprafs16.model;

import java.io.Serializable;

/**
 * Created by rafael on 24/04/16.
 */
public class UserAuthenticationWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userToken;
    private Long userId;

    public UserAuthenticationWrapper(String userToken, Long userId){
        this.userId = userId;
        this.userToken = userToken;
    }
}
