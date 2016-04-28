package ch.uzh.ifi.seal.soprafs16.model.characters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import ch.uzh.ifi.seal.soprafs16.model.User;

@Entity
@JsonTypeName("character")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value=Belle.class, name = "belle"),
        @JsonSubTypes.Type(value=Cheyenne.class, name = "cheyenne"),
        @JsonSubTypes.Type(value=Django.class, name = "django"),
        @JsonSubTypes.Type(value=Doc.class, name = "doc"),
        @JsonSubTypes.Type(value=Ghost.class, name = "ghost"),
        @JsonSubTypes.Type(value=Tuco.class, name = "tuco")
})
public class Character implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @OneToOne
    //@JsonView
    @JsonIgnore
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }
}
