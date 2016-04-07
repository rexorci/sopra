package ch.uzh.ifi.seal.soprafs16.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import ch.uzh.ifi.seal.soprafs16.constant.ActionType;

@Entity
public class Action implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Game game;

    @ElementCollection
    private List<Long> neededUserIds;

    @Column
    private ActionType actionType;

    @ElementCollection
    private List<Long> shootableUserIds;

    @ElementCollection
    private List<Long> punchableUserIds;

    @ElementCollection
    private List<Long> collectableItemIds;

    @ElementCollection
    private List<Integer> moveOptions;

    @Column
    private Long selectedCardid;

    @Column
    private Long shootTargetUserId;

    @Column
    private Long punchTargetUserId;

    @Column
    private Long collectTargetItemId;

    @Column
    private Integer moveTargetSteps;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public List<Long> getNeededUserIds() {
        return neededUserIds;
    }

    public void setNeededUserIds(List<Long> neededUserIds) {
        this.neededUserIds = neededUserIds;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public List<Long> getShootableUserIds() {
        return shootableUserIds;
    }

    public void setShootableUserIds(List<Long> shootableUserIds) {
        this.shootableUserIds = shootableUserIds;
    }

    public List<Long> getPunchableUserIds() {
        return punchableUserIds;
    }

    public void setPunchableUserIds(List<Long> punchableUserIds) {
        this.punchableUserIds = punchableUserIds;
    }

    public List<Long> getCollectableItemIds() {
        return collectableItemIds;
    }

    public void setCollectableItemIds(List<Long> collectableItemIds) {
        this.collectableItemIds = collectableItemIds;
    }

    public List<Integer> getMoveOptions() {
        return moveOptions;
    }

    public void setMoveOptions(List<Integer> moveOptions) {
        this.moveOptions = moveOptions;
    }

    public Long getSelectedCardid() {
        return selectedCardid;
    }

    public void setSelectedCardid(Long selectedCardid) {
        this.selectedCardid = selectedCardid;
    }

    public Long getShootTargetUserId() {
        return shootTargetUserId;
    }

    public void setShootTargetUserId(Long shootTargetUserId) {
        this.shootTargetUserId = shootTargetUserId;
    }

    public Long getPunchTargetUserId() {
        return punchTargetUserId;
    }

    public void setPunchTargetUserId(Long punchTargetUserId) {
        this.punchTargetUserId = punchTargetUserId;
    }

    public Long getCollectTargetItemId() {
        return collectTargetItemId;
    }

    public void setCollectTargetItemId(Long collectTargetItemId) {
        this.collectTargetItemId = collectTargetItemId;
    }

    public Integer getMoveTargetSteps() {
        return moveTargetSteps;
    }

    public void setMoveTargetSteps(Integer moveTargetSteps) {
        this.moveTargetSteps = moveTargetSteps;
    }
}
