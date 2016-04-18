package ch.uzh.ifi.seal.soprafs16.model.turns;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.RoundCard;

@Entity
public class Turn implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JsonIgnore
    private RoundCard roundCard;

    public Long getId() {
        return id;
    }

    public RoundCard getRoundCard() {
        return roundCard;
    }

    public void setRoundCard(RoundCard roundCard) {
        this.roundCard = roundCard;
    }
}
