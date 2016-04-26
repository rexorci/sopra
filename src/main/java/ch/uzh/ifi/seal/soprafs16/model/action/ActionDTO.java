package ch.uzh.ifi.seal.soprafs16.model.action;

import java.io.Serializable;

/**
 * Created by Timon Willi on 17.04.2016.
 */
public abstract class ActionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private long gameId;

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public long getGameId() {
        return gameId;
    }
}
