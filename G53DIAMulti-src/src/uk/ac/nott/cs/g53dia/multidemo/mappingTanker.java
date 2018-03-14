package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.Random;

public class mappingTanker extends Tanker {

    private int diagonalDirection;
    private multiFleet fleet;

    private int tankerX;
    private int tankerY;

    private int tankerXToUpdate;
    private int tankerYToUpdate;

    public mappingTanker(int diagonalDirection, multiFleet fleet) {
        this(new Random(), diagonalDirection, fleet);
    }

    public mappingTanker(Random r, int diagonalDirection, multiFleet fleet) {
        this.r = r;
        this.diagonalDirection = diagonalDirection;
        this.fleet = fleet;
        this.tankerSetup();
    }

    private void tankerSetup() {
        //Initialise the tanker to the origin
        tankerX = 0;
        tankerY = 0;

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
        fleet.updateEnvRep(view, tankerX, tankerY);

        updateTankerPos(diagonalDirection);
        return new MoveAction(diagonalDirection);
    }

    private void updateTankerPos(int moveDirection){
        switch (moveDirection) {
            case MoveAction.NORTH:
                tankerYToUpdate++;
                break;
            case MoveAction.SOUTH:
                tankerYToUpdate--;
                break;
            case MoveAction.EAST:
                tankerXToUpdate++;
                break;
            case MoveAction.WEST:
                tankerXToUpdate--;
                break;
            case MoveAction.NORTHEAST:
                tankerXToUpdate++; tankerYToUpdate++;
                break;
            case MoveAction.NORTHWEST:
                tankerXToUpdate--; tankerYToUpdate++;
                break;
            case MoveAction.SOUTHEAST:
                tankerXToUpdate++; tankerYToUpdate--;
                break;
            case MoveAction.SOUTHWEST:
                tankerXToUpdate--; tankerYToUpdate--;
                break;
        }
    }
}
