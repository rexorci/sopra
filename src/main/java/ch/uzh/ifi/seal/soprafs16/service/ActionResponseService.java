package ch.uzh.ifi.seal.soprafs16.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.Item;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
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
import ch.uzh.ifi.seal.soprafs16.model.repositories.DeckRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.ItemRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.UserRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.WagonLevelRepository;

/**
 * Created by Nico on 12.04.2016.
 */
@Service
@Transactional
public class ActionResponseService {
    @Autowired
    public UserRepository userRepo;
    @Autowired
    public GameRepository gameRepo;
    @Autowired
    public WagonLevelRepository wagonLevelRepo;
    @Autowired
    public ItemRepository itemRepo;
    @Autowired
    public CardRepository cardRepo;
    @Autowired
    public DeckRepository deckRepo;
    //endregion

    public void processResponse(DrawCardResponseDTO dcr) {
        User user = userRepo.findOne(dcr.getUserID());
        
        PlayerDeck<HandCard> hiddenDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHiddenDeck().getId());
        PlayerDeck<HandCard> handDeck = (PlayerDeck<HandCard>)deckRepo.findOne(user.getHandDeck().getId());
        for(int i = 0; i < 3; i++){
            if(hiddenDeck.size() > 0){
                HandCard hc = (HandCard)hiddenDeck.remove(hiddenDeck.size() - 1);
                handDeck.add(hc);
                hc.setDeck(handDeck);
                cardRepo.save(hc);
            }
        }
        deckRepo.save(hiddenDeck);
        deckRepo.save(handDeck);
    }

    public void processResponse(PlayCardResponseDTO pcr) {
        Game game = gameRepo.findOne(pcr.getGameId());
        User user = userRepo.findOne(pcr.getUserID());
        
        ActionCard ac = (ActionCard)cardRepo.findOne(pcr.getPlayedCard().getId());

        PlayerDeck<HandCard> handDeck = user.getHandDeck();

        handDeck.removeById(ac.getId());

        GameDeck<ActionCard> commonDeck = game.getCommonDeck();
        commonDeck.add(ac);
        ac.setDeck(commonDeck);

        cardRepo.save(ac);
        deckRepo.save(handDeck);
        deckRepo.save(commonDeck);
    }

    public void processResponse(MoveResponseDTO mr) {
        User user = userRepo.findOne(mr.getUserID());
        
        WagonLevel new_wl = wagonLevelRepo.findOne(mr.getWagonLevelId());
        WagonLevel old_wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());

        old_wl.removeUserById(user.getId());
        new_wl.getUsers().add(user);

        user.setWagonLevel(new_wl);

        wagonLevelRepo.save(old_wl);
        wagonLevelRepo.save(new_wl);
        userRepo.save(user);
    }

    public void processResponse(CollectItemResponseDTO cir) {
        User user = userRepo.findOne(cir.getUserID());


        WagonLevel wl = wagonLevelRepo.findOne(user.getWagonLevel().getId());
        Item item = itemRepo.findOne(getRandomItem(cir.getCollectedItemType(), wl).getId());
        wl.removeItemById(item.getId());

        item.setWagonLevel(null);
        item.setUser(user);
        user.getItems().add(item);

        itemRepo.save(item);
        wagonLevelRepo.save(wl);
        userRepo.save(user);
    }

    public void processResponse(PunchResponseDTO pr) {
        User user = userRepo.findOne(pr.getUserID());

        User victim = userRepo.findOne(pr.getVictimID());
        WagonLevel move_wl = wagonLevelRepo.findOne(pr.getWagonLevelID());
        WagonLevel drop_wl = wagonLevelRepo.findOne(victim.getWagonLevel().getId());
        Item item = getRandomItem(pr.getItemType(), victim);
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
    }

    public void processResponse(ShootResponseDTO sr) {
        User user = userRepo.findOne(sr.getUserID());

        if(user.getBulletsDeck().size() > 0){
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

    private Item getRandomItem(ItemType type, User user){
        if(type != ItemType.BAG){
            for(int i = 0; i < user.getItems().size(); i++){
                if(user.getItems().get(i).getItemType() == type){
                    return user.getItems().get(i);
                }
            }
        }
        else{
            List<Item> bags = new ArrayList<>();
            for(int i = 0; i < user.getItems().size(); i++){
                if(user.getItems().get(i).getItemType() == ItemType.BAG){
                    bags.add(user.getItems().get(i));
                }
            }
            return bags.get((int)(Math.random() * bags.size()));
        }
        return null;
    }

    private Item getRandomItem(ItemType type, WagonLevel wagonLevel){
        if(type != ItemType.BAG){
            for(int i = 0; i < wagonLevel.getItems().size(); i++){
                if(wagonLevel.getItems().get(i).getItemType() == type){
                    return wagonLevel.getItems().get(i);
                }
            }
        }
        else{
            List<Item> bags = new ArrayList<>();
            for(int i = 0; i < wagonLevel.getItems().size(); i++){
                if(wagonLevel.getItems().get(i).getItemType() == ItemType.BAG){
                    bags.add(wagonLevel.getItems().get(i));
                }
            }
            return bags.size() > 0 ? bags.get((int)(Math.random() * bags.size())) : null;
        }
        return null;
    }
}
