package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Fleet;

import java.util.Random;

public class singleFleet extends Fleet {

    /**
     * Number of tankers in the fleet
     */
    private static int FLEET_SIZE = 2;

    public int getFleetSize() {
        return FLEET_SIZE;
    }

    public singleFleet(){
        this(new Random());
    }

    /*
        Creates a fleet of FLEET_SIZE single agents
        These are exact copies of the single agents ran in CW1
            Although some code has been refactored for ease of use the functionality is the same
    */
    public singleFleet(Random r) {
        // Create the tankers
        for (int i=0; i<FLEET_SIZE; i++) {
            this.add(new psybc3TankerSingle(r));
        }
    }
}
