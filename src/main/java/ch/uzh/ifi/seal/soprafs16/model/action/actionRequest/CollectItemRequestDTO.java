package ch.uzh.ifi.seal.soprafs16.model.action.actionRequest;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
public class CollectItemRequestDTO extends ActionRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ElementCollection
    private List<Long> collectableItemIds;


    @Column
    private Long userId;

    @Column
    private Boolean hasCase;
    @Column
    private Boolean hasGem;

    @Column
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
