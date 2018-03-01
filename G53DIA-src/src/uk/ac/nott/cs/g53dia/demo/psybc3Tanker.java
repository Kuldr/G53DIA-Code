package uk.ac.nott.cs.g53dia.demo;

import uk.ac.nott.cs.g53dia.library.*;

import java.util.Random;

/**
 * A simple example Tanker
 * 
 * @author Julian Zappala
 */
/*
 * Copyright (c) 2011 Julian Zappala
 * 
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
public class psybc3Tanker extends Tanker {

    private int size;

    private Cell[][] envRep;

    private int tankerX;
    private int tankerY;

    private int tankerXToUpdate;
    private int tankerYToUpdate;

    private int diagonalDirection = 4;

    public psybc3Tanker() {
	    this(new Random());
    }

    public psybc3Tanker(Random r) {
	    this.r = r;
	    this.tankerSetup();
    }

    /* This setup runs at the creation of the tanker so that it
     * can initialise any settings that it needs too.
     */
    private void tankerSetup(){
        //Get the size of the environment and create a view array of the whole environment
        size = 1000; // TODO: This is arbitrary as fuck //Tanker.MAX_FUEL/2; //TODO: 1. Can I pass find this info, 2 can I pass this in by modifying the simulator.
        envRep = new Cell[2*size+1][2*size+1];
        // Generate initial environment
        for (int x = -size; x <= size; x++) {
            for (int y = -size; y <= size; y++) {
                envRep[coordToEnvIndex(x)][coordToEnvIndex(y)] = null;
            }
        }

        //Initialise the tanker to the origin
        tankerX = 0;
        tankerY = 0;
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

        int tankerPosInView = view.length/2;
        for (int x = 0; x<view.length; x++) {
            for (int y = 0; y<view[x].length; y++) {
                int xCoord = coordToEnvIndex(viewIndexToCoord(x-tankerPosInView, tankerX));
                int yCoord = coordToEnvIndex(viewIndexToCoord(tankerPosInView-y, tankerY));
                if( xCoord < envRep.length && xCoord >= 0 && yCoord < envRep.length && yCoord >= 0 ) {
                    //If xCoord and yCoord are in the range update the view
                    envRep[xCoord][yCoord] = view[x][y];
                }
            }
        }

        distanceToEnvRep closestFuelPump = findClosestFuelPump();
        distanceToEnvRep closestStationWTask = findClosestTask();
        distanceToEnvRep closestWell = findClosestWell();

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
                                                                            closestFuelPump.envX, closestFuelPump.envY);
                                        // Check if there is a nearby station with a task, you have waste capacity
                                        // and you have enough fuel to get to the station and to the next fuel pump w/o running out
        boolean checkDisposeWaste       = getCurrentCell(view) instanceof Well
                                            && getWasteLevel() > 0;
                                        // Check if you are on a well and there is waste to get rid of
        boolean checkMoveToWell         = closestWell != null
                                            && getWasteLevel() > 0
                                            && !isPointFurtherThanFuel(closestWell.envX, closestWell.envY,
                                                                            closestFuelPump.envX, closestFuelPump.envY);
                                        // Check if there is a nearby well, you have waste to get rid of
                                        // and you have enough fuel to get to the well and to the next fuel pump w/o running out
        boolean checkStationCloserWell  = closestStationWTask != null
                                            && closestWell != null
                                            && closestStationWTask.distance <= closestWell.distance;
                                        // Check if the nearest station with a task is nearer than the nearest well
        boolean checkAtWasteCapacity    = getWasteCapacity() <= 0
                                            && closestWell != null;

        //Priority 1: Actions that require you to be on the tile at that time,
        //              unlikely to happen randomly but if relevant should be resolved as.
        //              These task typically will be invoked as we have moved towards these tiles recently to try and complete another task
        //              By completing these task it will "end" the inferred long term behaviour of some tasks
        //              Note: As they require to be on a specific tile there are no clashes in this tier that need to be resolved.
        //              If a priority 1 action is completed then set the diagonal for travel to a new diagonal
        if( checkRefuel ){
            diagonalDirection = newDiagonalDirection();
            return new RefuelAction();
        } else if ( checkCollectWaste ){
            diagonalDirection = newDiagonalDirection();
            return new LoadWasteAction(((Station) getCurrentCell(view)).getTask());
        } else if ( checkDisposeWaste ){
            diagonalDirection = newDiagonalDirection();
            return new DisposeWasteAction();
        }

        //Priority 2: Maintenance actions that need to be satisfied before other actions to avoid failure due to lack of resources
        if( checkMoveToFuelPump ){
            return directionToMoveTowards(closestFuelPump.envX, closestFuelPump.envY);
        }

        //Priority 3: Maintaining non critical resources but they need to be taken care of before any further actions can take place
        if( checkAtWasteCapacity ){
            return directionToMoveTowards(closestWell.envX, closestWell.envY);
        }

        //Priority 4: Long term goals to complete the overall task
        //              Only achievable if you have the required resources
        //              Clashes are resolved by going to the closest of them, with Stations winning draws as collecting waste gains points
        if( checkMoveToStationWTask && checkMoveToWell ){
            if( checkStationCloserWell ){
                return directionToMoveTowards(closestStationWTask.envX, closestStationWTask.envY);
            } else {
                return directionToMoveTowards(closestWell.envX, closestWell.envY);
            }
        } else if ( checkMoveToStationWTask ){
            return directionToMoveTowards(closestStationWTask.envX, closestStationWTask.envY);
        } else if ( checkMoveToWell ){
            return directionToMoveTowards(closestWell.envX, closestWell.envY);
        }

        //Priority 5: All of the perceptions have failed to result in an action so explore the environment
        //              This is done with the idea to increase our knowledge and potentially find a task
        int move = diagonalDirection;
        updateTankerPos(move);
        return new MoveAction(move);
    }

    private int coordToEnvIndex(int c){
        int zero = size;
        int index = zero + c;
        return index;
    }

    private int viewIndexToCoord(int disRelToTanker, int tankerPos){
        int coord = tankerPos + disRelToTanker;
        return coord;
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

    private int newDiagonalDirection(){
        //Note this can randomly generate the same diagonal as previous

        //Loop until chosen a diagonal
        while(true){
            int randInt = r.nextInt(8);
            switch (randInt) {
                case MoveAction.NORTH:
                    break;
                case MoveAction.SOUTH:
                    break;
                case MoveAction.EAST:
                    break;
                case MoveAction.WEST:
                    break;
                case MoveAction.NORTHEAST:
                case MoveAction.NORTHWEST:
                case MoveAction.SOUTHEAST:
                case MoveAction.SOUTHWEST:
                    //If its any of the diagonals return it
                    return randInt;
            }
        }
    }

    private Boolean isPointFurtherThanFuel(int envIndexX, int envIndexY, int envFuelX, int envFuelY){
        int distanceToPoint = distanceToPointFromCurrentPos(envIndexX, envIndexY);
        int distanceFromPointToFuel = distanceBetweenPoints(envIndexX, envIndexY, envFuelX, envFuelY);
        int totalDistance = distanceToPoint + distanceFromPointToFuel;
        if( getFuelLevel() >= totalDistance*1.0015+1 ){
            return false;
        }

        return true;
    }

    private int distanceToPointFromCurrentPos(int envIndexX, int envIndexY){
        return distanceBetweenPoints(envIndexX, envIndexY, coordToEnvIndex(tankerX), coordToEnvIndex(tankerY));
    }

    private int distanceBetweenPoints(int envIndexX1, int envIndexY1, int envIndexX2, int envIndexY2){
        int dx = Math.abs(envIndexX1 - envIndexX2);
        int dy = Math.abs(envIndexY1 - envIndexY2);

        if( dx >= dy ){
            return dx;
        } else {
            return dy;
        }
    }

    private Action directionToMoveTowards(int envIndexX, int envIndexY){
        int dx = envIndexX - coordToEnvIndex(tankerX);
        int dy = envIndexY - coordToEnvIndex(tankerY);

        int move = 0;

        if( dx < 0 && dy > 0 ){
            move = 5;
        } else if ( dx == 0 && dy > 0 ){
            move = 0;
        } else if ( dx > 0 && dy > 0 ){
            move = 4;
        } else if ( dx < 0 && dy == 0 ){
            move = 3;
        } else if ( dx > 0 && dy == 0 ){
            move = 2;
        } else if ( dx < 0 && dy < 0 ){
            move = 7;
        } else if ( dx == 0 && dy < 0 ){
            move = 1;
        } else if ( dx > 0 && dy < 0 ) {
            move = 6;
        }

        updateTankerPos(move);
        return new MoveAction(move);
    }

    private static class distanceToEnvRep{
        public distanceToEnvRep(int distance, int envX, int envY){
            this.distance = distance;
            this.envX = envX;
            this.envY = envY;
        }

        int distance;
        int envX;
        int envY;
    }

    private distanceToEnvRep findClosestFuelPump(){
        distanceToEnvRep closest = null;
        for(int x = 0; x < envRep.length; x++){
            for(int y = 0; y < envRep[x].length; y++) {
                if( envRep[x][y] instanceof FuelPump ){
                    if( closest == null ){
                        closest = new distanceToEnvRep(distanceToPointFromCurrentPos(x, y), x, y);
                    } else if ( closest.distance > distanceToPointFromCurrentPos(x, y) ){
                        closest = new distanceToEnvRep(distanceToPointFromCurrentPos(x, y), x, y);
                    }
                }
            }
        }
        return closest;
    }

    private distanceToEnvRep findClosestWell(){
        distanceToEnvRep closest = null;
        for(int x = 0; x < envRep.length; x++){
            for(int y = 0; y < envRep[x].length; y++) {
                if( envRep[x][y] instanceof Well ){
                    if( closest == null ){
                        closest = new distanceToEnvRep(distanceToPointFromCurrentPos(x, y), x, y);
                    } else if ( closest.distance > distanceToPointFromCurrentPos(x, y) ){
                        closest = new distanceToEnvRep(distanceToPointFromCurrentPos(x, y), x, y);
                    }
                }
            }
        }
        return closest;
    }

    private distanceToEnvRep findClosestTask() {
        distanceToEnvRep closest = null;
        for (int x = 0; x < envRep.length; x++) {
            for (int y = 0; y < envRep[x].length; y++) {
                if (envRep[x][y] instanceof Station) {
                    Station s = (Station) envRep[x][y];
                    if (s.getTask() != null) {
                        //Only want to find stations with a task
                        if( closest == null ){
                            closest = new distanceToEnvRep(distanceToPointFromCurrentPos(x, y), x, y);
                        } else if ( closest.distance > distanceToPointFromCurrentPos(x, y) ){
                            closest = new distanceToEnvRep(distanceToPointFromCurrentPos(x, y), x, y);
                        }
                    }

                }
            }
        }
        return closest;
    }

}

/*TODO: Work out how far away the fuel station is
*       If the fuel station is about to become to far away
*       (Distance to the fuel station * 0.0015 for failable actions)
*       Move towards the fuel station */
