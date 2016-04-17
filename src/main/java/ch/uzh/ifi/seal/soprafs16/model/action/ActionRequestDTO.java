package ch.uzh.ifi.seal.soprafs16.model.action;

import java.io.Serializable;

import ch.uzh.ifi.seal.soprafs16.model.User;

/**
 * Created by Timon Willi on 17.04.2016.
 */
public abstract class ActionRequestDTO extends ActionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private User requestedPlayer;
}
