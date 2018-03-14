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

        //TODO: If low on fuel need to refuel and return to the search point


        //TODO: I THINK I MESSED UP THE DIRECTIONS :(
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
                return NORTHWEST;
            default:
                return NORTHWEST;
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
                return tankerX+VIEW_RANGE; //Move outside the current view
            case(NORTHWEST):
            case(SOUTHWEST):
                return tankerX-VIEW_RANGE; //Move outside the current view
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
                return tankerY+VIEW_RANGE;  //Move outside the current view
            case(SOUTHWEST):
            case(SOUTHEAST):
                return tankerY-VIEW_RANGE;  //Move outside the current view
            default:
                return tankerY; //Move action is either invalid so won't move or Y doesn't change
        }
    }
}

//State: Diagonal -> Straight -> Diagonal
//Diagonal is move towards current X, Y + VIEW_RANGE (size of view)
//Straight is move towards current X, Y but one of them is now -ive
