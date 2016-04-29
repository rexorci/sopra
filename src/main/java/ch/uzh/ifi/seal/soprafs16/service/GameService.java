package ch.uzh.ifi.seal.soprafs16.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.IntStream;

import ch.uzh.ifi.seal.soprafs16.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.constant.PhaseType;
import ch.uzh.ifi.seal.soprafs16.constant.SourceType;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.Item;
import ch.uzh.ifi.seal.soprafs16.model.Marshal;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.Wagon;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.cards.Card;
import ch.uzh.ifi.seal.soprafs16.model.cards.Deck;
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
import ch.uzh.ifi.seal.soprafs16.model.characters.Doc;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CharacterRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.TurnRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonRepository;
import ch.uzh.ifi.seal.soprafs16.model.turns.NormalTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.ReverseTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.SpeedupTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.TunnelTurn;
import ch.uzh.ifi.seal.soprafs16.model.turns.Turn;

/**
 * Created by Christoph on 06/04/16.
 */


@Service("gameService")
public class GameService {


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
    private TurnRepository turnRepo;
    //endregion

    /**
     * @param gameId
     * @return
     */
    public Long startGame(Long gameId) {
        try {
            Game game = gameRepo.findOne(gameId);

            boolean initSuccessful = true;

            game.setStatus(GameStatus.RUNNING);
            game.setWagons(new ArrayList<Wagon>());

            initSuccessful = initGameWagons(game) && initSuccessful;
            initSuccessful = initPlayerDecks(game) && initSuccessful;
            initSuccessful = initGameDecks(game) && initSuccessful;
            initSuccessful = initGameFigurines(game.getId()) && initSuccessful;
            initSuccessful = initGameItems(game.getId()) && initSuccessful;

            //set Game status variables
            game.setCurrentRound(0);
            game.setCurrentTurn(0);
            game.setCurrentPhase(PhaseType.PLANNING);
            game.setRoundStarter(game.getCurrentPlayer());
            game.setActionRequestCounter(0);
            game.setRoundPattern(((RoundCard) (game.getRoundCardDeck().getCards().get(0))).getStringPattern());

            gameRepo.save(game);

            if (initSuccessful) {
                return game.getId();
            } else {
                return (long) -1;
            }
        } catch (Exception ex) {
            return (long) -1;
        }
    }

