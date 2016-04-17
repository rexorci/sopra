package ch.uzh.ifi.seal.soprafs16.model;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nico on 13.04.2016.
 */
public class GameDeck<E extends Card> extends ArrayList<E> {
    Game game;

    public Game getGame() {
        return game;
    }
}
