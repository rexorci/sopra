package ch.uzh.ifi.seal.soprafs16.model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.constant.PhaseType;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.RoundCard;
import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

@Entity
public class Game implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String owner;

    @Column
    private GameStatus status;

    @Column
    private Integer currentPlayer;

    @OneToMany(mappedBy = "game")
    private List<User> users;

    @OneToMany(mappedBy = "game")
    private List<Wagon> wagons;

    @OneToOne
    private Marshal marshal;

    @Column
    private Integer currentRound;

    @Column
    private Integer currentTurn;

    @Column
    private PhaseType currentPhase;

    @OneToOne
    private GameDeck<RoundCard> roundCardDeck;

    @OneToOne
    private GameDeck<ActionCard> commonDeck;

    @JsonIgnore
    @Column
    private int actionCounter;

    @JsonIgnore
    @Column
    private int roundStarter;

    @OneToMany
    private List<ActionRequestDTO> actions;

    @ElementCollection
    private List<String> log;

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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public Integer getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Integer currentPlayer) {
        LoggerFactory.getLogger("Game").debug("setCurrentPlayer: " + currentPlayer);
        this.currentPlayer = currentPlayer;
    }

    public List<Wagon> getWagons() {
        return wagons;
    }

    public void setWagons(List<Wagon> wagons) {
        this.wagons = wagons;
    }

    public Marshal getMarshal() {
        return marshal;
    }

    public void setMarshal(Marshal marshal) {
        this.marshal = marshal;
    }

    public Integer getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(Integer currentRound) {
        this.currentRound = currentRound;
    }

    public Integer getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(Integer currentTurn) {
        this.currentTurn = currentTurn;
    }

    public PhaseType getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(PhaseType currentPhase) {
        this.currentPhase = currentPhase;
    }

    public List<String> getLog() {
        return log;
    }

    public void setLog(List<String> log) {
        this.log = log;
    }

    public GameDeck<ActionCard> getCommonDeck() {
        return commonDeck;
    }

    public void setCommonDeck(GameDeck<ActionCard> commonDeck) {
        this.commonDeck = commonDeck;
    }

    public List<ActionRequestDTO> getActions() {
        return actions;
    }

    public void setActions(List<ActionRequestDTO> actions) {
        this.actions = actions;
    }

    public GameDeck<RoundCard> getRoundCardDeck() {
        return roundCardDeck;
    }

    public void setRoundCardDeck(GameDeck<RoundCard> roundCardDeck) {
        this.roundCardDeck = roundCardDeck;
    }

    public Turn getCurrentTurnType(){
        if(roundCardDeck != null) {
            return ((RoundCard) roundCardDeck.getCards().get(currentRound)).getPattern().get(currentTurn);
        }
        else return null;
    }

    public int getActionCounter() {
        return actionCounter;
    }

    public void setActionCounter(int actionCounter) {
        this.actionCounter = actionCounter;
    }

    public int getRoundStarter() {
        return roundStarter;
    }

    public void setRoundStarter(int roundStarter) {
        this.roundStarter = roundStarter;
    }
}