package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.Random;

import static uk.ac.nott.cs.g53dia.multidemo.sharedTankerMethods.*;

public class psybc3TankerSingle extends Tanker {

    private int size;

    private Cell[][] envRep;

    private int tankerX;
    private int tankerY;

    private int tankerXToUpdate;
    private int tankerYToUpdate;

    private int diagonalDirection = 4;

    public psybc3TankerSingle() {
	    this(new Random());
    }

    public psybc3TankerSingle(Random r) {
	    this.r = r;
	    this.tankerSetup();
    }

    /* This setup runs at the creation of the tanker so that it
     * can initialise any settings that it needs too.
     */
    private void tankerSetup(){

        //Initialise the shared view of the environment
        size = 1000; //This is based upon the number of timesteps
        envRep = envRepSetup(size);

        //Initialise the tanker to the origin
        tankerX = 0;
        tankerY = 0;

        diagonalDirection = newDiagonalDirection(r);
    }

    /*
     * The following is a simple demonstration of how to write a
     * tanker. The code below is very stupid and simply moves the
     * tanker randomly until the fuel tank is half full, at which
     * point it returns to a fuel pump to refuel.
     */
    public Action senseAndAct(Cell[][] view, long timestep) {

        if( !actionFailed ) {
            // Action passed update the system
            tankerX = tankerXToUpdate;
            tankerY = tankerYToUpdate;
        }

        //Reset the update variables
        tankerXToUpdate = tankerX;
        tankerYToUpdate = tankerY;

        envRep = updateEnvRep(envRep, view, tankerX, tankerY, size);

        distanceToEnvRep closestFuelPump = findClosestFuelPump(envRep, tankerX, tankerY, size);
        distanceToEnvRep closestStationWTask = findClosestTask(envRep, tankerX, tankerY, size);
        distanceToEnvRep closestWell = findClosestWell(envRep, tankerX, tankerY, size);

        //Evaluate each situation
        boolean checkMoveToFuelPump     = closestFuelPump != null
                                            && getFuelLevel() <= Math.ceil(closestFuelPump.distance*1.0015+1)
                                            && !(getCurrentCell(view) instanceof FuelPump);
                                        // Check if there is enough fuel to get to the nearest fuel pump so long as you are not on a fuel pump
        boolean checkRefuel             = getCurrentCell(view) instanceof FuelPump
                                            && getFuelLevel() < MAX_FUEL;
                                        // Check if you are on a fuel pump and you have less than max fuel
        boolean checkCollectWaste       = getCurrentCell(view) instanceof Station
                                            && ((Station) getCurrentCell(view)).getTask() != null
                                            && getWasteCapacity() > 0;
                                        // Check if you are on a station, the station has a task and you have capacity to store the waste
        boolean checkMoveToStationWTask = closestStationWTask != null
                                            && getWasteCapacity() > 0
                                            && !isPointFurtherThanFuel(closestStationWTask.envX, closestStationWTask.envY,
                                                                            closestFuelPump.envX, closestFuelPump.envY,
                                                                            tankerX, tankerY, getFuelLevel(), size);
                                        // Check if there is a nearby station with a task, you have waste capacity
                                        // and you have enough fuel to get to the station and to the next fuel pump w/o running out
        boolean checkDisposeWaste       = getCurrentCell(view) instanceof Well
                                            && getWasteLevel() > 0;
                                        // Check if you are on a well and there is waste to get rid of
        boolean checkMoveToWell         = closestWell != null
                                            && getWasteLevel() > 0
                                            && !isPointFurtherThanFuel(closestWell.envX, closestWell.envY,
                                                                            closestFuelPump.envX, closestFuelPump.envY,
                                                                            tankerX, tankerY, getFuelLevel(), size);
                                        // Check if there is a nearby well, you have waste to get rid of
                                        // and you have enough fuel to get to the well and to the next fuel pump w/o running out
        boolean checkStationCloserWell  = closestStationWTask != null
                                            && closestWell != null
                                            && closestStationWTask.distance <= closestWell.distance;
                                        // Check if the nearest station with a task is nearer than the nearest well
        boolean checkAtWasteCapacity    = getWasteCapacity() <= 0
                                            && closestWell != null
                                            && !isPointFurtherThanFuel(closestWell.envX, closestWell.envY,
                                                                            closestFuelPump.envX, closestFuelPump.envY,
                                                                            tankerX, tankerY, getFuelLevel(), size);
                                        // Check if at max waste capacity and then aim to get rid of it if possible
        boolean checkCapacityIsGETask   = closestStationWTask != null
                                            && getWasteCapacity() >= ((Station) envRep[closestStationWTask.envX][closestStationWTask.envY]).getTask().getWasteRemaining();
                                        // Check if there is more waste at the station than there is waste capacity
                                        // As the task will never have more than the max waste capacity you can't have stations that are forever ignored

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
            int move = directionToMoveTowards(closestFuelPump.envX, closestFuelPump.envY, tankerX, tankerY, size);
            tankerXToUpdate += updateTankerXPos(move);
            tankerYToUpdate += updateTankerYPos(move);
            return new MoveAction(move);
        }

        //Priority 3: Maintaining non critical resources but they need to be taken care of before any further actions can take place
        if( checkAtWasteCapacity ){
            int move = directionToMoveTowards(closestWell.envX, closestWell.envY, tankerX, tankerY, size);
            tankerXToUpdate += updateTankerXPos(move);
            tankerYToUpdate += updateTankerYPos(move);
            return new MoveAction(move);
        }

        //Priority 4: Long term goals to complete the overall task
        //              Only achievable if you have the required resources
        //              Clashes are resolved by going to the closest of them, with Stations winning draws as collecting waste gains points
        if( !checkCapacityIsGETask && checkMoveToWell ){
            int move = directionToMoveTowards(closestWell.envX, closestWell.envY, tankerX, tankerY, size);
            tankerXToUpdate += updateTankerXPos(move);
            tankerYToUpdate += updateTankerYPos(move);
            return new MoveAction(move);
        }
        if( checkMoveToStationWTask && checkMoveToWell ){
            if( checkStationCloserWell ){
                int move = directionToMoveTowards(closestStationWTask.envX, closestStationWTask.envY, tankerX, tankerY, size);
                tankerXToUpdate += updateTankerXPos(move);
                tankerYToUpdate += updateTankerYPos(move);
                return new MoveAction(move);
            } else {
                int move = directionToMoveTowards(closestWell.envX, closestWell.envY, tankerX, tankerY, size);
                tankerXToUpdate += updateTankerXPos(move);
                tankerYToUpdate += updateTankerYPos(move);
                return new MoveAction(move);
            }
        } else if ( checkMoveToStationWTask ){
            int move = directionToMoveTowards(closestStationWTask.envX, closestStationWTask.envY, tankerX, tankerY, size);
            tankerXToUpdate += updateTankerXPos(move);
            tankerYToUpdate += updateTankerYPos(move);
            return new MoveAction(move);
        } else if ( checkMoveToWell ){
            int move = directionToMoveTowards(closestWell.envX, closestWell.envY, tankerX, tankerY, size);
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