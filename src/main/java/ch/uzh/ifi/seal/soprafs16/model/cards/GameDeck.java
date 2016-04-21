package ch.uzh.ifi.seal.soprafs16.model.cards;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import ch.uzh.ifi.seal.soprafs16.model.Game;

@Entity
public class GameDeck<T extends Card>  extends Deck implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @OneToOne
    @JsonIgnore
    private Game game;

    public GameDeck(){
        this.setCards(new ArrayList<T>());
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
