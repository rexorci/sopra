package ch.uzh.ifi.seal.soprafs16.model.cards.roundCards;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import ch.uzh.ifi.seal.soprafs16.model.cards.Card;
import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;

@Entity
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
        @JsonSubTypes.Type(StationCard.class),
        @JsonSubTypes.Type(AngryMarshalCard.class),
        @JsonSubTypes.Type(BrakingCard.class),
        @JsonSubTypes.Type(GetItAllCard.class),
        @JsonSubTypes.Type(PassengerRebellionCard.class),
        @JsonSubTypes.Type(BlankTunnelCard.class),
        @JsonSubTypes.Type(BlankBridgeCard.class)
})
public class RoundCard extends Card implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @OneToMany
    private List<Turn> pattern;

    public List<Turn> getPattern() {
        return pattern;
    }

    public void setPattern(List<Turn> pattern) {
        this.pattern = pattern;
    }

    //abstract class not easily possible with JsonMapping
    public  String getStringPattern(){
        return null;
    }
}
