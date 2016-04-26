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
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.CollectItemRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.DrawOrPlayCardRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.MoveMarshalRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.MoveRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.PunchRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.ShootRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.Card;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.CollectCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.MarshalCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.MoveCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.PunchCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ShootCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.roundCards.RoundCard;
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
@Service("glservice")
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
    //endregion

    public void update(Long id) {
        if (!gameRepo.exists(id)) return;
        Game game = gameRepo.findOne(id);
        Hibernate.initialize(game.getCommonDeck());
        Hibernate.initialize(game.getCurrentPhase());
        Hibernate.initialize(game.getCurrentPlayer());
        Hibernate.initialize(game.getActionRequestCounter());
        Hibernate.initialize(game.getRoundStarter());
        Hibernate.initialize(game.getRoundCardDeck());
        Hibernate.initialize(game.getUsers());

        int currentPlayer = game.getCurrentPlayer();
        List<User> users = game.getUsers();

        if (game.getCurrentPhase() == PhaseType.PLANNING) {
            processPlayerTurn(game, currentPlayer, users.size());

            setNextTurn(game, game.getUsers().size());
            if (game.getCurrentTurn() < getCurrentTurns(game).size()) {
                setNextPlayer(game, game.getUsers().size());
            }
            else{
                game.setCurrentPhase(PhaseType.EXECUTION);
                game.setActionRequestCounter(0);
            }
        }
        else if (game.getCurrentPhase() == PhaseType.EXECUTION) {
            logger.debug("phase_execution_start");

            game.getCommonDeck().remove(0);
            if(game.getCommonDeck().size() == 0) {
                // Next round is triggered
                game.setCurrentRound(game.getCurrentRound() + 1);
                if (game.getCurrentRound().equals(GameConstants.ROUNDS)) {
                    finishGame(game);
                } else {
                    game.setRoundStarter((game.getRoundStarter() + 1) % game.getUsers().size() );
                    game.setCurrentPlayer(game.getRoundStarter());
                    game.setCurrentTurn(0);
                    game.setCurrentPhase(PhaseType.PLANNING);
                }
            }
        }

        gameRepo.save(game);
    }

    private void processPlayerTurn(Game game, int currentPlayer, int playerCounter) {
        createDOPCRequestDTO(game, currentPlayer);
        game.setActionRequestCounter(game.getActionRequestCounter() + 1);
    }

    private void setNextTurn(Game game, int playerCounter) {
        // Turn end
        if ((!(game.getCurrentTurnType() instanceof SpeedupTurn) && game.getActionRequestCounter() % playerCounter == 0)
                || (game.getCurrentTurnType() instanceof SpeedupTurn && game.getActionRequestCounter() == (game.getCurrentTurn() + 2) * playerCounter)) {
            game.setCurrentTurn(game.getCurrentTurn() + 1);
            if (game.getCurrentTurn() < ((RoundCard)(game.getRoundCardDeck().get(
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

    private void createDOPCRequestDTO(Game game, int currentplayer) {
        logger.debug("DOPCrequest created");
        DrawOrPlayCardRequestDTO doprq = new DrawOrPlayCardRequestDTO();
        int size = game.getUsers().get(currentplayer).getHandDeck().size();
        for(int i = 0; i<size; i++)
        {
            if(game.getUsers().get(currentplayer).getHandDeck().get(i).getClass() != BulletCard.class)
            {
                doprq.getPlayableCardsId().add(game.getUsers().get(currentplayer).getHandDeck().get(i).getId());
            }
        }

        game.getActions().add(doprq);
        doprq.setSpielId(game.getId());
        Card c = new Card();
        c.setDeck(game.getCommonDeck());
        cardRepo.save(c);
        game.getCommonDeck().add(c);
        deckRepo.save(game.getCommonDeck());

    }

    public ActionRequestDTO createActionRequest(ActionCard actioncard, Long gameId, Long userId)
    {
        ActionRequestHelper arh = new ActionRequestHelper();
        return arh.execute(actioncard, gameId, userId);
    }

    private int mod(int a, int b) {
        int ret = a % b;
        return ret < 0 ? b + ret : ret;
    }

    private List<Turn> getCurrentTurns(Game game) {
        RoundCard rc = (RoundCard)game.getRoundCardDeck().get(game.getCurrentRound());
        ArrayList<Turn> turns = rc.getPattern();
        return turns;
    }

    private class ActionRequestHelper
    {
        private ActionRequestDTO execute(ActionCard ac, Long gameId, Long userId)
        {
            if (ac instanceof CollectCard)
            {
                return generateCollectRequest(gameId, userId);
            }
            if (ac instanceof PunchCard)
            {
                return generatePunchRequest(gameId, userId);
            }
            if (ac instanceof MoveCard)
            {
                return generateMoveRequest(gameId, userId);
            }
            if (ac instanceof ShootCard)
            {
                return generateShootRequest(gameId, userId);
            }

            if (ac instanceof MarshalCard)
            {
                return generateMoveMarshalRequest(gameId);
            }
            return null;

        }

        public ShootRequestDTO generateShootRequest(Long gameId,Long userId )
        {
            Game game = gameRepo.findOne(gameId);
            User user = userRepo.findOne(userId);
            ShootRequestDTO srq = new ShootRequestDTO();
            List<User> userList = new ArrayList<User>();
            srq.setShootableUserIds(new ArrayList<Long>());
            if(user.getWagonLevel().getLevelType() == LevelType.TOP)
            {
                getShootableUsersBeforeR(user, userList, user.getWagonLevel());
                getShootableUsersAfterR(user, userList, user.getWagonLevel());
            }
            if(user.getWagonLevel().getLevelType() == LevelType.BOTTOM)
            {
                getShootableUsersBeforeB(user, userList, user.getWagonLevel());
                getShootableUsersAfterB(user, userList, user.getWagonLevel());
            }
            if(userList.size()>2)
            {
                for(int i = 0; i < userList.size(); i++)
                {
                    if(userList.get(i).getCharacterType().equals("Belle"))
                    {
                        userList.remove(i);
                    }
                }
            }
            for(int i = 0; i<userList.size(); i++)
            {
                srq.getShootableUserIds().add(userList.get(i).getId());
            }
            srq.setSpielId(game.getId());
            srq.setUserId(user.getId());
            game.getActions().add(srq);

            userRepo.save(user);
            gameRepo.save(game);
            return srq;
        }

        public void getShootableUsersBeforeR(User user, List<User> shootable, WagonLevel wagonLevel)
        {
            if( wagonLevel.getWagonLevelBefore() != null)
            {
                int size = wagonLevel.getWagonLevelBefore().getUsers().size();
                for(int i = 0; i< size; i++)
                {
                    shootable.add(wagonLevel.getWagonLevelBefore().getUsers().get(i));
                }
                if (shootable.isEmpty()|| user.getCharacterType().equals("Django")){
                    getShootableUsersBeforeR(user, shootable, wagonLevel.getWagonLevelBefore());

                }
            }
        }
        public void getShootableUsersAfterR(User user, List<User> shootable, WagonLevel wagonLevel)
        {
            if(wagonLevel.getWagonLevelAfter() != null)
            {
                int size = wagonLevel.getWagonLevelAfter().getUsers().size();
                for(int i = 0; i < size; i++)
                {
                    shootable.add(wagonLevel.getWagonLevelAfter().getUsers().get(i));
                }
                if (shootable.isEmpty()|| user.getCharacterType().equals("Django")){
                    getShootableUsersAfterR(user, shootable, wagonLevel.getWagonLevelAfter());
                }
            }
        }

        public void getShootableUsersBeforeB(User user, List<User> shootable, WagonLevel wagonLevel)
        {
            if (wagonLevel.getWagonLevelBefore()!= null)
            {
                int size = wagonLevel.getWagonLevelBefore().getUsers().size();
                for(int i = 0; i < size; i++)
                {
                    shootable.add(wagonLevel.getWagonLevelBefore().getUsers().get(i));
                }
                if (user.getCharacterType().equals("Django") && shootable.isEmpty())
                {
                    getShootableUsersBeforeB(user, shootable, wagonLevel.getWagonLevelBefore());
                }
            }

        }

        public void getShootableUsersAfterB(User user, List<User> shootable, WagonLevel wagonLevel)
        {
            if(wagonLevel.getWagonLevelAfter() != null)
            {
                int size = wagonLevel.getWagonLevelAfter().getUsers().size();
                for (int i = 0; i<size; i++)
                {
                    shootable.add(wagonLevel.getWagonLevelAfter().getUsers().get(i));
                }
                if(shootable.isEmpty() && user.getCharacterType().equals("Django"))
                {
                    getShootableUsersAfterB(user, shootable, wagonLevel.getWagonLevelAfter());
                }
            }
        }

        public CollectItemRequestDTO generateCollectRequest(Long gameId, Long userId) {
            Game game = gameRepo.findOne(gameId);
            User user = userRepo.findOne(userId);
            CollectItemRequestDTO crq = new CollectItemRequestDTO();
            crq.setHasBag(Boolean.FALSE);
            crq.setHasCase(Boolean.FALSE);
            crq.setHasGem(Boolean.FALSE);
            if (user.getWagonLevel().getItems().size() > 0) {
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
            crq.setSpielId(game.getId());
            crq.setUserId(user.getId());
            game.getActions().add(crq);

            crq.setGame(game);

            actionRepo.save(crq);
            userRepo.save(user);
            gameRepo.save(game);
            return crq;
        }

        public MoveRequestDTO generateMoveRequest(Long gameId, Long userId)
        {
            User user = userRepo.findOne(userId);
            Game game = gameRepo.findOne(gameId);
            MoveRequestDTO mrq = new MoveRequestDTO();
            List<Long> movable = new ArrayList<Long>();
            mrq.setMovableWagonsLvlIds(new ArrayList<Long>());

            if(user.getWagonLevel().getLevelType() == LevelType.TOP)
            {
                getMovableBeforeR(user, movable, user.getWagonLevel());
                getMovableAfterR(user, movable, user.getWagonLevel());

                if (movable.size() > 3) {
                    for (int i = 0; i < 3; i++) {
                        mrq.getMovableWagonsLvlIds().add(movable.get(i));
                    }
                }
                else
                {
                    for (int i = 0; i<movable.size();i++)
                    {
                        mrq.getMovableWagonsLvlIds().add(movable.get(i));
                    }
                }

            }

            if(user.getWagonLevel().getLevelType() == LevelType.BOTTOM)
            {
                if(user.getWagonLevel().getWagonLevelBefore() != null) {
                    mrq.getMovableWagonsLvlIds().add(user.getWagonLevel().getWagonLevelBefore().getId());
                }
                if(user.getWagonLevel().getWagonLevelAfter() != null) {
                    mrq.getMovableWagonsLvlIds().add(user.getWagonLevel().getWagonLevelAfter().getId());
                }

            }

            mrq.setSpielId(game.getId());
            mrq.setUserId(user.getId());
            game.getActions().add(mrq);
            actionRepo.save(mrq);
            userRepo.save(user);
            gameRepo.save(game);

            return mrq;
        }

        public void getMovableBeforeR(User user, List<Long> movable, WagonLevel wagonLevel)
        {
            if( wagonLevel.getWagonLevelBefore() != null)
            {
                movable.add(wagonLevel.getWagonLevelBefore().getId());
                getMovableBeforeR(user, movable, wagonLevel.getWagonLevelBefore());
            }
        }
        public void getMovableAfterR(User user, List<Long> movable, WagonLevel wagonLevel)
        {

            if( wagonLevel.getWagonLevelBefore() != null)
            {
                movable.add(wagonLevel.getWagonLevelBefore().getId());
                getMovableAfterR(user, movable, wagonLevel.getWagonLevelBefore());
            }
        }


        public void getMovableBeforeB(User user, List<Long> movable, WagonLevel wagonLevel)
        {

            if( wagonLevel.getWagonLevelBefore() != null)
            {
                movable.add(wagonLevel.getWagonLevelBefore().getId());
                getMovableBeforeB(user, movable, wagonLevel.getWagonLevelBefore());
            }


        }

        public void getMovableAfterB(User user, List<Long> movable, WagonLevel wagonLevel)
        {

            if( wagonLevel.getWagonLevelBefore() != null)
            {
                movable.add(wagonLevel.getWagonLevelBefore().getId());
                getMovableAfterB(user, movable, wagonLevel.getWagonLevelBefore());
            }

        }

        public PunchRequestDTO generatePunchRequest(Long gameId, Long userId)
        {
            User user = userRepo.findOne(userId);
            Game game = gameRepo.findOne(gameId);

            PunchRequestDTO prq = new PunchRequestDTO();
            List<User> userList = new ArrayList<User>();
            prq.setPunchableUserIds(new ArrayList<Long>());

            for(int i = 0; i<user.getWagonLevel().getUsers().size(); i++ ) {
                userList.add(user.getWagonLevel().getUsers().get(i));
            }
            if(userList.size() > 2)
            {
                for(int i = 0; i < userList.size(); i++)
                {
                    if(userList.get(i).getCharacterType()== ("Belle") || userList.get(i).getId() == user.getId())
                    {
                        userList.remove(i);
                    }
                }
            }
            for(int i = 0; i<userList.size(); i++)
            {
                prq.getHasBag().add(i,Boolean.FALSE);
                prq.getHasCase().add(i, Boolean.FALSE);
                prq.getHasGem().add(i, Boolean.FALSE);
                prq.getPunchableUserIds().add(userList.get(i).getId());

                if(userList.get(i).getItems().size() > 0)
                {
                    for(int j = 0; j < userList.get(i).getItems().size(); j++)
                    {
                        if(userList.get(i).getItems().get(j).getItemType() == ItemType.GEM)
                        {
                            prq.getHasGem().set(i, Boolean.TRUE);
                        }

                        if(userList.get(i).getItems().get(j).getItemType() == ItemType.BAG)
                        {
                            prq.getHasBag().set(i, Boolean.TRUE);
                        }

                        if(userList.get(i).getItems().get(j).getItemType() == ItemType.CASE) {
                            prq.getHasCase().set(i, Boolean.TRUE);
                        }
                    }
                }
            }
            if(user.getWagonLevel().getWagonLevelBefore() != null)
            {
                prq.getMovable().add(user.getWagonLevel().getWagonLevelBefore().getId());
            }
            if(user.getWagonLevel().getWagonLevelAfter() != null)
            {
                prq.getMovable().add(user.getWagonLevel().getWagonLevelAfter().getId());
            }

            prq.setSpielId(game.getId());
            prq.setUserId(user.getId());
            game.getActions().add(prq);
            userRepo.save(user);
            gameRepo.save(game);
            return prq;
        }

        public MoveMarshalRequestDTO generateMoveMarshalRequest(Long gameId)
        {
            Game game = gameRepo.findOne(gameId);
            MoveMarshalRequestDTO mmrq = new MoveMarshalRequestDTO();
            mmrq.getMovableWagonsLvlIds().add(game.getMarshal().getWagonLevel().getWagonLevelBefore().getId());
            mmrq.getMovableWagonsLvlIds().add(game.getMarshal().getWagonLevel().getWagonLevelAfter().getId());


            mmrq.setSpielId(game.getId());
            game.getActions().add(mmrq);
            gameRepo.save(game);
            return mmrq;
        }



    }

}
