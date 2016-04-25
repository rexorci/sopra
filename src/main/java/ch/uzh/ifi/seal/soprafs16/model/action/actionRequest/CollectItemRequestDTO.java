package ch.uzh.ifi.seal.soprafs16.model.action.actionRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
public class CollectItemRequestDTO extends ActionRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Long> collectableItemIds;
    private long gameId;

    private long userId;

    private Boolean hasCase;
    private Boolean hasGem;
    private Boolean hasBag;

    public CollectItemRequestDTO()
    {
        this.collectableItemIds = new ArrayList<Long>();
    }

    public List<Long> getCollectableItemIds() {
        return collectableItemIds;
    }

    public void setCollectableItemIds(List<Long> collectableItemIds) {
        this.collectableItemIds = collectableItemIds;
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

    public Boolean getHasCase() {
        return hasCase;
    }

    public void setHasCase(Boolean hasCase) {
        this.hasCase = hasCase;
    }

    public Boolean getHasGem() {
        return hasGem;
    }

    public void setHasGem(Boolean hasGem) {
        this.hasGem = hasGem;
    }

    public Boolean getHasBag() {
        return hasBag;
    }

    public void setHasBag(Boolean hasBag) {
        this.hasBag = hasBag;
    }
}
