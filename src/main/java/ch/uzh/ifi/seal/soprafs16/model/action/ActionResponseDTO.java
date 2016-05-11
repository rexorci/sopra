package ch.uzh.ifi.seal.soprafs16.model.action;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

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
@JsonTypeName("actionResponseDTO")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CollectItemResponseDTO.class, name = "collectItemResponseDTO"),
        @JsonSubTypes.Type(value = MoveMarshalResponseDTO.class, name = "moveMarshalResponseDTO"),
        @JsonSubTypes.Type(value = MoveResponseDTO.class, name = "moveResponseDTO"),
        @JsonSubTypes.Type(value = PunchResponseDTO.class, name = "punchResponseDTO"),
        @JsonSubTypes.Type(value = ShootResponseDTO.class, name = "shootResponseDTO"),
        @JsonSubTypes.Type(value = DrawCardResponseDTO.class, name = "drawCardResponseDTO"),
        @JsonSubTypes.Type(value = PlayCardResponseDTO.class, name = "playCardResponseDTO")
})
public class ActionResponseDTO extends ActionDTO implements Serializable {

    private static final long serialVersionUID = 1L;
}
