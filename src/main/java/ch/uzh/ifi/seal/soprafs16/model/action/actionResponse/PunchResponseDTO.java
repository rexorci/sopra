package ch.uzh.ifi.seal.soprafs16.model.action.actionResponse;

import java.io.Serializable;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
public class PunchResponseDTO extends ActionResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long victimID;
    private Long wagonLevelID;
    private ItemType itemType;

    public ItemType getItemType() {
        return itemType;
    }

    public Long getWagonLevelID() {
        return wagonLevelID;
    }

    public Long getVictimID() {
        return victimID;
    }

    public void setVictimID(Long victimID) {
        this.victimID = victimID;
    }

    public void setWagonLevelID(Long wagonLevelID) {
        this.wagonLevelID = wagonLevelID;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }
}
