package ch.uzh.ifi.seal.soprafs16.model.action.actionResponse;

import java.io.Serializable;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;

/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
public class PlayCardResponseDTO extends ActionResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long playedCardId;

    public void setPlayedCardId(Long playedCardId) {
        this.playedCardId = playedCardId;
    }

    public Long getPlayedCardId() {
        return playedCardId;
    }
}
