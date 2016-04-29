package ch.uzh.ifi.seal.soprafs16.service;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.seal.soprafs16.GameConstants;
import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.constant.PhaseType;
import ch.uzh.ifi.seal.soprafs16.controller.GenericService;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.Item;
import ch.uzh.ifi.seal.soprafs16.model.Marshal;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.CollectItemRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.DrawOrPlayCardRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.MoveMarshalRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.MoveRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.PunchRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.ShootRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ChangeLevelCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.CollectCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.MarshalCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.MoveCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.PunchCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ShootCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.AngryMarshalCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.BrakingCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.GetItAllCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.HostageCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.MarshallsRevengeCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.PassengerRebellionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.PickPocketingCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.PivotablePoleCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.RoundCard;
import ch.uzh.ifi.seal.soprafs16.model.characters.Belle;
import ch.uzh.ifi.seal.soprafs16.model.characters.Django;
import ch.uzh.ifi.seal.soprafs16.model.characters.Doc;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ActionRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CharacterRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonRepository;
import ch.uzh.ifi.seal.soprafs16.model.turns.ReverseTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.SpeedupTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;

/**
 * Created by Nico on 05.04.2016.
 */
@Service
@Transactional
public class GameLogicService extends GenericService {

    Logger logger = LoggerFactory.getLogger(GameLogicService.class);
    //region Repositories
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private GameRepository gameRepo;
    @Autowired
    private WagonRepository wagonRepo;
    @Autowired
    private WagonLevelRepository wagonLevelRepo;
    @Autowired
    private ItemRepository itemRepo;
    @Autowired
    private MarshalRepository marshalRepo;
    @Autowired
    private CharacterRepository characterRepo;
    @Autowired
    private CardRepository cardRepo;
    @Autowired
    private DeckRepository deckRepo;
    @Autowired
    private ActionRepository actionRepo;
    @Autowired
    private ActionResponseService ars;
    //endregion

    public void update(Long id) {
        Game game = gameRepo.findOne(id);
        logger.debug("update started");

        Hibernate.initialize(game.getUsers());
        setPhase(game);

        if (game.getStatus() == GameStatus.FINISHED) return;

        if (game.getCurrentPhase() == PhaseType.PLANNING) {
            if (game.getActionRequestCounter() > 0) {
                setNextTurn(game, game.getUsers().size());
                setNextPlayer(game, game.getUsers().size());
            }
            processPlayerTurn(game);

        } else if (game.getCurrentPhase() == PhaseType.EXECUTION) {
            // Remove ActionCard and return it to player Deck
            processCommonDeck(game);
            game = gameRepo.findOne(game.getId());
        }

        gameRepo.save(game);
        game.getUsers().size();
    }

    private void setPhase(Game game) {
        if (game.getCurrentPhase() == PhaseType.EXECUTION && game.getCommonDeck().size() == 0) {
            if (!endRound(game)) game.setCurrentPhase(PhaseType.PLANNING);
        } else if (game.getCurrentPhase() == PhaseType.PLANNING && game.getActionRequestCounter() == calculatePlanningARcounter(game)) {
            game.setCurrentPhase(PhaseType.EXECUTION);
        }
    }

    private void processCommonDeck(Game game) {
        System.out.println("processCommonDeck");
        GameDeck<ActionCard> commonDeck = (GameDeck<ActionCard>) deckRepo.findOne(game.getCommonDeck().getId());
        ActionCard ac = (ActionCard) cardRepo.findOne(commonDeck.remove(0).getId());
        User user = userRepo.findOne(ac.getPlayedByUserId());
        PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>) deckRepo.findOne(user.getHiddenDeck().getId());
        ac.setPlayedByUserId(user.getId());
        ac.setDeck(hiddenDeck);
        hiddenDeck.getCards().add(ac);

        for (int i = 0; i < game.getUsers().size(); i++) {
            if (game.getUsers().get(i).getId().equals(ac.getPlayedByUserId()))
                game.setCurrentPlayer(i);
        }

        ac = cardRepo.save(ac);
        deckRepo.save(hiddenDeck);
        deckRepo.save(commonDeck);

