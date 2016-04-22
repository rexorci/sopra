package ch.uzh.ifi.seal.soprafs16.model.action.actionResponse;

import java.io.Serializable;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;

/**
 * Created by Nico on 22.04.2016.
 */
public class MoveMarshalResponseDTO extends ActionResponseDTO implements Serializable{

    private static final long serialVersionUID = 1L;

    private Long wagonLevelId;

    public void setWagonLevelId(Long wagonLevelId) {
        this.wagonLevelId = wagonLevelId;
    }

    public Long getWagonLevelId() {
        return wagonLevelId;
    }
}
