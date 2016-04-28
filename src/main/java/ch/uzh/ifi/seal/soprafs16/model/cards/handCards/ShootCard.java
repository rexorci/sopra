package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.ShootRequestDTO;

@Entity
@JsonTypeName("shootCard")
public class ShootCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private User user;

    @Override
    public ShootRequestDTO generateActionRequest(Game game, User user){
//        ShootRequestDTO srq = new ShootRequestDTO();
//        List<User> userList = new ArrayList<User>();
//        if(user.getWagonLevel().getLevelType() == LevelType.TOP)
//        {
//            getShootableUsersBeforeR(user, userList, user.getWagonLevel());
//            getShootableUsersAfterR(user, userList, user.getWagonLevel());
//        }
//        if(user.getWagonLevel().getLevelType() == LevelType.BOTTOM)
//        {
//            getShootableUsersBeforeB(user, userList, user.getWagonLevel());
//            getShootableUsersAfterB(user, userList, user.getWagonLevel());
//        }
//        if(userList.size()>2)
//        {
//            for(int i = 0; i < userList.size(); i++)
//            {
//                if(userList.get(i).getCharacterType().equals("Belle"))
//                {
//                    userList.remove(i);
//                }
//            }
//        }
//        for(int i = 0; i<userList.size(); i++)
//        {
//            srq.getShootableUserIds().add(userList.get(i).getId());
//        }
//        srq.setSpielId(game.getId());
//        srq.setUserId(user.getId());
//        game.getActions().add(srq);
//        return srq;
        return null;
    }

    @Override
    public ActionRequestDTO generateMarshalRequest(Game game) {
        return null;
    }

    ;

//    public void getShootableUsersBeforeR(User user, List<User> shootable, WagonLevel wagonLevel)
//    {
//        if( wagonLevel.getWagonLevelBefore() != null)
//        {
//            int size = wagonLevel.getWagonLevelBefore().getUsers().size();
//            for(int i = 0; i< size; i++)
//            {
//                shootable.add(wagonLevel.getWagonLevelBefore().getUsers().get(i));
//            }
//            if (shootable.isEmpty()|| user.getCharacterType().equals("Django")){
//                getShootableUsersBeforeR(user, shootable, wagonLevel.getWagonLevelBefore());
//
//            }
//        }
//    }
//     public void getShootableUsersAfterR(User user, List<User> shootable, WagonLevel wagonLevel)
//     {
//         if(wagonLevel.getWagonLevelAfter() != null)
//         {
//             int size = wagonLevel.getWagonLevelAfter().getUsers().size();
//             for(int i = 0; i < size; i++)
//             {
//                 shootable.add(wagonLevel.getWagonLevelAfter().getUsers().get(i));
//             }
//             if (shootable.isEmpty()|| user.getCharacterType().equals("Django")){
//                 getShootableUsersAfterR(user, shootable, wagonLevel.getWagonLevelAfter());
//             }
//         }
//     }
//
//    public void getShootableUsersBeforeB(User user, List<User> shootable, WagonLevel wagonLevel)
//    {
//        if (wagonLevel.getWagonLevelBefore()!= null)
//        {
//            int size = wagonLevel.getWagonLevelBefore().getUsers().size();
//            for(int i = 0; i < size; i++)
//            {
//                shootable.add(wagonLevel.getWagonLevelBefore().getUsers().get(i));
//            }
//            if (user.getCharacterType().equals("Django") && shootable.isEmpty())
//            {
//                getShootableUsersBeforeB(user, shootable, wagonLevel.getWagonLevelBefore());
//            }
//        }
//
//    }
//
//    public void getShootableUsersAfterB(User user, List<User> shootable, WagonLevel wagonLevel)
//    {
//        if(wagonLevel.getWagonLevelAfter() != null)
//        {
//            int size = wagonLevel.getWagonLevelAfter().getUsers().size();
//            for (int i = 0; i<size; i++)
//            {
//                shootable.add(wagonLevel.getWagonLevelAfter().getUsers().get(i));
//            }
//            if(shootable.isEmpty() && user.getCharacterType().equals("Django"))
//            {
//                getShootableUsersAfterB(user, shootable, wagonLevel.getWagonLevelAfter());
//            }
//        }
//    }



}
