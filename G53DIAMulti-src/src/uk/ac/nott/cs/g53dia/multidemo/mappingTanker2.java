package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.Random;

import static uk.ac.nott.cs.g53dia.multidemo.sharedTankerMethods.*;

// A tanker set up specifically to aid the other tankers by mapping the environement
    //This iteration randomly travels around mapping out the environment
public class mappingTanker2 extends Tanker {

    private multiFleet fleet;

    private int moveTowardsX;
    private int moveTowardsY;

    private int tankerX;
    private int tankerY;

    private int tankerXToUpdate;
    private int tankerYToUpdate;

    public mappingTanker2(multiFleet fleet) {
        this(new Random(), fleet);
    }

    public mappingTanker2(Random r, multiFleet fleet) {
        this.r = r;
        this.fleet = fleet;
        this.tankerSetup();
    }

    private void tankerSetup() {
        //Initialise the tanker to the origin
        tankerX = 0;
        tankerY = 0;

        updateCoordsToMoveTo();
    }

    @Override
    public Action senseAndAct(Cell[][] view, long timestep) {

        if( !actionFailed ) {
            // Action passed update the system
            tankerX = tankerXToUpdate;
            tankerY = tankerYToUpdate;
        }

        //Reset the update variables
        tankerXToUpdate = tankerX;
        tankerYToUpdate = tankerY;

        //Update the environment Representation
        fleet.processView(view, tankerX, tankerY);

        //Check if tanker reached the moveTowards Coord
        if( tankerX == moveTowardsX && tankerY == moveTowardsY ){
            //Reached the destination therefore change direction and move towards
            updateCoordsToMoveTo();
        }

        distanceToEnvRep closestFuelPump = findClosestFuelPump(fleet.getEnvRep(), tankerX, tankerY, fleet.getSize());

        //Evaluate each situation
        boolean checkMoveToFuelPump = checkMoveToFuelPump(closestFuelPump, getFuelLevel(), getCurrentCell(view));
        boolean checkRefuel         = checkRefuel(getFuelLevel(), getCurrentCell(view));

        //Priority 1: Actions that require you to be on the tile at that time,
        //              unlikely to happen randomly but if relevant should be resolved as.
        //              These task typically will be invoked as we have moved towards these tiles recently to try and complete another task
        //              By completing these task it will "end" the inferred long term behaviour of some tasks
        //              Note: As they require to be on a specific tile there are no clashes in this tier that need to be resolved.
        if( checkRefuel ) {
            updateCoordsToMoveTo();
            return new RefuelAction();
        }
        //Priority 2: Maintenance actions that need to be satisfied before other actions to avoid failure due to lack of resources
        if( checkMoveToFuelPump ){
            int move = directionToMoveTowards(closestFuelPump.envX, closestFuelPump.envY, tankerX, tankerY, fleet.getSize());
            tankerXToUpdate += updateTankerXPos(move);
            tankerYToUpdate += updateTankerYPos(move);
            return new MoveAction(move);
        }

        //Priority 5: This agents only task is to explore for the benefit off all other agents
        //              As nothing else is taking priority complete this task
        int move = directionToMoveTowards(coordToEnvIndex(moveTowardsX, fleet.getSize()),
                                            coordToEnvIndex(moveTowardsY, fleet.getSize()),
                                            tankerX, tankerY, fleet.getSize());
        tankerXToUpdate += updateTankerXPos(move);
        tankerYToUpdate += updateTankerYPos(move);
        return new MoveAction(move);

    }

    private void updateCoordsToMoveTo() {
        int minimumDelta = -Tanker.MAX_FUEL/2;
        int maximumDelta = Tanker.MAX_FUEL/2;
        int deltaX = r.nextInt((maximumDelta - minimumDelta) + 1 ) + minimumDelta;
        int deltaY = r.nextInt((maximumDelta - minimumDelta) + 1 ) + minimumDelta;
        moveTowardsX = tankerX + deltaX;
        moveTowardsY = tankerY + deltaY;
    }
}