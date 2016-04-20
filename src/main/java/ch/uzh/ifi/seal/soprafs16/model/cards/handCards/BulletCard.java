package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import ch.uzh.ifi.seal.soprafs16.constant.SourceType;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;

@Entity
public class BulletCard extends HandCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Column
    private SourceType sourceType;

    @Column
    private int bulletCounter;

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public int getBulletCounter() {
        return bulletCounter;
    }

    public void setBulletCounter(int bulletCounter) {
        this.bulletCounter = bulletCounter;
    }
}
