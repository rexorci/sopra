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
import ch.uzh.ifi.seal.soprafs16.model.cards.Card;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.AngryMarshalCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.BlankBridgeCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.BlankTunnelCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.BrakingCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.GetItAllCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.HostageCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.MarshallsRevengeCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.PassengerRebellionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.PickPocketingCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.PivotablePoleCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.RoundCard;
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
    //endregion

    public void update(Long id) {
        if (!gameRepo.exists(id)) return;

        Game game = gameRepo.findOne(id);

        Hibernate.initialize(game.getUsers());
        int currentPlayer = game.getCurrentPlayer();
        List<User> users = game.getUsers();

        if (game.getCurrentPhase() == PhaseType.PLANNING) {
            processPlayerTurn(game, currentPlayer, users.size());

            setNextTurn(game, game.getUsers().size());
            if (game.getCurrentTurn() < getCurrentTurns(game).size()) {
                setNextPlayer(game, game.getUsers().size());
            } else {
                game.setCurrentPhase(PhaseType.EXECUTION);
                game.setActionRequestCounter(0);
            }
        } else if (game.getCurrentPhase() == PhaseType.EXECUTION) {
            game.getCommonDeck().remove(0);
            if (game.getCommonDeck().size() == 0) {
                // Next round is triggered
                game.setCurrentRound(game.getCurrentRound() + 1);
                //executeRoundAction(game);
                //resetPlayerDecks(game);
                if (game.getCurrentRound().equals(GameConstants.ROUNDS)) {
                    finishGame(game);
                } else {
                    game.setRoundStarter((game.getRoundStarter() + 1) % game.getUsers().size());
                    game.setCurrentPlayer(game.getRoundStarter());
                    game.setCurrentTurn(0);
                    game.setCurrentPhase(PhaseType.PLANNING);
                }
            }
        }

        gameRepo.save(game);
    }

    private void executeRoundAction(Game game) {
        RoundCard rc = (RoundCard) cardRepo.findOne(game.getRoundCardDeck().get(game.getCurrentRound()).getId());
        RoundEndActionHelper reaHelper = new RoundEndActionHelper();
        //reaHelper.execute(rc, game.getId());
    }

    private void processPlayerTurn(Game game, int currentPlayer, int playerCounter) {
        createDOPCRequestDTO(game);
        game.setActionRequestCounter(game.getActionRequestCounter() + 1);
    }

    private void setNextTurn(Game game, int playerCounter) {
        // Turn end
        if ((!(game.getCurrentTurnType() instanceof SpeedupTurn) && game.getActionRequestCounter() % playerCounter == 0)
                || (game.getCurrentTurnType() instanceof SpeedupTurn && game.getActionRequestCounter() == (game.getCurrentTurn() + 2) * playerCounter)) {
            game.setCurrentTurn(game.getCurrentTurn() + 1);
            if (game.getCurrentTurn() < ((RoundCard) (game.getRoundCardDeck().get(
                    (game.getCurrentRound())))).getPattern().size()
                    && game.getCurrentTurnType() instanceof ReverseTurn) {
                game.setCurrentPlayer(game.getRoundStarter() + 1); // correction
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

    private void resetPlayerDecks(Game game) {
        Hibernate.initialize(game.getUsers());
        List<User> users = game.getUsers();
        for (User u : users) {
            u = userRepo.findOne(u.getId());
            PlayerDeck<HandCard> handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(u.getHandDeck().getId());
            PlayerDeck<HandCard> hiddenDeck = u.getHiddenDeck();

            while (handDeck.size() > 0) {
                HandCard hc = (HandCard) handDeck.remove(0);
                hc.setDeck(hiddenDeck);
                hiddenDeck.add(hc);

                cardRepo.save(hc);
            }

            for (int i = 0; i < 6; i++) {
                ActionCard ac = (ActionCard) hiddenDeck.remove((int) (Math.random() * hiddenDeck.size()));
                ac.setDeck(handDeck);
                handDeck.add(ac);

                cardRepo.save(ac);
            }

            // Character Skill Doc
            if (u.getCharacterType().equals("Doc")) {
                ActionCard ac = (ActionCard) hiddenDeck.remove((int) (Math.random() * hiddenDeck.size()));
                ac.setDeck(handDeck);
                handDeck.add(ac);

                cardRepo.save(ac);
            }
            deckRepo.save(handDeck);
            deckRepo.save(hiddenDeck);
        }
    }

    private void createDOPCRequestDTO(Game game) {
        // TODO
        Card c = new Card();
        c.setDeck(game.getCommonDeck());
        cardRepo.save(c);
        game.getCommonDeck().add(c);
        deckRepo.save(game.getCommonDeck());
    }

    private int mod(int a, int b) {
        int ret = a % b;
        return ret < 0 ? b + ret : ret;
    }

    private List<Turn> getCurrentTurns(Game game) {
        RoundCard rc = (RoundCard) game.getRoundCardDeck().get(game.getCurrentRound());
        ArrayList<Turn> turns = rc.getArrayList();
        return turns;
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
            for(User u: game.getUsers()){
                if(u.getWagonLevel().getLevelType() == LevelType.TOP){
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
                return bags.size() > 0 ? bags.get((int) (Math.random() * bags.size())) : null;
            }
            return null;
        }
    }


}