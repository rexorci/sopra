package ch.uzh.ifi.seal.soprafs16.model.action.actionRequest;

import java.io.Serializable;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
public class DrawOrPlayCardRequestDTO extends ActionRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long gameId;

    private long userId;

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
