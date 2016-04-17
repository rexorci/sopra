package ch.uzh.ifi.seal.soprafs16.model.action;

import java.io.Serializable;

/**
 * Created by Timon Willi on 17.04.2016.
 */
public abstract class ActionResponseDTO extends ActionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userToken;
}
