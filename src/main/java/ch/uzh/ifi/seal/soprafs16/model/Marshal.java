package ch.uzh.ifi.seal.soprafs16.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;

@Entity
public class Marshal implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JsonIgnore
    private Game game;

    @OneToOne
    @JsonIgnore
    private WagonLevel wagonLevel;

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public WagonLevel getWagonLevel() {
        return wagonLevel;
    }

    public void setWagonLevel(WagonLevel wagonLevel) {
        this.wagonLevel = wagonLevel;
    }
}
