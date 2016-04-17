package ch.uzh.ifi.seal.soprafs16.model.action.actionRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
public class ShootRequestDTO extends ActionRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Long> shootableUserIds;
    private long gameId;

    public ShootRequestDTO()
    {
        this.shootableUserIds = new ArrayList<Long>();
    }
    public List<Long> getShootableUserIds() {
        return shootableUserIds;
    }

    public void setShootableUserIds(List<Long> shootableUserIds) {
        this.shootableUserIds = shootableUserIds;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }
}
