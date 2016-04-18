package ch.uzh.ifi.seal.soprafs16.model.action.actionRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.seal.soprafs16.model.Action;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
public class PunchRequestDTO extends ActionRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long gameId;

    private long userId;

    private List<Long> punchableUserIds;

    public PunchRequestDTO()
    {
        this.punchableUserIds = new ArrayList<Long>();
    }

    public List<Long> getPunchableUserIds() {
        return punchableUserIds;
    }

    public void setPunchableUserIds(List<Long> punchableUserIds) {
        this.punchableUserIds = punchableUserIds;
    }

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
