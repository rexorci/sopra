package ch.uzh.ifi.seal.soprafs16.model.cards;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;

@Entity
public class PlayerDeck<T extends HandCard> extends Deck implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @OneToOne
    @JsonIgnore
    private User user;

    public PlayerDeck(){
        this.setCards(new ArrayList<T>());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
