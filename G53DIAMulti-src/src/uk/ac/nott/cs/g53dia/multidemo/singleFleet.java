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

    public singleFleet(Random r) {
        // Create the tankers
        for (int i=0; i<FLEET_SIZE; i++) {
            this.add(new psybc3TankerSingle(r));
        }
    }
}
