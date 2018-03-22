package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.Random;

import static uk.ac.nott.cs.g53dia.multidemo.sharedTankerMethods.*;

public class multiFleet extends Fleet {

    private static int NUMBER_OF_MAPPING_AGENTS = 1;
    private static int NUMBER_OF_MULTI_AGENTS = 2;

    /**
     * Number of tankers in the fleet
     */
    public static int FLEET_SIZE = NUMBER_OF_MULTI_AGENTS + NUMBER_OF_MAPPING_AGENTS;

    private Cell[][] envRep;

    private int size;

    private long timestep;
    private Integer taskListX[];
    private Integer taskListY[];

    public Cell[][] getEnvRep() {
        return envRep;
    }

    public int getSize() {
        return size;
    }

    public int getFleetSize() {
        return FLEET_SIZE;
    }

    public multiFleet() {
        this(new Random());
    }

    public multiFleet(Random r) {
        //Create all of the Tankers
        createTankers(r);

        //Initialise the shared view of the environment
        size = 1000; //This is based upon the number of timesteps
        envRep = envRepSetup(size);

        //Initialise the task list
        taskListX = new Integer[FLEET_SIZE];
        taskListY = new Integer[FLEET_SIZE];
        timestep = -1; // This is to ensure that the timestep and array are initialise
        resetTaskList(0);
    }


    private void createTankers(Random r) {
        // Create mapping tankers going with initial direction based upon each diagonal, rotating around
//        int[] diagonals = new int[]{MoveAction.NORTHEAST, MoveAction.NORTHWEST, MoveAction.SOUTHEAST, MoveAction.SOUTHWEST};
        for (int i=0; i<NUMBER_OF_MAPPING_AGENTS; i++) {
            this.add(new mappingTanker2(r, this));
        }

        // Create the none mapping tankers
        for (int i=NUMBER_OF_MAPPING_AGENTS; i<FLEET_SIZE; i++) {
            this.add(new psybc3TankerMulti(r, this));
        }
    }

    public void processView(Cell[][] view, int tankerX, int tankerY) {
        envRep = updateEnvRep(envRep, view, tankerX, tankerY, size);
    }

    public void resetTaskList(long currentTimeStep) {
        if( timestep != currentTimeStep ){
            for( int k = 0; k < taskListX.length; k++ ) { // Iterate through the given tasks
                taskListX[k] = null;
                taskListY[k] = null;
            }
            timestep = currentTimeStep;
        }
    }

    public distanceToEnvRep findClosestTaskNotGiven(int tankerX, int tankerY) {
        distanceToEnvRep closest = null;
        for (int x = 0; x < envRep.length; x++) { // Iterate through all X coords
            for (int y = 0; y < envRep[x].length; y++) { // Iterate through all Y coords
                Boolean taskGivenOut = false;
                for( int k = 0; k < taskListX.length; k++ ) { // Iterate through the given tasks
                    if (taskListY[k] != null && taskListX[k] != null && taskListX[k] == x && taskListY[k] == y) {
                            taskGivenOut = true;
                    }
                }
                if ( !taskGivenOut ) {
                    if (envRep[x][y] instanceof Station) {
                        Station s = (Station) envRep[x][y];
                        if (s.getTask() != null) {
                            //Only want to find stations with a task
                            if (closest == null) {
                                closest = new distanceToEnvRep(distanceToPointFromCurrentPos(x, y, tankerX, tankerY, size), x, y);
                            } else if (closest.distance > distanceToPointFromCurrentPos(x, y, tankerX, tankerY, size)) {
                                closest = new distanceToEnvRep(distanceToPointFromCurrentPos(x, y, tankerX, tankerY, size), x, y);
                            }
                        }
                    }
                }
            }
        }
        for( int k = 0; k < taskListX.length; k++ ) { // Iterate through the given tasks
            if( taskListX[k] == null && closest != null ){
                taskListX[k] = closest.envX;
                taskListY[k] = closest.envY;
            }
        }
        return closest;
    }
}
