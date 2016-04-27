package ch.uzh.ifi.seal.soprafs16.model.action;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.CollectItemResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.DrawCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.MoveMarshalResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.MoveResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.PlayCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.PunchResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.ShootResponseDTO;


/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
        @JsonSubTypes.Type(CollectItemResponseDTO.class),
        @JsonSubTypes.Type(MoveMarshalResponseDTO.class),
        @JsonSubTypes.Type(MoveResponseDTO.class),
        @JsonSubTypes.Type(PunchResponseDTO.class),
        @JsonSubTypes.Type(ShootResponseDTO.class),
        @JsonSubTypes.Type(DrawCardResponseDTO.class),
        @JsonSubTypes.Type(PlayCardResponseDTO.class)
})
@JsonDeserialize
public class ActionResponseDTO extends ActionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userID;

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public Long getUserID() {
        return userID;
    }
}
