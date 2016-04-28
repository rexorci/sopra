package ch.uzh.ifi.seal.soprafs16.model.action.actionRequest;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
@JsonTypeName("punchRequestDTO")
public class PunchRequestDTO extends ActionRequestDTO implements Serializable {


    private static final long serialVersionUID = 1L;

//    private long gameId;

//    @Column
//    private long userId;
    @ElementCollection
    private List<Long> punchableUserIds;
    @ElementCollection
    private List<Boolean> hasGem;
    @ElementCollection
    private List<Boolean> hasBag;
    @ElementCollection
    private List<Boolean> hasCase;
    @ElementCollection
    private List<Long> movable;

    public PunchRequestDTO()
    {
        this.punchableUserIds = new ArrayList<Long>();
        this.hasGem = new ArrayList<Boolean>();
        this.hasBag = new ArrayList<Boolean>();
        this.hasCase = new ArrayList<Boolean>();
        this.movable = new ArrayList<Long>();
    }

    public List<Long> getPunchableUserIds() {
        return punchableUserIds;
    }

    public void setPunchableUserIds(List<Long> punchableUserIds) {
        this.punchableUserIds = punchableUserIds;
    }

//    public long getSpielId() {
//        return gameId;
//    }
//
//    public void setSpielId(long gameId) {
//        this.gameId = gameId;
//    }

//    public long getUserId() {
//        return userId;
//    }
//
//    public void setUserId(long userId) {
//        this.userId = userId;
//    }

    public List<Boolean> getHasGem() {
        return hasGem;
    }

    public void setHasGem(List<Boolean> hasGem) {
        this.hasGem = hasGem;
    }

    public List<Boolean> getHasBag() {
        return hasBag;
    }

    public void setHasBag(List<Boolean> hasBag) {
        this.hasBag = hasBag;
    }

    public List<Boolean> getHasCase() {
        return hasCase;
    }

    public void setHasCase(List<Boolean> hasCase) {
        this.hasCase = hasCase;
    }

    public List<Long> getMovable() {
        return movable;
    }

    public void setMovable(List<Long> movable) {
        this.movable = movable;
    }
}