    private boolean initGameWagons(Game game) {
        try {
            int maxWagons; //Locomotive is also a wagon, so 4 Wagons means 3 carriages and 1 locomotive
            if (game.getUsers().size() <= 3) {
                maxWagons = 4;
            } else {
                maxWagons = game.getUsers().size() + 1;
            }

            for (int i = 0; i < maxWagons; i++) {
                Wagon wagon = new Wagon();
                wagon.setGame(game);
                game.getWagons().add(wagon);
                wagonRepo.save(wagon);

                WagonLevel topLevel = new WagonLevel();
                topLevel.setLevelType(LevelType.TOP);
                topLevel.setItems(new ArrayList<Item>());
                wagon.setTopLevel(topLevel);
                topLevel.setWagon(wagon);
                topLevel.setUsers(new ArrayList<User>());
                wagonLevelRepo.save(topLevel);

                WagonLevel botLevel = new WagonLevel();
                botLevel.setLevelType(LevelType.BOTTOM);
                botLevel.setItems(new ArrayList<Item>());
                wagon.setBottomLevel(botLevel);
                botLevel.setWagon(wagon);
                botLevel.setUsers(new ArrayList<User>());
                wagonLevelRepo.save(botLevel);
            }

            int counter = 0;
            for (Wagon w : game.getWagons()) {
                Wagon thisWagon = game.getWagons().get(counter);
                if (counter != 0) {
                    Wagon wagonBefore = game.getWagons().get(counter - 1);
                    thisWagon.getTopLevel().setWagonLevelBefore(wagonBefore.getTopLevel());
                    thisWagon.getBottomLevel().setWagonLevelBefore(wagonBefore.getBottomLevel());
                }
                if (counter != maxWagons - 1) {
                    Wagon wagonAfter = game.getWagons().get(counter + 1);
                    thisWagon.getTopLevel().setWagonLevelAfter(wagonAfter.getTopLevel());
                    thisWagon.getBottomLevel().setWagonLevelAfter(wagonAfter.getBottomLevel());
                }
                wagonRepo.save(thisWagon);
                counter++;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean initPlayerDecks(Game game) {
        try {
            for (User user : game.getUsers()) {

                //give personal Bulletcards to each player
                PlayerDeck<BulletCard> bulletsDeck = new PlayerDeck<BulletCard>();

                bulletsDeck.setUser(user);
                user.setBulletsDeck(bulletsDeck);
                deckRepo.save(bulletsDeck);
                userRepo.save(user);
                if (!user.getCharacter().equals(null)) {
                    for (int i = 0; i < 6; i++) {
                        BulletCard bulletCard = new BulletCard();
                        bulletCard.setBulletCounter(i + 1);
                        SourceType st = SourceType.valueOf(user.getCharacter().getClass().getSimpleName().toUpperCase());
                        bulletCard.setSourceType(st);
                        bulletsDeck.getCards().add(bulletCard);
                        bulletCard.setDeck(bulletsDeck);
                        cardRepo.save(bulletCard);
                    }
                } else {
                    return false;
                }

                //give handcards to each player (6 for each, except doc gets 7)
                PlayerDeck<HandCard> handDeck = new PlayerDeck<>();
                handDeck.setUser(user);
                user.setHandDeck(handDeck);
                deckRepo.save(handDeck);
                userRepo.save(user);

                ArrayList<ActionCard> allActionCards = getActionCards(user);

                int drawCardsAmount = user.getCharacter().getClass().equals(Doc.class) ? 7 : 6;

                final int[] randomChosenHandCards = new Random().ints(0, 10).distinct().limit(drawCardsAmount).toArray();
                for (int randomIndex : randomChosenHandCards) {
                    handDeck.getCards().add(allActionCards.get(randomIndex));
                    allActionCards.get(randomIndex).setDeck(handDeck);
                    allActionCards.get(randomIndex).setPlayedByUserId(user.getId());
                }

                //put the rest of the player's actioncards into his hiddendeck
                PlayerDeck<HandCard> hiddenDeck = new PlayerDeck<>();
                hiddenDeck.setUser(user);
                user.setHiddenDeck(hiddenDeck);
                deckRepo.save(hiddenDeck);
                userRepo.save(user);

                for (int i = 0; i < 10; i++) {
                    final int finalI = i;
                    if (!IntStream.of(randomChosenHandCards).anyMatch(x -> x == finalI)) {
                        hiddenDeck.getCards().add(allActionCards.get(i));
                        allActionCards.get(i).setDeck(hiddenDeck);
                        allActionCards.get(i).setPlayedByUserId(user.getId());
                    }
                }
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean initGameDecks(Game game) {
        try {
            //region RoundcardDeck
            //set up a list of possible roundcards (without stationcards) to randomly choose from
            ArrayList<RoundCard> possibleRoundCards = setPatternOnRoundCards();
            //choose 4 Random Roundcards
            final int[] randomChosenRoundCards = new Random().ints(0, 7).distinct().limit(4).toArray();
            GameDeck<RoundCard> roundCardDeck = new GameDeck<>();
            roundCardDeck.setGame(game);
            game.setRoundCardDeck(roundCardDeck);
            deckRepo.save(roundCardDeck);
            gameRepo.save(game);

            for (int randomIndex : randomChosenRoundCards) {
                roundCardDeck.getCards().add(possibleRoundCards.get(randomIndex));
                possibleRoundCards.get(randomIndex).setDeck(roundCardDeck);
            }

            //add a stationcard
            setPatternOnStationCards();
            Random rn = new Random();
            int stationCardId = rn.nextInt(3);

            RoundCard stationCard = setPatternOnStationCards().get(stationCardId);
            roundCardDeck.getCards().add(stationCard);
            stationCard.setDeck(roundCardDeck);
            //endregion
            //region neutralBulletsDeck
            GameDeck<BulletCard> neutralBulletsDeck = new GameDeck<BulletCard>();
            neutralBulletsDeck.setGame(game);
            game.setNeutralBulletsDeck(neutralBulletsDeck);
            deckRepo.save(neutralBulletsDeck);
            gameRepo.save(game);
            for (int i = 0; i < 13; i++) {
                BulletCard bulletCard = new BulletCard();
                bulletCard.setBulletCounter(i + 1);
                bulletCard.setSourceType(SourceType.MARSHAL);
                neutralBulletsDeck.getCards().add(bulletCard);
                bulletCard.setDeck(neutralBulletsDeck);
                cardRepo.save(bulletCard);
            }
            //endregion
            //region commonDeck
            GameDeck<ActionCard> commonDeck = new GameDeck<ActionCard>();
            commonDeck.setGame(game);
            game.setCommonDeck(commonDeck);
            deckRepo.save(commonDeck);
            gameRepo.save(game);
            //endregion
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private ArrayList<ActionCard> getActionCards(User user) {
        try {
            ArrayList<ActionCard> actionCards = new ArrayList<>();
            //10ActionCards in Total, 2x Move, 2x ChangeLevel, 1x Punch, 1x MoveMarshal, 2x Shoot, 2x Collect
            actionCards.add(new MoveCard());
            actionCards.add(new MoveCard());
            actionCards.add(new ChangeLevelCard());
            actionCards.add(new ChangeLevelCard());
            actionCards.add(new PunchCard());
            actionCards.add(new MarshalCard());
            actionCards.add(new ShootCard());
            actionCards.add(new ShootCard());

            actionCards.add(new CollectCard());
            actionCards.add(new CollectCard());
            for (ActionCard c : actionCards) {
                cardRepo.save(c);
            }

            return actionCards;
        } catch (Exception ex) {
            return null;
        }
    }

    private ArrayList<RoundCard> setPatternOnRoundCards() {
        try {
            ArrayList<RoundCard> possibleRoundCards = new ArrayList<>();

            //AngryMarshalCard
            AngryMarshalCard angryMarshalCard = (AngryMarshalCard) createTurnPattern(new AngryMarshalCard());
            possibleRoundCards.add(angryMarshalCard);
            //PivotablePoleCard
            PivotablePoleCard pivotablePoleCard = (PivotablePoleCard) createTurnPattern(new PivotablePoleCard());
            possibleRoundCards.add(pivotablePoleCard);
            //BrakingCard
            BrakingCard brakingCard = (BrakingCard) createTurnPattern(new BrakingCard());
            possibleRoundCards.add(brakingCard);
            //GetItAllCard
            GetItAllCard getItAllCard = (GetItAllCard) createTurnPattern(new GetItAllCard());
            possibleRoundCards.add(getItAllCard);
            //PassengerRebellionCard
            PassengerRebellionCard passengerRebellionCard = (PassengerRebellionCard) createTurnPattern(new PassengerRebellionCard());
            possibleRoundCards.add(passengerRebellionCard);
            //BlankTunnelCard
            BlankTunnelCard blankTunnelCard = (BlankTunnelCard) createTurnPattern(new BlankTunnelCard());
            possibleRoundCards.add(blankTunnelCard);
            //BlankBridgeCard
            BlankBridgeCard blankBridgeCard = (BlankBridgeCard) createTurnPattern(new BlankBridgeCard());
            possibleRoundCards.add(blankBridgeCard);

            return possibleRoundCards;
        } catch (Exception ex) {
            return null;
        }
    }

    private ArrayList<RoundCard> setPatternOnStationCards() {
        try {
            ArrayList<RoundCard> possibleStationCards = new ArrayList<>();

            //PickPocketingCard
            PickPocketingCard pickPocketingCard = (PickPocketingCard) createTurnPattern(new PickPocketingCard());
            possibleStationCards.add(pickPocketingCard);
            //MarshallsRevengeCard
            MarshallsRevengeCard marshallsRevengeCard = (MarshallsRevengeCard) createTurnPattern(new MarshallsRevengeCard());
            possibleStationCards.add(marshallsRevengeCard);
            //HostageCard
            HostageCard hostageCard = (HostageCard) createTurnPattern(new HostageCard());
            possibleStationCards.add(hostageCard);

            return possibleStationCards;
        } catch (Exception ex) {
            return null;
        }
    }

    private RoundCard createTurnPattern(RoundCard roundCard) {
        try {
            ArrayList<Turn> pattern = new ArrayList<>();
            roundCard.setPattern(pattern);
            cardRepo.save(roundCard);
            for (char c : roundCard.getStringPattern().toCharArray()) {
                switch (c) {
                    case 'N':
                        NormalTurn normalTurn = new NormalTurn();
                        normalTurn.setRoundCard(roundCard);
                        pattern.add(normalTurn);
                        turnRepo.save(normalTurn);
                        break;
                    case 'T':
                        TunnelTurn tunnelTurn = new TunnelTurn();
                        tunnelTurn.setRoundCard(roundCard);
                        pattern.add(tunnelTurn);
                        turnRepo.save(tunnelTurn);
                        break;
                    case 'R':
                        ReverseTurn reverseTurn = new ReverseTurn();
                        reverseTurn.setRoundCard(roundCard);
                        pattern.add(reverseTurn);
                        turnRepo.save(reverseTurn);
                        break;
                    case 'S':
                        SpeedupTurn speedupTurn = new SpeedupTurn();
                        speedupTurn.setRoundCard(roundCard);
                        pattern.add(speedupTurn);
                        turnRepo.save(speedupTurn);
                        break;
                    default:
                        return null;
                }
            }

            return roundCard;
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean initGameFigurines(long gameId) {
        try {
            Game game = gameRepo.findOne(gameId);
            //set Random 1st Player
            Random rn = new Random();
            int firstPlayer = rn.nextInt(game.getUsers().size());
            game.setCurrentPlayer(firstPlayer);

            //place odd-number-players in last waggon, even-number-players in second-last
            WagonLevel lastWagonLevelBot = game.getWagons().get(game.getWagons().size() - 1).getBottomLevel();
            WagonLevel secondLastWagonLevelBot = lastWagonLevelBot.getWagonLevelBefore();

            boolean isEven = false;
            int userCount = game.getUsers().size();
            for (int i = 0; i < userCount; i++) {
                int userIndex = (firstPlayer + i) % userCount;
                User user = game.getUsers().get(userIndex);
                if (isEven) {
                    secondLastWagonLevelBot.getUsers().add(user);
                    user.setWagonLevel(secondLastWagonLevelBot);
                    isEven = false;
                } else {
                    lastWagonLevelBot.getUsers().add(user);
                    user.setWagonLevel(lastWagonLevelBot);
                    isEven = true;
                }

            }

            //place Marshal
            Marshal marshal = new Marshal();
            marshal.setGame(game);
            game.setMarshal(marshal);
            game.getWagons().get(0).getBottomLevel().setMarshal(marshal);
            marshal.setWagonLevel(game.getWagons().get(0).getBottomLevel());
            marshalRepo.save(marshal);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean initGameItems(long gameId) {
        try {
            //give 250$ bag to every user
            Game game = gameRepo.findOne(gameId);
            for (User user : game.getUsers()) {
                Item bag = new Item();
                bag.setValue(250);
                bag.setItemType(ItemType.BAG);
                user.getItems().add(bag);
                bag.setUser(user);
                itemRepo.save(bag);
                userRepo.save(user);
            }

            //put
            WagonLevel locomotiveBot = game.getWagons().get(0).getBottomLevel();
            Item moneyCase = new Item();
            moneyCase.setValue(1000);
            moneyCase.setItemType(ItemType.CASE);
            moneyCase.setWagonLevel(locomotiveBot);
            locomotiveBot.getItems().add(moneyCase);
            itemRepo.save(moneyCase);

            //put all the possible wagontypes in the wagonTypes list, the tuples are <#Gems,#Bags>
            ArrayList<AbstractMap.SimpleImmutableEntry<Integer, Integer>> wagonTypes = new ArrayList<>();
            wagonTypes.add(new AbstractMap.SimpleImmutableEntry<Integer, Integer>(3, 0));
            wagonTypes.add(new AbstractMap.SimpleImmutableEntry<Integer, Integer>(1, 1));
            wagonTypes.add(new AbstractMap.SimpleImmutableEntry<Integer, Integer>(0, 1));
            wagonTypes.add(new AbstractMap.SimpleImmutableEntry<Integer, Integer>(1, 3));
            wagonTypes.add(new AbstractMap.SimpleImmutableEntry<Integer, Integer>(1, 4));
            int carriageCount = game.getWagons().size() - 1;
            //randomly select which wagonTypes will be used in this game (if less than 5 players play, not all wagons are taken)
            final int[] randomChosenWagonTypes = new Random().ints(0, 5).distinct().limit(carriageCount).toArray();

            //put all the possible bag-types in the bagTypes list, values from 300$ to 500$ appear twice, 250$ appear 8times, but at the start every player already gets 1 250$ bag
            ArrayList<Integer> bagTypes = new ArrayList<>();
            for (int i = 0; i < 8 - game.getUsers().size(); i++) {
                bagTypes.add(250);
            }
            for (int i = 0; i < 5; i++) {
                bagTypes.add(300 + i * 50);
                bagTypes.add(300 + i * 50);
            }

            int bagsToDistribute = 0;
            for (int i : randomChosenWagonTypes) {
                bagsToDistribute += wagonTypes.get(i).getValue();
            }

            //randomly choose the bags that will be distributed on the wagons out of the pool of possible bags
            final int[] randomChosenItemTypes = new Random().ints(0, 18 - game.getUsers().size()).distinct().limit(bagsToDistribute).toArray();

            //put the diamond/bag combinations into the wagons (bottomLevel)
            int wagonTypeCounter = 0;
            int bagTypeCounter = 0;
            for (Wagon w : game.getWagons().subList(1, game.getWagons().size())) {
                int gems = wagonTypes.get(randomChosenWagonTypes[wagonTypeCounter]).getKey();
                int bags = wagonTypes.get(randomChosenWagonTypes[wagonTypeCounter]).getValue();
                WagonLevel botLevel = w.getBottomLevel();
                for (int d = 0; d < gems; d++) {
                    Item gem = new Item();
                    gem.setItemType(ItemType.GEM);
                    gem.setValue(500);
                    gem.setWagonLevel(botLevel);
                    botLevel.getItems().add(gem);
                    itemRepo.save(gem);
                }
                for (int b = 0; b < bags; b++) {
                    Item bag = new Item();
                    bag.setItemType(ItemType.BAG);
                    bag.setValue(bagTypes.get(randomChosenItemTypes[bagTypeCounter++]));
                    bag.setWagonLevel(botLevel);
                    botLevel.getItems().add(bag);
                    itemRepo.save(bag);
                }
                wagonLevelRepo.save(botLevel);
                wagonTypeCounter++;
            }

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    //region user leaves game
    public boolean removeUser(User user, Game game) {
        try {
            boolean successful = true;
            user.setGame(null);
            game.getUsers().remove(user);
            if (user.getCharacter() != null) {
                ch.uzh.ifi.seal.soprafs16.model.characters.Character oldChar = user.getCharacter();
                user.setCharacter(null);
                oldChar.setUser(null);
                characterRepo.delete(oldChar);
            }
            if (user.getItems() != null) {
                int itemCounter = user.getItems().size();
                //with standard foreach loop exception is thrown -> user.getItems can be empty although we are already in the list
                for (int i = itemCounter - 1; i >= 0; i--) {
                    successful = deleteItem(user.getItems().get(i)) && successful;
                }
                userRepo.save(user);
                user.setItems(null);
                userRepo.save(user);
            }
            if (user.getWagonLevel() != null) {
                WagonLevel wagonLevel = user.getWagonLevel();
                wagonLevel.getUsers().remove(user);
                user.setWagonLevel(null);
                wagonLevelRepo.save(wagonLevel);
                userRepo.save(user);
            }
            if (user.getBulletsDeck() != null) {
                PlayerDeck<BulletCard> bulletCardDeck = user.getBulletsDeck();
                int cardCounter = bulletCardDeck.getCards().size();
                for (int i = cardCounter - 1; i >= 0; i--) {
                    successful = deleteCard((Card) bulletCardDeck.getCards().get(i));
                }
                bulletCardDeck.setCards(null);
                bulletCardDeck.setUser(null);
                user.setBulletsDeck(null);
                deckRepo.delete(bulletCardDeck);
                userRepo.save(user);
            }
            if (user.getHandDeck() != null) {
                PlayerDeck<HandCard> handCardDeck = user.getHandDeck();
                int cardCounter = handCardDeck.getCards().size();
                for (int i = cardCounter - 1; i >= 0; i--) {
                    successful = deleteCard((Card) handCardDeck.getCards().get(i));
                }
                handCardDeck.setCards(null);
                handCardDeck.setUser(null);
                user.setHandDeck(null);
                deckRepo.delete(handCardDeck);
                userRepo.save(user);
            }
            if (user.getHiddenDeck() != null) {
                PlayerDeck<HandCard> hiddenCardDeck = user.getHiddenDeck();
                int cardCounter = hiddenCardDeck.getCards().size();
                for (int i = cardCounter - 1; i >= 0; i--) {
                    successful = deleteCard((Card) hiddenCardDeck.getCards().get(i));
                }
                hiddenCardDeck.setCards(null);
                hiddenCardDeck.setUser(null);
                user.setHiddenDeck(null);
                deckRepo.delete(hiddenCardDeck);
                userRepo.save(user);
            }
            gameRepo.save(game);
            userRepo.save(user);

            return successful;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean deleteItem(Item item) {
        try {
            if (item.getUser() != null) {
                User user = item.getUser();
                user.getItems().remove(item);
                item.setUser(null);
                userRepo.save(user);
            }
            if (item.getWagonLevel() != null) {
                WagonLevel wagonLevel = item.getWagonLevel();
                wagonLevel.getItems().remove(item);
                item.setWagonLevel(null);
                wagonLevelRepo.save(wagonLevel);
            }
            itemRepo.delete(item);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean deleteCard(Card card) {
        try {
            Deck deck = card.getDeck();
            deck.getCards().remove(card);
            card.setDeck(null);
            cardRepo.delete(card);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean deleteGame(Game game) {
        try {
            boolean successful = true;
            if (game.getRoundCardDeck() != null) {
                GameDeck<RoundCard> roundCardDeck = game.getRoundCardDeck();
                int cardCounter = roundCardDeck.getCards().size();
                for (int i = cardCounter - 1; i >= 0; i--) {
                    int turnCounter = ((RoundCard) (roundCardDeck.getCards().get(i))).getPattern().size();
                    for (int tc = turnCounter - 1; tc >= 0; tc--) {
                        Turn turn = ((RoundCard) (roundCardDeck.getCards().get(i))).getPattern().get(tc);
                        ((RoundCard) (roundCardDeck.getCards().get(i))).getPattern().remove(turn);
                        turn.setRoundCard(null);
                        turnRepo.delete(turn);
                    }
                    ((RoundCard) (roundCardDeck.getCards().get(i))).setPattern(null);
                    cardRepo.save((Card) roundCardDeck.getCards().get(i));
                    successful = deleteCard((Card) roundCardDeck.getCards().get(i));
                }
                roundCardDeck.setCards(null);
                roundCardDeck.setGame(null);
                game.setRoundCardDeck(null);
                deckRepo.delete(roundCardDeck);
                gameRepo.save(game);
            }
            if (game.getNeutralBulletsDeck() != null) {
                GameDeck<BulletCard> bulletCardDeck = game.getNeutralBulletsDeck();
                int cardCounter = bulletCardDeck.getCards().size();
                for (int i = cardCounter - 1; i >= 0; i--) {
                    successful = deleteCard((Card) bulletCardDeck.getCards().get(i));
                }
                bulletCardDeck.setCards(null);
                bulletCardDeck.setGame(null);
                game.setNeutralBulletsDeck(null);
                deckRepo.delete(bulletCardDeck);
                gameRepo.save(game);
            }
            if (game.getCommonDeck() != null) {
                GameDeck<ActionCard> commonDeck = game.getCommonDeck();
                int cardCounter = commonDeck.getCards().size();
                for (int i = cardCounter - 1; i >= 0; i--) {
                    successful = deleteCard((Card) commonDeck.getCards().get(i));
                }
                commonDeck.setCards(null);
                commonDeck.setGame(null);
                game.setCommonDeck(null);
                deckRepo.delete(commonDeck);
                gameRepo.save(game);
            }
            if (game.getMarshal() != null) {
                Marshal marshal = game.getMarshal();
                marshal.setGame(null);
                game.setMarshal(null);
                marshalRepo.save(marshal);
                gameRepo.save(game);
            }
            if (game.getWagons() != null) {
                int wagonCounter = game.getWagons().size();
                for (int i = wagonCounter - 1; i >= 0; i--) {
                    Wagon wagon = game.getWagons().get(i);

                    WagonLevel wagonLevelTop = wagon.getTopLevel();
                    int itemCounterTop = wagonLevelTop.getItems().size();
                    for (int ict = itemCounterTop - 1; ict >= 0; ict--) {
                        successful = deleteItem(wagonLevelTop.getItems().get(ict)) && successful;
                    }
                    wagonLevelTop.setItems(null);
                    wagonLevelRepo.save(wagonLevelTop);

                    WagonLevel wagonLevelBot = wagon.getBottomLevel();
                    int itemCounterBot = wagonLevelBot.getItems().size();
                    for (int icb = itemCounterBot - 1; icb >= 0; icb--) {
                        successful = deleteItem(wagonLevelBot.getItems().get(icb)) && successful;
                    }
                    wagonLevelBot.setItems(null);
                    wagonLevelRepo.save(wagonLevelBot);

                    wagonLevelTop.setWagon(null);
                    wagon.setTopLevel(null);
                    wagonLevelRepo.save(wagonLevelTop);
                    wagonRepo.save(wagon);
                    wagonLevelBot.setWagon(null);
                    wagon.setBottomLevel(null);
                    wagonLevelRepo.save(wagonLevelBot);
                    wagonRepo.save(wagon);

                    wagon.setGame(null);
                    game.getWagons().remove(wagon);
                    wagonRepo.delete(wagon);
                }
                game.setWagons(null);
                gameRepo.save(game);
            }

            gameRepo.delete(game);
            return successful;
        } catch (Exception ex) {
            return false;
        }
    }
    //endregion

    //region createDemoGame Logic
    public Long createDemoGame(Long gameId) {
        try {
            Game game = gameRepo.findOne(gameId);
            //set Stationcard as Roundcard
            game.setCurrentRound(game.getRoundCardDeck().getCards().size() - 1);

            boolean creationSuccessful = true;
            //put bulletcards into decks
            creationSuccessful = creationSuccessful && placeBulletsDemo(game);

            //give some items to users
            creationSuccessful = creationSuccessful && giveItemsDemo(game);

            //place marshal in second wagon
            creationSuccessful = creationSuccessful && relocateMarshal(game, 1);

            //place 1 user in first wagon (after locomotive)
            creationSuccessful = creationSuccessful && relocatePlayer(game.getUsers().get(0), game.getWagons().get(0).getBottomLevel());

            //place 1 user in on top of 3rd wagon
            creationSuccessful = creationSuccessful && relocatePlayer(game.getUsers().get(1), game.getWagons().get(3).getTopLevel());

            if (creationSuccessful) {
                return gameId;
            } else {
                return (long) -1;
            }
        } catch (Exception ex) {
            return (long) -1;
        }
    }

    private boolean placeBulletsDemo(Game game) {
        try {
            addPlayerBulletToDeck(game.getUsers().get(0), game.getUsers().get(1));

            addPlayerBulletToDeck(game.getUsers().get(1), game.getUsers().get(0));
            addPlayerBulletToDeck(game.getUsers().get(1), game.getUsers().get(0));

            //both players got shot once by the marshal
            addMarshalBulletToDeck(game, game.getUsers().get(0));
            addMarshalBulletToDeck(game, game.getUsers().get(1));

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void addPlayerBulletToDeck(User fromUser, User toUser) {
        PlayerDeck<BulletCard> bulletsDeckFromUser = fromUser.getBulletsDeck();
        PlayerDeck<HandCard> handDeckToUser = toUser.getHandDeck();
        int id = bulletsDeckFromUser.getCards().size() - 1;

        //take a bullet from the shooter to the shot user
        BulletCard bulletCard = (BulletCard) bulletsDeckFromUser.getCards().get(id);
        bulletsDeckFromUser.getCards().remove(id);
        deckRepo.save(bulletsDeckFromUser);

        bulletCard.setDeck(handDeckToUser);
        handDeckToUser.getCards().add(bulletCard);
        cardRepo.save(bulletCard);

        relevelHandDeck(toUser);
    }

    private void addMarshalBulletToDeck(Game fromGame, User toUser) {
        GameDeck<BulletCard> bulletsDeckFrom = fromGame.getNeutralBulletsDeck();
        PlayerDeck<HandCard> handDeckToUser = toUser.getHandDeck();
        int id = bulletsDeckFrom.getCards().size() - 1;

        //take a bullet from the shooter to the shot user
        BulletCard bulletCard = (BulletCard) bulletsDeckFrom.getCards().get(id);
        bulletsDeckFrom.getCards().remove(id);
        deckRepo.save(bulletsDeckFrom);

        bulletCard.setDeck(handDeckToUser);
        handDeckToUser.getCards().add(bulletCard);
        cardRepo.save(bulletCard);

        relevelHandDeck(toUser);
    }

    //put an actioncard to the hiddendeck, so that the total number of handcard stays the same
    private void relevelHandDeck(User user) {
        HandCard handCard = (HandCard) user.getHandDeck().getCards().get(0);
        user.getHandDeck().getCards().remove(0);
        deckRepo.save(user.getHandDeck());

        handCard.setDeck(user.getHiddenDeck());
        user.getHiddenDeck().getCards().add(handCard);
        cardRepo.save(handCard);
    }

    private boolean giveItemsDemo(Game game) {
        try {
            boolean successful = true;
            successful = handOverItem(game.getUsers().get(0), ItemType.BAG, game) && successful;
            successful = handOverItem(game.getUsers().get(0), ItemType.CASE, game) && successful;

            successful = handOverItem(game.getUsers().get(1), ItemType.GEM, game) && successful;
            successful = handOverItem(game.getUsers().get(1), ItemType.GEM, game) && successful;
            successful = handOverItem(game.getUsers().get(1), ItemType.BAG, game) && successful;

            return successful;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean handOverItem(User user, ItemType itemType, Game game) {
        try {
            for (Wagon wagon : game.getWagons()) {
                int index = wagonLevelContainsItem(wagon.getBottomLevel(), itemType);
                if (index != -1) {
                    Item item = wagon.getBottomLevel().getItems().get(index);
                    item.setWagonLevel(null);
                    wagon.getBottomLevel().getItems().remove(index);
                    wagonLevelRepo.save(wagon.getBottomLevel());
                    itemRepo.save(item);
                    user.getItems().add(item);
                    item.setUser(user);
                    itemRepo.save(item);
                    userRepo.save(user);
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    private int wagonLevelContainsItem(WagonLevel wagonLevel, ItemType itemType) {
        int index = 0;
        for (Item item : wagonLevel.getItems()) {
            if (item.getItemType().equals(itemType)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private boolean relocateMarshal(Game game, int wagonId) {
        try {
            WagonLevel wagonLevelNew = game.getWagons().get(wagonId).getBottomLevel();
            Marshal marshal = game.getMarshal();
            WagonLevel wagonLevelOld = marshal.getWagonLevel();
            wagonLevelOld.setMarshal(null);
            wagonLevelRepo.save(wagonLevelOld);
            marshal.setWagonLevel(wagonLevelNew);
            wagonLevelNew.setMarshal(marshal);
            wagonLevelRepo.save(wagonLevelNew);
            marshalRepo.save(marshal);

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean relocatePlayer(User user, WagonLevel wagonLevelNew) {
        try {
            WagonLevel wagonLevelOld = user.getWagonLevel();
            wagonLevelOld.getUsers().remove(user);
            wagonLevelRepo.save(wagonLevelOld);
            user.setWagonLevel(wagonLevelNew);
            wagonLevelNew.getUsers().add(user);
            wagonLevelRepo.save(wagonLevelNew);
            userRepo.save(user);

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    //endregion
}