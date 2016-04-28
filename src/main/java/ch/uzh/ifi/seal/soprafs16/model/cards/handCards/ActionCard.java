
package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

@Entity
@JsonTypeName("actionCard")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ShootCard.class, name = "shootCard"),
        @JsonSubTypes.Type(value = CollectCard.class, name = "collectCard"),
        @JsonSubTypes.Type(value = MarshalCard.class, name = "marshalCard"),
        @JsonSubTypes.Type(value = MoveCard.class, name = "moveCard"),
        @JsonSubTypes.Type(value = ChangeLevelCard.class, name = "changeLevelCard"),
        @JsonSubTypes.Type(value = PunchCard.class, name = "punchCard")
})
public abstract class ActionCard extends HandCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public abstract ActionRequestDTO generateActionRequest(Game game, User user);
    public abstract ActionRequestDTO generateMarshalRequest(Game game);
    @Column
    private Long playedByUserId;

    @Column
    private boolean  playedHidden;

    public Long getPlayedByUserId() {
        return playedByUserId;
    }

    public void setPlayedByUserId(Long playedByUserId) {
        this.playedByUserId = playedByUserId;
    }

    public boolean isPlayedHidden() {
        return playedHidden;
    }

    public void setPlayedHidden(boolean playedHidden) {
        this.playedHidden = playedHidden;
    }

}
