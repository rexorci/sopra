package ch.uzh.ifi.seal.soprafs16.model.cards.roundCards;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import ch.uzh.ifi.seal.soprafs16.model.cards.Card;
import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;

@Entity
@JsonTypeName("roundCard")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = StationCard.class, name = "stationCard"),
        @JsonSubTypes.Type(value = AngryMarshalCard.class, name = "angryMarshalCard"),
        @JsonSubTypes.Type(value = BrakingCard.class,name = "brakingCard"),
        @JsonSubTypes.Type(value = GetItAllCard.class, name = "getItAllCard"),
        @JsonSubTypes.Type(value = PassengerRebellionCard.class, name = "passengerRebellionCard"),
        @JsonSubTypes.Type(value = BlankTunnelCard.class, name = "blankTunnelCard"),
        @JsonSubTypes.Type(value = BlankBridgeCard.class, name = "blankBridgeCard"),
        @JsonSubTypes.Type(value = PivotablePoleCard.class, name = "pivotablePoleCard")
})
public abstract class RoundCard extends Card implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @OneToMany
    private List<Turn> pattern;

    public List<Turn> getPattern(){
        return pattern;
    }

    public void setPattern(List<Turn> pattern) {
        this.pattern = pattern;
    }

    @JsonIgnore
    public int getTurnCount() {
        return pattern.size();
    }

    public abstract String getStringPattern();
}
