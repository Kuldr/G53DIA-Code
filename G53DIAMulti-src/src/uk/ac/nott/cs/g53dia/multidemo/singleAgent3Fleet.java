package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Fleet;

import java.util.Random;

public class singleAgent3Fleet extends Fleet {

    /**
     * Number of tankers in the fleet
     */
    private static int FLEET_SIZE = 3;

    public singleAgent3Fleet(){
        this(new Random());
    }

    public singleAgent3Fleet(Random r) {
        // Create the tankers
        for (int i=0; i<FLEET_SIZE; i++) {
            this.add(new psybc3TankerSingle(r));
        }
    }
}
