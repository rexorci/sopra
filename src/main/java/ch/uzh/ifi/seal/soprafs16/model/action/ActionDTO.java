package ch.uzh.ifi.seal.soprafs16.model.action;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
@JsonTypeName("actionDTO")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(ActionRequestDTO.class),
        @JsonSubTypes.Type(ActionResponseDTO.class)
})
public class ActionDTO implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue
    private Long id;

    private Long userId;

    public void setUserId(Long userID) {
        this.userId = userID;
    }

    public Long getUserId() {
        return userId;
    }

    @Column
    private Long spielId;

    public Long getSpielId() {
        return spielId;
    }

    public void setSpielId(Long spielId) {
        this.spielId = spielId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}