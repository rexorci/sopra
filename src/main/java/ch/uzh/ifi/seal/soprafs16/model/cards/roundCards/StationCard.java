package ch.uzh.ifi.seal.soprafs16.model.cards.roundCards;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;

@Entity
@JsonTypeName("stationCard")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PickPocketingCard.class, name = "pickPocketingCard"),
        @JsonSubTypes.Type(value = MarshallsRevengeCard.class, name = "marshallsRevengeCard"),
        @JsonSubTypes.Type(value = HostageCard.class, name = "hostageCard")
})
public abstract class StationCard extends RoundCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

}
