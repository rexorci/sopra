package ch.uzh.ifi.seal.soprafs16.model.action;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;

/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
public abstract class ActionRequestDTO extends ActionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Game game;

    private long gamerId;
    private long userId;

}
