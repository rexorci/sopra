package ch.uzh.ifi.seal.soprafs16.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import ch.uzh.ifi.seal.soprafs16.constant.UserStatus;

@Entity
public class User implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private UserStatus status;

    @ManyToOne
    @JsonIgnore
    private Game game;

//    @OneToMany(mappedBy = "user")
//    private List<Move> moves;

    @ManyToOne
    @JsonIgnore
    private WagonLevel wagonLevel;

    @OneToMany
    private List<Item> items;

    //region helper Variables for Serialization
//    @Column
//    private Long gameIdOld;
//    @Column
//    private Long wagonLevelIdOld;
//    @Column
//    private Long gameIdNew;
    @Column
    private Long wagonLevelIdNew;
    //endregion

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

//    public List<Move> getMoves() {
//        return moves;
//    }
//
//    public void setMoves(List<Move> moves) {
//        this.moves = moves;
//    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
    @JsonIgnore
    public WagonLevel getWagonLevel() {
        return wagonLevel;
    }

    public void setWagonLevel(WagonLevel wagonLevel) {
        this.wagonLevel = wagonLevel;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    //region helper Variables
//    public Long getGameIdOld() {
//        return gameIdOld;
//    }
//
//    public void setGameIdOld(Long gameIdOld) {
//        this.gameIdOld = gameIdOld;
//    }

//    public Long getWagonLevelIdOld() {
//        return wagonLevelIdOld;
//    }
//
//    public void setWagonLevelIdOld(Long wagonLevelIdOld) {
//        this.wagonLevelIdOld = wagonLevelIdOld;
//    }

//    public Long getGameIdNew() {
//        return gameIdNew;
//    }
//
//    public void setGameIdNew(Long gameIdNew) {
//        this.gameIdNew = gameIdNew;
//    }

    public Long getWagonLevelIdNew() {
        return wagonLevelIdNew;
    }

    public void setWagonLevelIdNew(Long wagonLevelIdNew) {
        this.wagonLevelIdNew = wagonLevelIdNew;
    }

    //endregion

}
