package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.Fleet;
import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;

import java.util.Random;

import static uk.ac.nott.cs.g53dia.multidemo.sharedTankerMethods.*;

public class multiFleet extends Fleet {
    //TODO: REFACTOR THE SHARED METHODS IN THIS CLASS

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
        size = 1000; //This is based upon the number of timesteps
        envRep = envRepSetup(size);
    }

    private void createTankers(Random r) {
        // Create mapping tankers in each of the 4 diagonals
        this.add(new mappingTanker(r, MoveAction.NORTHEAST, this));
        this.add(new mappingTanker(r, MoveAction.NORTHWEST, this));
        this.add(new mappingTanker(r, MoveAction.SOUTHEAST, this));
        this.add(new mappingTanker(r, MoveAction.SOUTHWEST, this));

//        for (int i=0; i<FLEET_SIZE; i++) {
//            this.add(new mappingTanker(r));
//        }
    }

    public void processView(Cell[][] view, int tankerX, int tankerY) {
        envRep = updateEnvRep(envRep, view, tankerX, tankerY, size);
    }

    public Cell[][] getEnvRep() {
        return envRep;
    }
}
