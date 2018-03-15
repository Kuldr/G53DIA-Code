package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.Fleet;
import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;

import java.util.Random;

import static uk.ac.nott.cs.g53dia.multidemo.sharedTankerMethods.*;

public class multiFleet extends Fleet {

    private static int NUMBER_OF_MAPPING_AGENTS = 4;

    /**
     * Number of tankers in the fleet
     */
    private static int FLEET_SIZE = 3 + NUMBER_OF_MAPPING_AGENTS;

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

    //NOTE: Score of 54275 for 4 mapping and 3 shared memory agents


    private void createTankers(Random r) {
        // Create mapping tankers in each of the 4 diagonals
        this.add(new mappingTanker(r, MoveAction.NORTHEAST, this));
        this.add(new mappingTanker(r, MoveAction.NORTHWEST, this));
        this.add(new mappingTanker(r, MoveAction.SOUTHEAST, this));
        this.add(new mappingTanker(r, MoveAction.SOUTHWEST, this));

        // Create the none mapping tankers
        for (int i=NUMBER_OF_MAPPING_AGENTS; i<FLEET_SIZE; i++) {
            this.add(new psybc3TankerMulti(r, this));
        }
    }

    public void processView(Cell[][] view, int tankerX, int tankerY) {
        envRep = updateEnvRep(envRep, view, tankerX, tankerY, size);
    }

    public Cell[][] getEnvRep() {
        return envRep;
    }
}
