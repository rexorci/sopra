package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

import javax.persistence.Entity;

@Entity
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
        @JsonSubTypes.Type(ShootCard.class),
        @JsonSubTypes.Type(CollectCard.class),
        @JsonSubTypes.Type(MarshalCard.class),
        @JsonSubTypes.Type(MoveCard.class),
        @JsonSubTypes.Type(ChangeLevelCard.class),
        @JsonSubTypes.Type(PunchCard.class)
})
public class ActionCard extends HandCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

}
