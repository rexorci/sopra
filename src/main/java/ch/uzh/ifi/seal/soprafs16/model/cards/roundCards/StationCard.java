package ch.uzh.ifi.seal.soprafs16.model.cards.roundCards;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

import javax.persistence.Entity;

@Entity
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
        @JsonSubTypes.Type(PickPocketingCard.class),
        @JsonSubTypes.Type(MarshallsRevengeCard.class),
        @JsonSubTypes.Type(HostageCard.class)
})
public class StationCard extends RoundCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

}
