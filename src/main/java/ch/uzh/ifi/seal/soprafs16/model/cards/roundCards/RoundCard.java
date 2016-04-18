package ch.uzh.ifi.seal.soprafs16.model.cards.roundCards;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import ch.uzh.ifi.seal.soprafs16.model.cards.Card;
import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;

@Entity
public class RoundCard extends Card implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @OneToMany
    private List<Turn> pattern;

    public ArrayList<Turn> getPattern() {
        ArrayList<Turn> turns = new ArrayList<>();
        for(Turn t: pattern){
            turns.add(t);
        }
        return turns;
    }

    public void setPattern(List<Turn> pattern) {
        this.pattern = pattern;
    }

    public int getTurnCount(){
        return pattern.size();
    }
}
