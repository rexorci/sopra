package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import java.io.Serializable;

import javax.persistence.Entity;

@Entity
public abstract class ActionCard extends HandCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public void createActionRequest(){

    }
}
