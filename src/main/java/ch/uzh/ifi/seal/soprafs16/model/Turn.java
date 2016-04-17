package ch.uzh.ifi.seal.soprafs16.model;

/**
 * Created by Nico on 13.04.2016.
 */
public class Turn {
    Type type;

    public Turn(Type type){
        this.type = type;
    }
    public enum Type {
        SPEEDUP, REVERSE, NORMAL
    }

    public Type getType(){
        return type;
    }
}
