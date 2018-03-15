package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.Random;

import static uk.ac.nott.cs.g53dia.multidemo.sharedTankerMethods.*;
import static uk.ac.nott.cs.g53dia.multilibrary.MoveAction.*;

public class mappingTanker extends Tanker {

    private multiFleet fleet;

    private int moveDirection;
    private int moveTowardsX;
    private int moveTowardsY;

    private int tankerX;
    private int tankerY;

    private int tankerXToUpdate;
    private int tankerYToUpdate;

    public mappingTanker(int diagonalDirection, multiFleet fleet) {
        this(new Random(), diagonalDirection, fleet);
    }

    public mappingTanker(Random r, int diagonalDirection, multiFleet fleet) {
        this.r = r;
        this.moveDirection = diagonalDirection;
        this.fleet = fleet;
        this.tankerSetup();
    }

    private void tankerSetup() {
        //Initialise the tanker to the origin
        tankerX = 0;
        tankerY = 0;

        updateCoordsToMoveTo();

        //TODO: Write tanker setup
    }

    @Override
    public Action senseAndAct(Cell[][] view, long timestep) {

        //TODO: OMG HELP THIS IS AWFUL, VERY EASY TO GET STUCK IN A LOOP BUT HOW TO STAY SYSTEMATIC

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
            moveDirection = nextDirection();
            updateCoordsToMoveTo();
        }

        distanceToEnvRep closestFuelPump = findClosestFuelPump(fleet.getEnvRep(), tankerX, tankerY, fleet.getSize());

        //TODO: Move Booleans into the shared tanker??

        //Evaluate each situation
        boolean checkMoveToFuelPump = closestFuelPump != null
                                        && getFuelLevel() <= Math.ceil(closestFuelPump.distance*1.0015+1)
                                        && !(getCurrentCell(view) instanceof FuelPump);
                                      // Check if there is enough fuel to get to the nearest fuel pump so long as you are not on a fuel pump
        boolean checkRefuel         = getCurrentCell(view) instanceof FuelPump
                                        && getFuelLevel() < MAX_FUEL;
                                      // Check if you are on a fuel pump and you have less than max fuel

        //TODO: MAKE REFUELING RETURN TO THE ORIGINAL POINT ALONG THE ROUTE ? MAYBE MAYBE NOT BUT WE HAVE BIGGER PROBLEMS RN

        //Priority 1: Actions that require you to be on the tile at that time,
        //              unlikely to happen randomly but if relevant should be resolved as.
        //              These task typically will be invoked as we have moved towards these tiles recently to try and complete another task
        //              By completing these task it will "end" the inferred long term behaviour of some tasks
        //              Note: As they require to be on a specific tile there are no clashes in this tier that need to be resolved.
        if( checkRefuel ) {
            return new RefuelAction();
        }
        //Priority 2: Maintenance actions that need to be satisfied before other actions to avoid failure due to lack of resources
        if( checkMoveToFuelPump ){
            int move = directionToMoveTowards(closestFuelPump.envX, closestFuelPump.envY, tankerX, tankerY, fleet.getSize());
            tankerXToUpdate += updateTankerXPos(move);
            tankerYToUpdate += updateTankerYPos(move);
            return new MoveAction(move);
        }
        //Priority 3: This agents only task is to explore for the benefit off all other agents
        //              As nothing else is taking priority complete this task
        int move = directionToMoveTowards(coordToEnvIndex(moveTowardsX, fleet.getSize()),
                                            coordToEnvIndex(moveTowardsY, fleet.getSize()),
                                            tankerX, tankerY, fleet.getSize());
        tankerXToUpdate += updateTankerXPos(move);
        tankerYToUpdate += updateTankerYPos(move);
        return new MoveAction(move);
    }

    private int nextDirection(){
        switch(moveDirection) {
            case NORTHEAST:
                return WEST;
            case WEST:
                return NORTHWEST;
            case NORTHWEST:
                return SOUTH;
            case SOUTH:
                return SOUTHWEST;
            case SOUTHWEST:
                return EAST;
            case EAST:
                return SOUTHEAST;
            case SOUTHEAST:
                return NORTH;
            case NORTH:
                return NORTHEAST;
            default:
                return NORTHEAST;
        }
    }

    private void updateCoordsToMoveTo() {
        moveTowardsX = coordXToMoveTo();
        moveTowardsY = coordYToMoveTo();
    }

    private int coordXToMoveTo() {
        switch(moveDirection) {
            case(WEST):
            case(EAST):
                return -tankerX; //Flip the sign of the x coord to move towards
            case(NORTHEAST):
            case(SOUTHEAST):
                return tankerX+2*VIEW_RANGE; //Move outside the current view
            case(NORTHWEST):
            case(SOUTHWEST):
                return tankerX-2*VIEW_RANGE; //Move outside the current view
            default:
                return tankerX; //Move action is either invalid so won't move or X doesn't change
        }
    }

    private int coordYToMoveTo() {
        switch(moveDirection) {
            case(NORTH):
            case(SOUTH):
                return -tankerY;
            case(NORTHWEST):
            case(NORTHEAST):
                return tankerY+2*VIEW_RANGE;  //Move outside the current view
            case(SOUTHWEST):
            case(SOUTHEAST):
                return tankerY-2*VIEW_RANGE;  //Move outside the current view
            default:
                return tankerY; //Move action is either invalid so won't move or Y doesn't change
        }
    }
}