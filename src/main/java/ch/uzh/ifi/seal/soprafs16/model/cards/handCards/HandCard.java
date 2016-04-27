package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.cards.Card;

@Entity
@JsonTypeName("handCard")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value=BulletCard.class, name = "bulletCard"),
        @JsonSubTypes.Type(value = ActionCard.class, name = "actionCard")
})
public class HandCard extends Card implements Serializable {

    private static final long serialVersionUID = 1L;
}
