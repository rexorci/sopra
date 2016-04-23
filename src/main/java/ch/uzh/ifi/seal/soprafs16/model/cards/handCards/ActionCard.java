package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
        @JsonSubTypes.Type(ShootCard.class),
        @JsonSubTypes.Type(CollectCard.class),
        @JsonSubTypes.Type(MarshalCard.class),
        @JsonSubTypes.Type(MoveCard.class),
        @JsonSubTypes.Type(ChangeLevelCard.class),
        @JsonSubTypes.Type(PunchCard.class)
})
public class ActionCard extends HandCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

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
