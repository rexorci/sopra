package ch.uzh.ifi.seal.soprafs16.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

/**
 * Created by rafael on 24/04/16.
 */
@JsonTypeName("userAuthentication")
public class UserAuthenticationWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    public String userToken;
    public Long userId;

    public UserAuthenticationWrapper(String userToken, Long userId){
        this.userId = userId;
        this.userToken = userToken;
    }
}
