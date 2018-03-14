package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.Random;

import static uk.ac.nott.cs.g53dia.multidemo.sharedTankerMethods.*;

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

        //TODO: Search unless low on fuel, in which case refuel and return to the search point

        tankerXToUpdate += updateTankerXPos(diagonalDirection);
        tankerYToUpdate += updateTankerYPos(diagonalDirection);
        return new MoveAction(diagonalDirection);
    }
}
