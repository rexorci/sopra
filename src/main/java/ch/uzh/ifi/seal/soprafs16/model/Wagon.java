package ch.uzh.ifi.seal.soprafs16.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class Wagon implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Game game;

    @OneToOne
    private WagonLevel topLevel;

    @OneToOne
    private WagonLevel bottomLevel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public WagonLevel getTopLevel(){
        return topLevel;
    }

    public void setTopLevel(WagonLevel topLevel){
        this.topLevel = topLevel;
    }

    public WagonLevel getBottomLevel(){
        return bottomLevel;
    }

    public void setBottomLevel(WagonLevel bottomLevel){
        this.bottomLevel = bottomLevel;
    }
}
