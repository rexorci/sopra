package ch.uzh.ifi.seal.soprafs16.model.action;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

import javax.persistence.ManyToOne;

import ch.uzh.ifi.seal.soprafs16.model.Game;

/**
 * Created by Timon Willi on 17.04.2016.
 */
public abstract class ActionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private long gameId;

    public long getGameId() {
        return gameId;
    }
}
