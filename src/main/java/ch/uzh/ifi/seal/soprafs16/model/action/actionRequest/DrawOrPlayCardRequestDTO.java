package ch.uzh.ifi.seal.soprafs16.model.action.actionRequest;

import com.fasterxml.jackson.annotation.JsonTypeName;

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
@JsonTypeName("drawOrPlayCardRequestDTO")
public class DrawOrPlayCardRequestDTO extends ActionRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;



    //private long gameId;

//    @Column
//    private long userId;
    @ElementCollection
    private List<Long> playableCardsId;

    public DrawOrPlayCardRequestDTO()
    {
        this.playableCardsId = new ArrayList<Long>();
    }

//    public long getSpielId() {
//        return gameId;
//    }

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

    public List<Long> getPlayableCardsId() {
        return playableCardsId;
    }

    public void setPlayableCardsId(List<Long> playableCardsId) {
        this.playableCardsId = playableCardsId;
    }
}
