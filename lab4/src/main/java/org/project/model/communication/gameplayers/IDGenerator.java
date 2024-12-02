package org.project.model.communication.gameplayers;

import java.util.Random;

class IDGenerator {
    private final static IDGenerator INSTANCE = new IDGenerator();
    private int id  = new Random().nextInt();
    private IDGenerator(){}
    static IDGenerator getInstance(){
        return INSTANCE;
    }
    int generate(){
        if (id < 0){
            id = -id;
        }
        return id++;
    }
}
