package ch.uzh.ifi.seal.soprafs16.model.action;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.CollectItemRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.DrawOrPlayCardRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.MoveMarshalRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.MoveRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.PunchRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.ShootRequestDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
@JsonTypeName("ActionRequestDTO")
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
        @JsonSubTypes.Type(CollectItemRequestDTO.class),
        @JsonSubTypes.Type(MoveMarshalRequestDTO.class),
        @JsonSubTypes.Type(MoveRequestDTO.class),
        @JsonSubTypes.Type(PunchRequestDTO.class),
        @JsonSubTypes.Type(ShootRequestDTO.class),
        @JsonSubTypes.Type(DrawOrPlayCardRequestDTO.class)
})
public class ActionRequestDTO extends ActionDTO implements Serializable {

    private static final long serialVersionUID = 1L;



    @ManyToOne
    @JsonIgnore
    private Game game;

//    private long gameId;
    //private long userId;

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

}