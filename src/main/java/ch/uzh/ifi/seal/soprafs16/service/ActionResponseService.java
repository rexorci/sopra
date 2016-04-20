package ch.uzh.ifi.seal.soprafs16.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.Item;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.CollectItemResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.DrawCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.MoveResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.PlayCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.PunchResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.ShootResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.cards.GameDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.PlayerDeck;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.ActionCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.BulletCard;
import ch.uzh.ifi.seal.soprafs16.model.cards.handCards.HandCard;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CardRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.CharacterRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.MarshalRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonRepository;

/**
 * Created by Nico on 12.04.2016.
 */
@Service
public class ActionResponseService {
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

    public void processResponse(ActionResponseDTO response) {
        Game game = gameRepo.findOne(response.getGameId());
        User user = userRepo.findOne(response.getUserID());

        if (response instanceof DrawCardResponseDTO) {
            for(int i = 0; i < 3; i++){
                if(user.getHiddenDeck().size() > 0){
                    HandCard hc = (HandCard)user.getHiddenDeck().remove(user.getHiddenDeck().size() - 1);
                    PlayerDeck<HandCard> handDeck = user.getHandDeck();
                    handDeck.add(hc);
                    hc.setDeck(handDeck);

                    cardRepo.save(hc);
                    deckRepo.save(handDeck);
                    deckRepo.save(user.getHiddenDeck());
                }
            }
        } else if (response instanceof PlayCardResponseDTO) {
            PlayCardResponseDTO pcdto = (PlayCardResponseDTO)response;
            ActionCard ac = pcdto.getPlayedCard();

            PlayerDeck<HandCard> handDeck = user.getHandDeck();

            handDeck.getCards().remove(ac);

            GameDeck<ActionCard> commonDeck = game.getCommonDeck();
            commonDeck.add(ac);
            ac.setDeck(commonDeck);

            deckRepo.save(commonDeck);
            deckRepo.save(handDeck);
            cardRepo.save(ac);
        } else if (response instanceof MoveResponseDTO) {
            MoveResponseDTO mr = (MoveResponseDTO)response;
            WagonLevel new_wl = wagonLevelRepo.findOne(mr.getWagonLevelId());
            WagonLevel old_wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());

            old_wl.getUsers().remove(user);
            new_wl.getUsers().add(user);

            user.setWagonLevel(new_wl);

            wagonLevelRepo.save(old_wl);
            wagonLevelRepo.save(new_wl);
            userRepo.save(user);
        } else if (response instanceof CollectItemResponseDTO) {
            CollectItemResponseDTO cir = (CollectItemResponseDTO)response;
            Item item = itemRepo.findOne(cir.getId());
            WagonLevel wl = wagonLevelRepo.findOne(item.getWagonLevel().getId());
            wl.getItems().remove(item);

            item.setWagonLevel(null);
            item.setUser(user);

            user.getItems().add(item);

            itemRepo.save(item);
            userRepo.save(user);
            wagonLevelRepo.save(wl);
        } else if (response instanceof PunchResponseDTO) {
            PunchResponseDTO pr = (PunchResponseDTO)response;
            User victim = userRepo.findOne(pr.getVictimID());
            WagonLevel move_wl = wagonLevelRepo.findOne(pr.getWagonLevelID());
            WagonLevel drop_wl = wagonLevelRepo.findOne(victim.getWagonLevel().getId());
            Item item = getRandomItem(pr.getItemType(), drop_wl);
            item = itemRepo.findOne(item.getId());
            // Drop Item
            victim.getItems().remove(item);
            drop_wl.getItems().add(item);

            item.setUser(null);
            item.setWagonLevel(drop_wl);
            // Move user
            drop_wl.getUsers().remove(victim);
            move_wl.getUsers().add(victim);

            victim.setWagonLevel(move_wl);

            wagonLevelRepo.save(drop_wl);
            wagonLevelRepo.save(move_wl);
            userRepo.save(user);
            userRepo.save(victim);
            itemRepo.save(item);
        } else if (response instanceof ShootResponseDTO) {
            if(user.getBulletsDeck().size() > 0){
                ShootResponseDTO sr = (ShootResponseDTO)response;
                User victim = userRepo.findOne(sr.getVictimId());
                PlayerDeck<BulletCard> bulletCardDeck = user.getBulletsDeck();
                PlayerDeck<HandCard> hiddenDeck = victim.getHiddenDeck();
                BulletCard bc = (BulletCard)bulletCardDeck.remove(user.getBulletsDeck().size() - 1);
                hiddenDeck.add(bc);

                bc.setDeck(hiddenDeck);
                deckRepo.save(bulletCardDeck);
                deckRepo.save(hiddenDeck);
                userRepo.save(victim);
                userRepo.save(user);
                cardRepo.save(bc);
            }
        }

        gameRepo.save(game);
    }

    public Item getRandomItem(ItemType type, WagonLevel level){
        if(type != ItemType.BAG){
            for(int i = 0; i < level.getItems().size(); i++){
                if(level.getItems().get(i).getItemType() == type){
                    return level.getItems().get(i);
                }
            }
        }
        else{
             List<Item> bags = new ArrayList<>();
            for(int i = 0; i < level.getItems().size(); i++){
                if(level.getItems().get(i).getItemType() == ItemType.BAG){
                    bags.add(level.getItems().get(i));
                }
            }
            return bags.get((int)(Math.random() * bags.size() + 1));
        }
        return null;
    }
}
