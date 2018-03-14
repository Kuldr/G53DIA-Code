package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Fleet;

import java.util.Random;

public class psybc3Fleet extends Fleet {

    /**
     * Number of tankers in the fleet
     */
    private static int FLEET_SIZE = 10;

    public psybc3Fleet() {
        // Creation/Seeding of the random function
        Random r = new Random();

        // Create the tankers
        for (int i=0; i<FLEET_SIZE; i++) {
            this.add(new psybc3TankerSingle(r));
        }
    }
}