        ActionRequestHelper actionRequestHelper = new ActionRequestHelper();
        ActionRequestDTO ardto = actionRequestHelper.execute(ac, game.getId(), user.getId());

        if(ardto == null){
            update(game.getId());
        }
    }

    private int calculatePlanningARcounter(Game game) {
        int sum = 0;
        RoundCard r = (RoundCard) game.getRoundCardDeck().get(game.getCurrentRound());
        for (Turn t : r.getPattern()) {
            sum += game.getUsers().size();
            // SpeedUp turn requires 2 action requests per user
            if (t instanceof SpeedupTurn) {
                sum += game.getUsers().size();
            }
        }

        return sum;
    }

    private boolean endRound(Game game) {
        // Next round is triggered
        game.setCurrentRound(game.getCurrentRound() + 1);
        if (game.getCurrentRound().equals(GameConstants.ROUNDS)) {
            finishGame(game);
            return true;
        } else {
            System.out.println("round " + game.getCurrentRound() + " end");
            executeRoundAction(game);
            resetPlayerDecks(game);
            game.setRoundStarter((game.getRoundStarter() + 1) % game.getUsers().size());
            game.setCurrentPlayer(game.getRoundStarter());
            game.setCurrentTurn(0);
            game.setCurrentPhase(PhaseType.PLANNING);
            game.setActionRequestCounter(0);
            return false;
        }
    }

    private void executeRoundAction(Game game) {
        RoundCard rc = (RoundCard) cardRepo.findOne(game.getRoundCardDeck().get(game.getCurrentRound()-1).getId());
        RoundEndActionHelper reaHelper = new RoundEndActionHelper();
        reaHelper.execute(rc, game.getId());
    }

    private void processPlayerTurn(Game game) {
        logger.debug("processPlayerTurn");
        createDOPCRequestDTO(game);
        game.setActionRequestCounter(game.getActionRequestCounter() + 1);
    }

    private void setNextTurn(Game game, int playerCounter) {
        logger.debug("setNextTurn");
        // Turn end
        if ((!(game.getCurrentTurnType() instanceof SpeedupTurn) && game.getActionRequestCounter() % playerCounter == 0)
                || (game.getCurrentTurnType() instanceof SpeedupTurn && game.getActionRequestCounter() == (game.getCurrentTurn() + 2) * playerCounter)) {
            game.setCurrentTurn(game.getCurrentTurn() + 1);
            if (game.getCurrentTurn() < ((RoundCard) (game.getRoundCardDeck().get(
                    game.getCurrentRound()))).getPattern().size()
                    && game.getCurrentTurnType() instanceof ReverseTurn) {
                game.setCurrentPlayer((game.getRoundStarter() + 1) % playerCounter); // correction
            }
        }
    }

    private void finishGame(Game game) {
        game.setStatus(GameStatus.FINISHED);
        gameRepo.save(game);
    }

    private void setNextPlayer(Game game, int playerCounter) {
        Turn t = game.getCurrentTurnType();
        if (!(t instanceof SpeedupTurn) || game.getActionRequestCounter() % 2 == 0) {
            if (t instanceof ReverseTurn) {
                game.setCurrentPlayer(mod(game.getCurrentPlayer() - 1, playerCounter));

            } else {
                game.setCurrentPlayer(mod(game.getCurrentPlayer() + 1, playerCounter));
            }
        }
    }

    private void createDOPCRequestDTO(Game game) {
        logger.debug("DOPCrequest created");
        int currentplayer = game.getCurrentPlayer();
        DrawOrPlayCardRequestDTO doprq = new DrawOrPlayCardRequestDTO();
        int size = game.getUsers().get(currentplayer).getHandDeck().size();
        for (int i = 0; i < size; i++) {
            if (game.getUsers().get(currentplayer).getHandDeck().get(i).getClass() != BulletCard.class) {
                doprq.getPlayableCardsId().add(game.getUsers().get(currentplayer).getHandDeck().get(i).getId());
            }
        }

        doprq.setSpielId(game.getId());
        doprq.setUserId(game.getUsers().get(currentplayer).getId());
        doprq = actionRepo.save(doprq);
        game.getActions().add(doprq);

        gameRepo.save(game);
    }

    private void resetPlayerDecks(Game game) {
        Hibernate.initialize(game.getUsers());
        List<User> users = game.getUsers();
        for (User u : users) {
            u = userRepo.findOne(u.getId());
            PlayerDeck<HandCard> handDeck = (PlayerDeck<HandCard>) deckRepo.findOne(u.getHandDeck().getId());
            PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>) deckRepo.findOne(u.getHiddenDeck().getId());

            while (handDeck.size() > 0) {
                HandCard hc = (HandCard) handDeck.remove(0);
                hc = (HandCard) cardRepo.findOne(hc.getId());
                hc.setDeck(hiddenDeck);
                hiddenDeck.add(hc);

                cardRepo.save(hc);
            }

            for (int i = 0; i < 6; i++) {
                HandCard hc = (HandCard) hiddenDeck.remove((int) (Math.random() * hiddenDeck.size()));
                hc = (HandCard) cardRepo.findOne(hc.getId());
                hc.setDeck(handDeck);
                handDeck.add(hc);

                cardRepo.save(hc);
            }

            // Character Skill Doc
            if (u.getCharacter() instanceof Doc) {
                HandCard hc = (HandCard) hiddenDeck.remove((int) (Math.random() * hiddenDeck.size()));
                hc = (HandCard) cardRepo.findOne(hc.getId());
                hc.setDeck(handDeck);
                handDeck.add(hc);

                cardRepo.save(hc);
            }

            deckRepo.save(handDeck);
            deckRepo.save(hiddenDeck);
        }
    }

    public ActionRequestDTO createActionRequest(ActionCard actioncard, Long gameId, Long userId) {
        ActionRequestHelper arh = new ActionRequestHelper();
        return arh.execute(actioncard, gameId, userId);
    }

    private int mod(int a, int b) {
        int ret = a % b;
        return ret < 0 ? b + ret : ret;
    }

    /// TODO: testing
    private class RoundEndActionHelper {
        private void execute(RoundCard rc, Long gameId) {
            if (rc instanceof AngryMarshalCard) {
                execute((AngryMarshalCard) rc, gameId);
            } else if (rc instanceof BrakingCard) {
                execute((BrakingCard) rc, gameId);
            } else if (rc instanceof GetItAllCard) {
                execute((GetItAllCard) rc, gameId);
            } else if (rc instanceof HostageCard) {
                execute((HostageCard) rc, gameId);
            } else if (rc instanceof MarshallsRevengeCard) {
                execute((MarshallsRevengeCard) rc, gameId);
            } else if (rc instanceof PassengerRebellionCard) {
                execute((PassengerRebellionCard) rc, gameId);
            } else if (rc instanceof PickPocketingCard) {
                execute((PickPocketingCard) rc, gameId);
            } else if (rc instanceof PivotablePoleCard) {
                execute((PivotablePoleCard) rc, gameId);
            }
        }

        private void execute(AngryMarshalCard amc, Long gameId) {
            Game game = gameRepo.findOne(gameId);
            Marshal marshal = marshalRepo.findOne(game.getMarshal().getId());
            WagonLevel wl = wagonLevelRepo.findOne(marshal.getWagonLevel().getId());
            WagonLevel wlTop = wagonLevelRepo.findOne(wl.getWagon().getTopLevel().getId());
            GameDeck<BulletCard> neutralBulletsDeck = (GameDeck<BulletCard>) deckRepo.findOne(game.getNeutralBulletsDeck().getId());

            Hibernate.initialize(wlTop.getUsers());
            for (User u : wlTop.getUsers()) {
                if (game.getNeutralBulletsDeck().size() > 0) {
                    BulletCard bc = (BulletCard) neutralBulletsDeck.remove(0);
                    PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>) deckRepo.findOne((u.getHiddenDeck().getId()));
                    bc.setDeck(hiddenDeck);
                    hiddenDeck.add(bc);
                    cardRepo.save(bc);
                    deckRepo.save(hiddenDeck);
                }
            }

            deckRepo.save(neutralBulletsDeck);

            if (wl.getWagonLevelAfter() != null) {
                WagonLevel wlAfter = wagonLevelRepo.findOne(wl.getWagonLevelAfter().getId());
                wl.setMarshal(null);
                marshal.setWagonLevel(wlAfter);
                wlAfter.setMarshal(marshal);

                marshalRepo.save(marshal);
                wagonLevelRepo.save(wl);
                wagonLevelRepo.save(wlAfter);
            }
        }

        private void execute(BrakingCard bc, Long gameId) {
            Game game = gameRepo.findOne(gameId);
            for (User u : game.getUsers()) {
                User user = userRepo.findOne(u.getId());
                if (user.getWagonLevel().getLevelType() == LevelType.TOP
                        && user.getWagonLevel().getWagon().getId() != game.getWagons().get(0).getId()) {
                    WagonLevel wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());
                    WagonLevel before = wagonLevelRepo.findOne(wl.getWagonLevelBefore().getId());

                    user.setWagonLevel(before);
                    wl.removeUserById(user.getId());
                    before.getUsers().add(user);

                    userRepo.save(user);
                    wagonLevelRepo.save(wl);
                    wagonLevelRepo.save(before);
                }
            }
        }

        private void execute(GetItAllCard giac, Long gameId) {
            Game game = gameRepo.findOne(gameId);
            Marshal marshal = game.getMarshal();
            WagonLevel wl = wagonLevelRepo.findOne(marshal.getWagonLevel().getId());

            Item moneyCase = new Item();
            moneyCase.setUser(null);
            moneyCase.setWagonLevel(wl);
            moneyCase.setItemType(ItemType.CASE);
            moneyCase.setValue(1000);

            wl.getItems().add(moneyCase);

            itemRepo.save(moneyCase);
            wagonLevelRepo.save(wl);
        }

        private void execute(HostageCard hc, Long gameId) {
            Game game = gameRepo.findOne(gameId);
            WagonLevel locTop = wagonLevelRepo.findOne(game.getWagons().get(0).getTopLevel().getId());
            WagonLevel locBottom = wagonLevelRepo.findOne(game.getWagons().get(0).getBottomLevel().getId());

            for (User u : locTop.getUsers()) {
                Item bag = new Item();
                bag.setValue(250);
                bag.setUser(u);
                bag.setWagonLevel(null);
                bag.setItemType(ItemType.BAG);

                itemRepo.save(bag);
                u.getItems().add(bag);
                userRepo.save(u);
            }

            for (User u : locBottom.getUsers()) {
                Item bag = new Item();
                bag.setValue(250);
                bag.setUser(u);
                bag.setWagonLevel(null);
                bag.setItemType(ItemType.BAG);

                itemRepo.save(bag);
                u.getItems().add(bag);
                userRepo.save(u);
            }
        }

        private void execute(MarshallsRevengeCard mrc, Long gameId) {
            Game game = gameRepo.findOne(gameId);
            WagonLevel wl = wagonLevelRepo.findOne(game.getMarshal().getWagonLevel().getWagon().getTopLevel().getId());

            for (User u : wl.getUsers()) {
                Item item = getMinPurse(u);

                if (item != null) {
                    u = userRepo.findOne(u.getId());
                    item = itemRepo.findOne(item.getId());
                    u.removeItemById(item.getId());
                    item.setUser(null);
                    item.setWagonLevel(wl);
                    wl.getItems().add(item);

                    itemRepo.save(item);
                    userRepo.save(u);
                    wagonLevelRepo.save(wl);
                }
            }
        }

        private void execute(PassengerRebellionCard prc, Long gameId) {
            Game game = gameRepo.findOne(gameId);
            for (User u : game.getUsers()) {
                if (u.getWagonLevel().getLevelType() == LevelType.BOTTOM) {
                    if (game.getNeutralBulletsDeck().size() > 0) {
                        u = userRepo.findOne(u.getId());
                        GameDeck<BulletCard> neutralBulletsDeck = (GameDeck<BulletCard>) deckRepo.findOne(game.getNeutralBulletsDeck().getId());
                        PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>) deckRepo.findOne(u.getHiddenDeck().getId());
                        BulletCard bc = (BulletCard) cardRepo.findOne(neutralBulletsDeck.remove(0).getId());

                        bc.setDeck(hiddenDeck);
                        hiddenDeck.add(bc);

                        cardRepo.save(bc);
                        deckRepo.save(neutralBulletsDeck);
                        deckRepo.save(hiddenDeck);
                    }
                }
            }
        }

        private void execute(PickPocketingCard ppc, Long gameId) {
            Game game = gameRepo.findOne(gameId);

            for (User u : game.getUsers()) {
                if (u.getWagonLevel().getUsers().size() == 1) {
                    Item item = getRandomItem(ItemType.BAG, u.getWagonLevel());
                    if (item != null) {
                        u = userRepo.findOne(u.getId());
                        item = itemRepo.findOne(item.getId());
                        WagonLevel wl = wagonLevelRepo.findOne(u.getWagonLevel().getId());

                        u.getItems().add(item);
                        wl.removeItemById(item.getId());
                        item.setWagonLevel(null);
                        item.setUser(u);

                        userRepo.save(u);
                        itemRepo.save(item);
                        wagonLevelRepo.save(wl);
                    }
                }
            }
        }

        private void execute(PivotablePoleCard ppc, Long gameId) {
            Game game = gameRepo.findOne(gameId);
            // Get top wagonlevel of caboose
            WagonLevel caboose = wagonLevelRepo.findOne(game.getWagons().get(game.getWagons().size() - 1).getTopLevel().getId());
            for (User u : game.getUsers()) {
                if (u.getWagonLevel().getLevelType() == LevelType.TOP) {
                    u = userRepo.findOne(u.getId());
                    u.setWagonLevel(caboose);
                    WagonLevel wl = wagonLevelRepo.findOne(u.getWagonLevel().getId());
                    wl.removeUserById(u.getId());
                    caboose.getUsers().add(u);

                    userRepo.save(u);
                    wagonLevelRepo.save(wl);
                }
            }
            wagonLevelRepo.save(caboose);
        }

        // Helper Functions

        private Item getMinPurse(User user) {
            Item min = new Item();
            min.setValue(Integer.MAX_VALUE);
            for (Item item : user.getItems()) {
                if (item.getItemType() == ItemType.BAG && item.getValue() < min.getValue()) {
                    min = item;
                }
            }
            if (min.getValue() < Integer.MAX_VALUE) {
                return min;
            }
            return null;
        }

        private Item getRandomItem(ItemType type, WagonLevel wagonLevel) {
            if (type != ItemType.BAG) {
                for (int i = 0; i < wagonLevel.getItems().size(); i++) {
                    if (wagonLevel.getItems().get(i).getItemType() == type) {
                        return wagonLevel.getItems().get(i);
                    }
                }
            } else {
                List<Item> bags = new ArrayList<>();
                for (int i = 0; i < wagonLevel.getItems().size(); i++) {
                    if (wagonLevel.getItems().get(i).getItemType() == ItemType.BAG) {
                        bags.add(wagonLevel.getItems().get(i));
                    }
                }
                return !bags.isEmpty() ? bags.get((int) (Math.random() * bags.size())) : null;
            }
            return null;
        }
    }

    private class ActionRequestHelper {
        private ActionRequestDTO execute(ActionCard ac, Long gameId, Long userId) {
            if (ac instanceof CollectCard) {
                return generateCollectRequest(gameId, userId);
            }
            if (ac instanceof PunchCard) {
                return generatePunchRequest(gameId, userId);
            }
            if (ac instanceof MoveCard) {
                return generateMoveRequest(gameId, userId);
            }
            if (ac instanceof ShootCard) {
                return generateShootRequest(gameId, userId);
            }
            if (ac instanceof MarshalCard) {
                return generateMoveMarshalRequest(gameId, userId);
            }
            if (ac instanceof ChangeLevelCard){
                User user = userRepo.findOne(userId);
                ars.changeLevel(user);
                return null;
            }
            return null;

        }

        public ShootRequestDTO generateShootRequest(Long gameId, Long userId) {
            Game game = gameRepo.findOne(gameId);
            User user = userRepo.findOne(userId);
            ShootRequestDTO srq = new ShootRequestDTO();
            List<User> userList = new ArrayList<User>();
            srq.setShootableUserIds(new ArrayList<Long>());
            if (user.getWagonLevel().getLevelType() == LevelType.TOP) {
                getShootableUsersBeforeR(user, userList, user.getWagonLevel());
                getShootableUsersAfterR(user, userList, user.getWagonLevel());
            }
            if (user.getWagonLevel().getLevelType() == LevelType.BOTTOM) {
                getShootableUsersBeforeB(user, userList, user.getWagonLevel());
                getShootableUsersAfterB(user, userList, user.getWagonLevel());
            }
            if (userList.size() >= 2) {
                for (int i = 0; i < userList.size(); i++) {
                    if (userList.get(i).getCharacter() instanceof Belle) {
                        userList.remove(i);
                    }
                }
            }
            for (int i = 0; i < userList.size(); i++) {
                srq.getShootableUserIds().add(userList.get(i).getId());
            }

            if(srq.getShootableUserIds().isEmpty()){
                return null;
            }
            srq.setSpielId(game.getId());
            srq.setUserId(user.getId());
            srq = actionRepo.save(srq);

            game.getActions().add(srq);
            actionRepo.save(srq);
            userRepo.save(user);
            gameRepo.save(game);
            return srq;
        }

        public void getShootableUsersBeforeR(User user, List<User> shootable, WagonLevel wagonLevel) {
            if (wagonLevel.getWagonLevelBefore() != null) {
                int size = wagonLevel.getWagonLevelBefore().getUsers().size();
                for (int i = 0; i < size; i++) {
                    shootable.add(wagonLevel.getWagonLevelBefore().getUsers().get(i));
                }
                if (shootable.isEmpty()) {
                    getShootableUsersBeforeR(user, shootable, wagonLevel.getWagonLevelBefore());
                }
                if (user.getCharacter() instanceof Django) {
                    getShootableUsersBeforeR(user, shootable, wagonLevel.getWagonLevelBefore());
                }
            }
        }

        public void getShootableUsersAfterR(User user, List<User> shootable, WagonLevel wagonLevel) {
            if (wagonLevel.getWagonLevelAfter() != null) {
                int size = wagonLevel.getWagonLevelAfter().getUsers().size();
                for (int i = 0; i < size; i++) {
                    shootable.add(wagonLevel.getWagonLevelAfter().getUsers().get(i));
                }
                if (shootable.isEmpty()) {
                    getShootableUsersAfterR(user, shootable, wagonLevel.getWagonLevelAfter());

                }
                if (user.getCharacter() instanceof Django) {
                    getShootableUsersAfterR(user, shootable, wagonLevel.getWagonLevelAfter());
                }
            }
        }

        public void getShootableUsersBeforeB(User user, List<User> shootable, WagonLevel wagonLevel) {
            if (wagonLevel.getWagonLevelBefore() != null) {
                int size = wagonLevel.getWagonLevelBefore().getUsers().size();
                for (int i = 0; i < size; i++) {
                    shootable.add(wagonLevel.getWagonLevelBefore().getUsers().get(i));
                }
                if (user.getCharacter() instanceof Django && shootable.isEmpty()) {
                    getShootableUsersBeforeB(user, shootable, wagonLevel.getWagonLevelBefore());
                }
            }

        }

        public void getShootableUsersAfterB(User user, List<User> shootable, WagonLevel wagonLevel) {
            if (wagonLevel.getWagonLevelAfter() != null) {
                int size = wagonLevel.getWagonLevelAfter().getUsers().size();
                for (int i = 0; i < size; i++) {
                    shootable.add(wagonLevel.getWagonLevelAfter().getUsers().get(i));
                }
                if (shootable.isEmpty() && user.getCharacter() instanceof Django) {
                    getShootableUsersAfterB(user, shootable, wagonLevel.getWagonLevelAfter());
                }
            }
        }

        //region collectRequest
        public CollectItemRequestDTO generateCollectRequest(Long gameId, Long userId) {
            System.out.println("collectRequest");
            Game game = gameRepo.findOne(gameId);
            User user = userRepo.findOne(userId);
            CollectItemRequestDTO crq = new CollectItemRequestDTO();
            crq.setHasBag(Boolean.FALSE);
            crq.setHasCase(Boolean.FALSE);
            crq.setHasGem(Boolean.FALSE);
            if (!user.getWagonLevel().getItems().isEmpty()) {
                for (int i = 0; i < user.getWagonLevel().getItems().size(); i++) {
                    if (user.getWagonLevel().getItems().get(i).getItemType() == ItemType.GEM) {
                        crq.setHasGem(Boolean.TRUE);
                    }
                    if (user.getWagonLevel().getItems().get(i).getItemType() == ItemType.BAG) {
                        crq.setHasBag(Boolean.TRUE);
                    }
                    if (user.getWagonLevel().getItems().get(i).getItemType() == ItemType.CASE) {
                        crq.setHasCase(Boolean.TRUE);
                    }
                }
            }

            if(!crq.getHasBag() && !crq.getHasCase() && !crq.getHasGem()){
                return null;
            }
            crq.setSpielId(game.getId());
            crq.setUserId(user.getId());
            crq.setGame(game);
            crq = actionRepo.save(crq);

            game.getActions().add(crq);
            userRepo.save(user);
            gameRepo.save(game);
            return crq;
        }

        //endregion collectrequest
        public MoveRequestDTO generateMoveRequest(Long gameId, Long userId) {
            System.out.println("moveRequest");
            User user = userRepo.findOne(userId);
            Game game = gameRepo.findOne(gameId);
            MoveRequestDTO mrq = new MoveRequestDTO();
            List<Long> movable = new ArrayList<Long>();
            mrq.setMovableWagonsLvlIds(new ArrayList<>());

            if (user.getWagonLevel().getLevelType() == LevelType.TOP) {
                getMovableBeforeR(user, movable, user.getWagonLevel());
                getMovableAfterR(user, movable, user.getWagonLevel());

                if (movable.size() > 3) {
                    for (int i = 0; i < 3; i++) {
                        mrq.getMovableWagonsLvlIds().add(movable.get(i));
                    }
                } else {
                    for (int i = 0; i < movable.size(); i++) {
                        mrq.getMovableWagonsLvlIds().add(movable.get(i));
                    }
                }
            }

            if (user.getWagonLevel().getLevelType() == LevelType.BOTTOM) {
                if (user.getWagonLevel().getWagonLevelBefore() != null) {
                    mrq.getMovableWagonsLvlIds().add(user.getWagonLevel().getWagonLevelBefore().getId());
                }
                if (user.getWagonLevel().getWagonLevelAfter() != null) {
                    mrq.getMovableWagonsLvlIds().add(user.getWagonLevel().getWagonLevelAfter().getId());
                }
            }

            if(mrq.getMovableWagonsLvlIds().isEmpty()){
                return null;
            }

            mrq.setSpielId(game.getId());
            mrq.setUserId(user.getId());
            mrq = actionRepo.save(mrq);
            game.getActions().add(mrq);
            userRepo.save(user);
            gameRepo.save(game);

            return mrq;
        }

        public void getMovableBeforeR(User user, List<Long> movable, WagonLevel wagonLevel) {
            if (wagonLevel.getWagonLevelBefore() != null) {
                movable.add(wagonLevel.getWagonLevelBefore().getId());
                getMovableBeforeR(user, movable, wagonLevel.getWagonLevelBefore());
            }
        }

        public void getMovableAfterR(User user, List<Long> movable, WagonLevel wagonLevel) {

            if (wagonLevel.getWagonLevelBefore() != null) {
                movable.add(wagonLevel.getWagonLevelBefore().getId());
                getMovableAfterR(user, movable, wagonLevel.getWagonLevelBefore());
            }
        }

        public void getMovableBeforeB(User user, List<Long> movable, WagonLevel wagonLevel) {
            if (wagonLevel.getWagonLevelBefore() != null) {
                movable.add(wagonLevel.getWagonLevelBefore().getId());
                getMovableBeforeB(user, movable, wagonLevel.getWagonLevelBefore());
            }
        }

        public void getMovableAfterB(User user, List<Long> movable, WagonLevel wagonLevel) {
            if (wagonLevel.getWagonLevelBefore() != null) {
                movable.add(wagonLevel.getWagonLevelBefore().getId());
                getMovableAfterB(user, movable, wagonLevel.getWagonLevelBefore());
            }

        }

        public PunchRequestDTO generatePunchRequest(Long gameId, Long userId) {
            System.out.println("punchRequest");
            User user = userRepo.findOne(userId);
            Game game = gameRepo.findOne(gameId);

            PunchRequestDTO prq = new PunchRequestDTO();
            List<User> userList = new ArrayList<User>();
            prq.setPunchableUserIds(new ArrayList<Long>());

            for (User u: user.getWagonLevel().getUsers()) {
                if(!u.getId().equals(user.getId())){
                    userList.add(u);
                }
            }
            if (userList.size() > 1) {
                for (int i = 0; i < userList.size(); i++) {
                    if (userList.get(i).getId() == user.getId()) {
                        userList.remove(i);
                    }
                }
            }
            if (userList.size() >= 2) {
                for (int i = 0; i < userList.size(); i++) {
                    if (userList.get(i).getCharacter() instanceof Belle) {
                        userList.remove(i);
                    }
                }
            }
            for (int i = 0; i < userList.size(); i++) {
                prq.getHasBag().add(i, Boolean.FALSE);
                prq.getHasCase().add(i, Boolean.FALSE);
                prq.getHasGem().add(i, Boolean.FALSE);
                prq.getPunchableUserIds().add(userList.get(i).getId());

                if (!userList.get(i).getItems().isEmpty()) {
                    for (int j = 0; j < userList.get(i).getItems().size(); j++) {
                        switch(userList.get(i).getItems().get(j).getItemType()){
                            case GEM:
                                prq.getHasGem().set(i, Boolean.TRUE);
                                break;
                            case BAG:
                                prq.getHasBag().set(i, Boolean.TRUE);
                                break;
                            case CASE:
                                prq.getHasCase().set(i, Boolean.TRUE);
                                break;

                        }
                    }
                }
            }
            if (user.getWagonLevel().getWagonLevelBefore() != null) {
                prq.getMovable().add(user.getWagonLevel().getWagonLevelBefore().getId());
            }
            if (user.getWagonLevel().getWagonLevelAfter() != null) {
                prq.getMovable().add(user.getWagonLevel().getWagonLevelAfter().getId());
            }

            if(prq.getPunchableUserIds().isEmpty()){
                return null;
            }

            prq.setSpielId(game.getId());
            prq.setUserId(user.getId());
            prq = actionRepo.save(prq);
            game.getActions().add(prq);
            userRepo.save(user);
            gameRepo.save(game);
            return prq;
        }

        public MoveMarshalRequestDTO generateMoveMarshalRequest(Long gameId, Long userId) {
            System.out.println("moveMarshalRequest");
            Game game = gameRepo.findOne(gameId);
            MoveMarshalRequestDTO mmrq = new MoveMarshalRequestDTO();

            if (game.getMarshal().getWagonLevel().getWagonLevelBefore() != null) {
                mmrq.getMovableWagonsLvlIds().add(game.getMarshal().getWagonLevel().getWagonLevelBefore().getId());
            }
            if (game.getMarshal().getWagonLevel().getWagonLevelAfter() != null) {
                mmrq.getMovableWagonsLvlIds().add(game.getMarshal().getWagonLevel().getWagonLevelAfter().getId());
            }

            mmrq.setSpielId(game.getId());
            mmrq.setUserId(userId);
            mmrq = actionRepo.save(mmrq);
            game.getActions().add(mmrq);
            gameRepo.save(game);
            return mmrq;
        }


    }

}


