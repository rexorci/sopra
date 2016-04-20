package ch.uzh.ifi.seal.soprafs16.service;

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
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
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

    /**
     * @param game
     * @return
     */
    public Long startGame(Game game, User owner, GameRepository gameRepo, UserRepository userRepo, WagonRepository wagonRepo, WagonLevelRepository wagonLevelRepo, MarshalRepository marshalRepo,
                          DeckRepository deckRepo, CardRepository cardRepo, ItemRepository itemRepo, TurnRepository turnRepo) {
        try {
            boolean initSuccessful = true;

            game.setStatus(GameStatus.RUNNING);
            game.setWagons(new ArrayList<Wagon>());

            initSuccessful = initGameWagons(game, wagonRepo, wagonLevelRepo) && initSuccessful;
            initSuccessful = initPlayerDecks(game, userRepo, deckRepo, cardRepo) && initSuccessful;
            initSuccessful = initGameDecks(game, gameRepo, userRepo, deckRepo, cardRepo, turnRepo) && initSuccessful;
            initSuccessful = initGameFigurines(game.getId(), gameRepo, marshalRepo) && initSuccessful;
            initSuccessful = initGameItems(game.getId(), gameRepo, itemRepo, userRepo, wagonLevelRepo) && initSuccessful;

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

    private boolean initGameWagons(Game game, WagonRepository wagonRepo, WagonLevelRepository wagonLevelRepo) {
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

    private boolean initPlayerDecks(Game game, UserRepository userRepo, DeckRepository deckRepo, CardRepository cardRepo) {
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
                        SourceType st = SourceType.valueOf(user.getCharacterType().toUpperCase());
                        bulletCard.setSourceType(st);
                        bulletsDeck.getCards().add(bulletCard);
                        bulletCard.setDeck(bulletsDeck);
                        cardRepo.save(bulletCard);
                    }
                } else {
                    return false;
                }

                //give 6 handcards to each player
                PlayerDeck<HandCard> handDeck = new PlayerDeck<>();
                handDeck.setUser(user);
                user.setHandDeck(handDeck);
                deckRepo.save(handDeck);
                userRepo.save(user);

                ArrayList<HandCard> allActionCards = (getActionCards(cardRepo, user));

                final int[] randomChosenHandCards = new Random().ints(0, 10).distinct().limit(6).toArray();
                for (int randomIndex : randomChosenHandCards) {
                    handDeck.getCards().add(allActionCards.get(randomIndex));
                    allActionCards.get(randomIndex).setDeck(handDeck);
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
                    }
                }
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean initGameDecks(Game game, GameRepository gameRepo, UserRepository userRepo, DeckRepository deckRepo, CardRepository cardRepo, TurnRepository turnRepo) {
        try {
            //region RoundcardDeck
            //set up a list of possible roundcards (without stationcards) to randomly choose from
            ArrayList<RoundCard> possibleRoundCards = setPatternOnRoundCards(cardRepo, turnRepo);
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
            setPatternOnStationCards(cardRepo, turnRepo);
            Random rn = new Random();
            int stationCardId = rn.nextInt(3);

            RoundCard stationCard = setPatternOnStationCards(cardRepo, turnRepo).get(stationCardId);
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

    private ArrayList<HandCard> getActionCards(CardRepository cardRepo, User user) {
        try {
            ArrayList<HandCard> actionCards = new ArrayList<>();
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
            for (HandCard c : actionCards) {
                cardRepo.save(c);
            }

            return actionCards;
        } catch (Exception ex) {
            return null;
        }
    }

    private ArrayList<RoundCard> setPatternOnRoundCards(CardRepository cardRepo, TurnRepository turnRepo) {
        try {
            ArrayList<RoundCard> possibleRoundCards = new ArrayList<>();

            //AngryMarshalCard
            AngryMarshalCard angryMarshalCard = (AngryMarshalCard) createTurnPattern(new AngryMarshalCard(), turnRepo, cardRepo);
            possibleRoundCards.add(angryMarshalCard);
            //PivotablePoleCard
            PivotablePoleCard pivotablePoleCard = (PivotablePoleCard) createTurnPattern(new PivotablePoleCard(), turnRepo, cardRepo);
            possibleRoundCards.add(pivotablePoleCard);
            //BrakingCard
            BrakingCard brakingCard = (BrakingCard) createTurnPattern(new BrakingCard(), turnRepo, cardRepo);
            possibleRoundCards.add(brakingCard);
            //GetItAllCard
            GetItAllCard getItAllCard = (GetItAllCard) createTurnPattern(new GetItAllCard(), turnRepo, cardRepo);
            possibleRoundCards.add(getItAllCard);
            //PassengerRebellionCard
            PassengerRebellionCard passengerRebellionCard = (PassengerRebellionCard) createTurnPattern(new PassengerRebellionCard(), turnRepo, cardRepo);
            possibleRoundCards.add(passengerRebellionCard);
            //BlankTunnelCard
            BlankTunnelCard blankTunnelCard = (BlankTunnelCard) createTurnPattern(new BlankTunnelCard(), turnRepo, cardRepo);
            possibleRoundCards.add(blankTunnelCard);
            //BlankBridgeCard
            BlankBridgeCard blankBridgeCard = (BlankBridgeCard) createTurnPattern(new BlankBridgeCard(), turnRepo, cardRepo);
            possibleRoundCards.add(blankBridgeCard);

            return possibleRoundCards;
        } catch (Exception ex) {
            return null;
        }
    }

    private ArrayList<RoundCard> setPatternOnStationCards(CardRepository cardRepo, TurnRepository turnRepo) {
        try {
            ArrayList<RoundCard> possibleStationCards = new ArrayList<>();

            //PickPocketingCard
            PickPocketingCard pickPocketingCard = (PickPocketingCard) createTurnPattern(new PickPocketingCard(), turnRepo, cardRepo);
            possibleStationCards.add(pickPocketingCard);
            //MarshallsRevengeCard
            MarshallsRevengeCard marshallsRevengeCard = (MarshallsRevengeCard) createTurnPattern(new MarshallsRevengeCard(), turnRepo, cardRepo);
            possibleStationCards.add(marshallsRevengeCard);
            //HostageCard
            HostageCard hostageCard = (HostageCard) createTurnPattern(new HostageCard(), turnRepo, cardRepo);
            possibleStationCards.add(hostageCard);

            return possibleStationCards;
        } catch (Exception ex) {
            return null;
        }
    }

    private RoundCard createTurnPattern(RoundCard roundCard, TurnRepository turnRepo, CardRepository cardRepo) {
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

    private boolean initGameFigurines(long gameId, GameRepository gameRepo, MarshalRepository marshalRepo) {
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

    private boolean initGameItems(long gameId, GameRepository gameRepo, ItemRepository itemRepo, UserRepository userRepo, WagonLevelRepository wagonLevelRepo) {
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
}
