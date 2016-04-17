package ch.uzh.ifi.seal.soprafs16.model;

import javax.persistence.GeneratedValue;
import javax.persistence.OneToOne;
import javax.persistence.Id;

/**
 * Created by Nico on 13.04.2016.
 */
public class RoundCard extends Card {
    @Id
    @GeneratedValue
    private long id;

    @OneToOne
    private Turn[] pattern;

    public Turn[] getPattern() {
        return pattern;
    }

    public void setPattern(Turn[] pattern) {
        this.pattern = pattern;
    }
}
