package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.Fleet;
import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;

import java.util.Random;

public class multiFleet extends Fleet {

    /**
     * Number of tankers in the fleet
     */
    private static int FLEET_SIZE = 4;

    private Cell[][] envRep;

    public int getSize() {
        return size;
    }

    private int size;

    public multiFleet(){
        this(new Random());
    }

    public multiFleet(Random r) {
        //Create all of the Tankers
        createTankers(r);

        //Initialise the shared view of the environment
        environmentRepresentationSetup();
    }

    private void environmentRepresentationSetup() {
        //Get the size of the environment and create a view array of the whole environment
        size = 1000; //This is based upon the number of timesteps
        envRep = new Cell[2*size+1][2*size+1];
        // Generate initial environment
        for (int x = -size; x <= size; x++) {
            for (int y = -size; y <= size; y++) {
                envRep[coordToEnvIndex(x)][coordToEnvIndex(y)] = null;
            }
        }
    }

    private void createTankers(Random r) {
        // Create mapping tankers in each of the 4 diagonals
        this.add(new mappingTanker(r, MoveAction.NORTHEAST, this));
        this.add(new mappingTanker(r, MoveAction.NORTHWEST, this));
        this.add(new mappingTanker(r, MoveAction.SOUTHEAST, this));
        this.add(new mappingTanker(r, MoveAction.SOUTHWEST, this));

        for (int i=0; i<FLEET_SIZE; i++) {
//            this.add(new mappingTanker(r));
        }
    }

    public void updateEnvRep(Cell[][] view, int tankerX, int tankerY){
        int tankerPosInView = view.length/2;
        for (int x = 0; x<view.length; x++) {
            for (int y = 0; y<view[x].length; y++) {
                int xCoord = coordToEnvIndex(viewIndexToCoord(x-tankerPosInView, tankerX));
                int yCoord = coordToEnvIndex(viewIndexToCoord(tankerPosInView-y, tankerY));
                if( xCoord < envRep.length && xCoord >= 0 && yCoord < envRep.length && yCoord >= 0 ) {
                    //If xCoord and yCoord are in the range update the view
                    envRep[xCoord][yCoord] = view[x][y];
                }
            }
        }
    }

    public Cell[][] getEnvRep(){
        return envRep;
    }

    private int coordToEnvIndex(int c){
        int zero = size;
        int index = zero + c;
        return index;
    }

    private int viewIndexToCoord(int disRelToTanker, int tankerPos){
        int coord = tankerPos + disRelToTanker;
        return coord;
    }
}
