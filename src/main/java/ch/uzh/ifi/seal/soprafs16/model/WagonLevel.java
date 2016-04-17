package ch.uzh.ifi.seal.soprafs16.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import ch.uzh.ifi.seal.soprafs16.constant.LevelType;

@Entity
public class WagonLevel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JsonIgnore
    private Wagon wagon;

    @OneToMany
    private List<Item> items;

    @Column
    private LevelType levelType;

    @OneToOne
    private Marshal marshal;

    @OneToMany
    private List<User> users;

    @JsonIgnore
    @OneToOne
    private WagonLevel wagonLevelBefore;

    @JsonIgnore
    @OneToOne
    private WagonLevel wagonLevelAfter;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Wagon getWagon() {
        return wagon;
    }

    public void setWagon(Wagon wagon) {
        this.wagon = wagon;
    }

    public LevelType getLevelType() {
        return levelType;
    }

    public void setLevelType(LevelType levelType) {
        this.levelType = levelType;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Marshal getMarshal() {
        return marshal;
    }

    public void setMarshal(Marshal marshal) {
        this.marshal = marshal;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public WagonLevel getWagonLevelAfter() {
        return wagonLevelAfter;
    }

    public void setWagonLevelAfter(WagonLevel wagonLevelAfter) {
        this.wagonLevelAfter = wagonLevelAfter;
    }

    public WagonLevel getWagonLevelBefore() {
        return wagonLevelBefore;
    }

    public void setWagonLevelBefore(WagonLevel wagonLevelBefore) {
        this.wagonLevelBefore = wagonLevelBefore;
    }
}
