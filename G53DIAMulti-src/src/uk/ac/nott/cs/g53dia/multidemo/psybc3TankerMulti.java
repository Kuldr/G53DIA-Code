package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.Random;

import static uk.ac.nott.cs.g53dia.multidemo.sharedTankerMethods.*;

//Same overall design as the single tanker from CW1 with 2 changes to allow for it to work better in a multiagent system
    //First of all it uses the fleet to have a single shared representation of the environment, by having all tankers
        //share information it means that each individual can make better informed decisions
    //Second is that the tankers will get tasks from the fleet to avoid 2 tankers getting the same task, this prevents
        //a situation where 2 tankers try and collect the same waste and one failing to do so
        //Note: Tasks are given out based upon which tanker asked first, not which is closest, this could be optimised
            //further but it works well enough in the current form
public class psybc3TankerMulti extends Tanker {

    private multiFleet fleet;

    private int tankerX;
    private int tankerY;

    private int tankerXToUpdate;
    private int tankerYToUpdate;

    private int diagonalDirection;

    public psybc3TankerMulti(multiFleet fleet) {
	    this(new Random(), fleet);
    }

    public psybc3TankerMulti(Random r, multiFleet fleet) {
	    this.r = r;
        this.fleet = fleet;
	    this.tankerSetup();
    }

    /* This setup runs at the creation of the tanker so that it
     * can initialise any settings that it needs too.
     */
    private void tankerSetup(){
        //Initialise the tanker to the origin
        tankerX = 0;
        tankerY = 0;

        diagonalDirection = newDiagonalDirection(r);
    }

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
        fleet.resetTaskList(timestep);

        distanceToEnvRep closestFuelPump = findClosestFuelPump(fleet.getEnvRep(), tankerX, tankerY, fleet.getSize());
        distanceToEnvRep closestStationWTask = fleet.findClosestTaskNotGiven(tankerX, tankerY);
        distanceToEnvRep closestWell = findClosestWell(fleet.getEnvRep(), tankerX, tankerY, fleet.getSize());

        //Evaluate each situation
        boolean checkMoveToFuelPump     = checkMoveToFuelPump(closestFuelPump, getFuelLevel(), getCurrentCell(view));
        boolean checkRefuel             = checkRefuel(getFuelLevel(), getCurrentCell(view));
        boolean checkCollectWaste       = checkCollectWaste(getWasteCapacity(), getCurrentCell(view));
        boolean checkMoveToStationWTask = checkMoveToStationWTask(closestStationWTask, getWasteCapacity(), closestFuelPump, tankerX, tankerY, getFuelLevel(), fleet.getSize());
        boolean checkDisposeWaste       = checkDisposeWaste(getCurrentCell(view), getWasteLevel());
        boolean checkMoveToWell         = checkMoveToWell(closestWell, getWasteLevel(), closestFuelPump, tankerX, tankerY, getFuelLevel(), fleet.getSize());
        boolean checkStationCloserWell  = checkStationCloserWell(closestStationWTask, closestWell);
        boolean checkAtWasteCapacity    = checkAtWasteCapacity(getWasteCapacity(), closestWell, closestFuelPump, tankerX, tankerY, getFuelLevel(), fleet.getSize());
        boolean checkCapacityIsGETask   = checkCapacityIsGETask(closestStationWTask, getWasteCapacity(), fleet.getEnvRep());

        //Priority 1: Actions that require you to be on the tile at that time,
        //              unlikely to happen randomly but if relevant should be resolved as.
        //              These task typically will be invoked as we have moved towards these tiles recently to try and complete another task
        //              By completing these task it will "end" the inferred long term behaviour of some tasks
        //              Note: As they require to be on a specific tile there are no clashes in this tier that need to be resolved.
        //              If a priority 1 action is completed then set the diagonal for travel to a new diagonal
        if( checkRefuel ){
            diagonalDirection = newDiagonalDirection(r);
            return new RefuelAction();
        } else if ( checkCollectWaste ){
            diagonalDirection = newDiagonalDirection(r);
            return new LoadWasteAction(((Station) getCurrentCell(view)).getTask());
        } else if ( checkDisposeWaste ){
            diagonalDirection = newDiagonalDirection(r);
            return new DisposeWasteAction();
        }

        //Priority 2: Maintenance actions that need to be satisfied before other actions to avoid failure due to lack of resources
        if( checkMoveToFuelPump ){
            int move = directionToMoveTowards(closestFuelPump.envX, closestFuelPump.envY, tankerX, tankerY, fleet.getSize());
            tankerXToUpdate += updateTankerXPos(move);
            tankerYToUpdate += updateTankerYPos(move);
            return new MoveAction(move);
        }

        //Priority 3: Maintaining non critical resources but they need to be taken care of before any further actions can take place
        if( checkAtWasteCapacity ){
            int move = directionToMoveTowards(closestWell.envX, closestWell.envY, tankerX, tankerY, fleet.getSize());
            tankerXToUpdate += updateTankerXPos(move);
            tankerYToUpdate += updateTankerYPos(move);
            return new MoveAction(move);
        }

        //Priority 4: Long term goals to complete the overall task
        //              Only achievable if you have the required resources
        //              Clashes are resolved by going to the closest of them, with Stations winning draws as collecting waste gains points
        if( !checkCapacityIsGETask && checkMoveToWell ){
            int move = directionToMoveTowards(closestWell.envX, closestWell.envY, tankerX, tankerY, fleet.getSize());
            tankerXToUpdate += updateTankerXPos(move);
            tankerYToUpdate += updateTankerYPos(move);
            return new MoveAction(move);
        }
        if( checkMoveToStationWTask && checkMoveToWell ){
            if( checkStationCloserWell ){
                int move = directionToMoveTowards(closestStationWTask.envX, closestStationWTask.envY, tankerX, tankerY, fleet.getSize());
                tankerXToUpdate += updateTankerXPos(move);
                tankerYToUpdate += updateTankerYPos(move);
                return new MoveAction(move);
            } else {
                int move = directionToMoveTowards(closestWell.envX, closestWell.envY, tankerX, tankerY, fleet.getSize());
                tankerXToUpdate += updateTankerXPos(move);
                tankerYToUpdate += updateTankerYPos(move);
                return new MoveAction(move);
            }
        } else if ( checkMoveToStationWTask ){
            int move = directionToMoveTowards(closestStationWTask.envX, closestStationWTask.envY, tankerX, tankerY, fleet.getSize());
            tankerXToUpdate += updateTankerXPos(move);
            tankerYToUpdate += updateTankerYPos(move);
            return new MoveAction(move);
        } else if ( checkMoveToWell ){
            int move = directionToMoveTowards(closestWell.envX, closestWell.envY, tankerX, tankerY, fleet.getSize());
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
}