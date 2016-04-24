package ch.uzh.ifi.seal.soprafs16.model.action.actionResponse;

import java.io.Serializable;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
public class CollectItemResponseDTO extends ActionResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private ItemType collectedItemType;
    private Long id;

    public Long getId() {
        return id;
    }

    public ItemType getCollectedItemType() {
        return collectedItemType;
    }

    public void setCollectedItemType(ItemType collectedItemType) {
        this.collectedItemType = collectedItemType;
    }
}
