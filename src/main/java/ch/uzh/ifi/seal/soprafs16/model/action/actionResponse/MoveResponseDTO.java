package ch.uzh.ifi.seal.soprafs16.model.action.actionResponse;

import java.io.Serializable;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
public class MoveResponseDTO extends ActionResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long wagonLevelId;

    public Long getWagonLevelId() {
        return wagonLevelId;
    }
}
