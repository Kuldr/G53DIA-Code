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

    private boolean needToReturnToPath = false;
    private Integer returnToPathX = null;
    private Integer returnToPathY = null;

    private int tankerX;
    private int tankerY;

    private int tankerXToUpdate;
    private int tankerYToUpdate;

    private int diagonalDirection;

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
        diagonalDirection = newDiagonalDirection(r);
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
            moveDirection = nextDirection();
            updateCoordsToMoveTo();
        }

        distanceToEnvRep closestFuelPump = findClosestFuelPump(fleet.getEnvRep(), tankerX, tankerY, fleet.getSize());

        //Evaluate each situation
        boolean checkMoveToFuelPump = checkMoveToFuelPump(closestFuelPump, getFuelLevel(), getCurrentCell(view));
        boolean checkRefuel         = checkRefuel(getFuelLevel(), getCurrentCell(view));
        boolean checkMoveTowards    = !isPointFurtherThanFuel(coordToEnvIndex(moveTowardsX, fleet.getSize()), coordToEnvIndex(moveTowardsY, fleet.getSize()), closestFuelPump.envX, closestFuelPump.envY, tankerX, tankerY, getFuelLevel(), fleet.getSize());
        boolean checkReturnToPath;
        if( returnToPathX != null && returnToPathY != null ) {
            checkReturnToPath = !isPointFurtherThanFuel(coordToEnvIndex(returnToPathX, fleet.getSize()), coordToEnvIndex(returnToPathY, fleet.getSize()), closestFuelPump.envX, closestFuelPump.envY, tankerX, tankerY, getFuelLevel(), fleet.getSize());
        } else {
            checkReturnToPath = false;
        }
        if( returnToPathX != null && returnToPathY != null && tankerX == returnToPathX && tankerY == returnToPathY ) {
            needToReturnToPath = false;
            returnToPathX = null;
            returnToPathY = null;
        }

        //Priority 1: Actions that require you to be on the tile at that time,
        //              unlikely to happen randomly but if relevant should be resolved as.
        //              These task typically will be invoked as we have moved towards these tiles recently to try and complete another task
        //              By completing these task it will "end" the inferred long term behaviour of some tasks
        //              Note: As they require to be on a specific tile there are no clashes in this tier that need to be resolved.
        if( checkRefuel ) {
            diagonalDirection = newDiagonalDirection(r);
            return new RefuelAction();
        }
        //Priority 2: Maintenance actions that need to be satisfied before other actions to avoid failure due to lack of resources
        if( checkMoveToFuelPump ){
            if(  !needToReturnToPath ) {
                //Set the return point before moving
                needToReturnToPath = true;
                returnToPathX = tankerX;
                returnToPathY = tankerY;
            }
            int move = directionToMoveTowards(closestFuelPump.envX, closestFuelPump.envY, tankerX, tankerY, fleet.getSize());
            tankerXToUpdate += updateTankerXPos(move);
            tankerYToUpdate += updateTankerYPos(move);
            return new MoveAction(move);
        }

        //Priority 3: Return back to the point on the path where the agent was before diverting to refuel
        if( needToReturnToPath && checkReturnToPath ) {
            int move = directionToMoveTowards(coordToEnvIndex(returnToPathX, fleet.getSize()), coordToEnvIndex(returnToPathY, fleet.getSize()), tankerX, tankerY, fleet.getSize());
            tankerXToUpdate += updateTankerXPos(move);
            tankerYToUpdate += updateTankerYPos(move);
            return new MoveAction(move);
        }


        //Priority 4: This agents only task is to explore for the benefit off all other agents
        //              As nothing else is taking priority complete this task
        if( checkMoveTowards ){
            int move = directionToMoveTowards(coordToEnvIndex(moveTowardsX, fleet.getSize()),
                                                coordToEnvIndex(moveTowardsY, fleet.getSize()),
                                                tankerX, tankerY, fleet.getSize());
            tankerXToUpdate += updateTankerXPos(move);
            tankerYToUpdate += updateTankerYPos(move);
            return new MoveAction(move);
        }

        //Priority 5: All of the perceptions have failed to result in an action so explore the environment
        //              This is done with the idea to increase our knowledge and potentially find a task
        int move = diagonalDirection;
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