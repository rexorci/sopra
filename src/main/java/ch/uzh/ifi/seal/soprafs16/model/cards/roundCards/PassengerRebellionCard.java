package ch.uzh.ifi.seal.soprafs16.model.cards.roundCards;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;


@Entity
@JsonTypeName("passengerRebellionCard")
public class PassengerRebellionCard extends RoundCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    public String getStringPattern(){
        return "NNTNN";
    }
}
