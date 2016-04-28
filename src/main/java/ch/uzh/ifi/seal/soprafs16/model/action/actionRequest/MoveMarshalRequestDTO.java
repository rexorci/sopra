package ch.uzh.ifi.seal.soprafs16.model.action.actionRequest;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

/**
 * Created by Timon Willi on 22.04.2016.
 */
@Entity
@JsonTypeName("moveMarshalRequestDTO")
public class MoveMarshalRequestDTO extends ActionRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ElementCollection
    private List<Long> movableWagonsLvlIds;

    //private long gameId;

    public MoveMarshalRequestDTO()
    {
        this.movableWagonsLvlIds = new ArrayList<Long>();
    }

    public List<Long> getMovableWagonsLvlIds() {
        return movableWagonsLvlIds;
    }

    public void setMovableWagonsLvlIds(List<Long> movableWagonsLvlIds) {
        this.movableWagonsLvlIds = movableWagonsLvlIds;
    }
//
//    public long getSpielId() {
//        return gameId;
//    }
//
//    public void setSpielId(long gameId) {
//        this.gameId = gameId;
//    }
}
