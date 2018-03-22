package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.Random;

public class sharedTankerMethods {

    public static int coordToEnvIndex(int c, int size){
        int zero = size;
        int index = zero + c;
        return index;
    }

    public static int viewIndexToCoord(int disRelToTanker, int tankerPos){
        int coord = tankerPos + disRelToTanker;
        return coord;
    }

    public static int newDiagonalDirection(Random r){
        //Note this can randomly generate the same diagonal as previous

        //Loop until chosen a diagonal
        while(true){
            int randInt = r.nextInt(8);
            switch (randInt) {
                case MoveAction.NORTH:
                case MoveAction.SOUTH:
                case MoveAction.EAST:
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

    public static Boolean isPointFurtherThanFuel(int envIndexX, int envIndexY, int envFuelX, int envFuelY, int tankerX, int tankerY, int fuelLevel, int size){
        int distanceToPoint = distanceToPointFromCurrentPos(envIndexX, envIndexY, tankerX, tankerY, size);
        int distanceFromPointToFuel = distanceBetweenPoints(envIndexX, envIndexY, envFuelX, envFuelY);
        int totalDistance = distanceToPoint + distanceFromPointToFuel;
        //if( fuelLevel >= Math.ceil(totalDistance*1.0015+1) ) { TODO: Possibly change to this to reduce fails, would have to rerun
        if( fuelLevel >= totalDistance*1.0015+1 ) {
            return false;
        }

        return true;
    }

    public static int distanceToPointFromCurrentPos(int envIndexX, int envIndexY, int tankerX, int tankerY, int size){
        return distanceBetweenPoints(envIndexX, envIndexY, coordToEnvIndex(tankerX, size), coordToEnvIndex(tankerY, size));
    }

    private static int distanceBetweenPoints(int envIndexX1, int envIndexY1, int envIndexX2, int envIndexY2){
        int dx = Math.abs(envIndexX1 - envIndexX2);
        int dy = Math.abs(envIndexY1 - envIndexY2);

        if( dx >= dy ){
            return dx;
        } else {
            return dy;
        }
    }

    public static int directionToMoveTowards(int envIndexX, int envIndexY, int tankerX, int tankerY, int size){
        int dx = envIndexX - coordToEnvIndex(tankerX, size);
        int dy = envIndexY - coordToEnvIndex(tankerY, size);

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

        return move;
    }

    public static class distanceToEnvRep{
        public distanceToEnvRep(int distance, int envX, int envY){
            this.distance = distance;
            this.envX = envX;
            this.envY = envY;
        }

        int distance;
        int envX;
        int envY;
    }

    //Although these 3 methods are similar as instanceof requires a compile time reference
    // and you can't create cells to pass as objects to test against they have to be separate
    public static distanceToEnvRep findClosestFuelPump(Cell[][] envRep, int tankerX, int tankerY, int size){
        distanceToEnvRep closest = null;
        for(int x = 0; x < envRep.length; x++){
            for(int y = 0; y < envRep[x].length; y++) {
                if( envRep[x][y] instanceof FuelPump){
                    if( closest == null ){
                        closest = new distanceToEnvRep(distanceToPointFromCurrentPos(x, y, tankerX, tankerY, size), x, y);
                    } else if ( closest.distance > distanceToPointFromCurrentPos(x, y, tankerX, tankerY, size) ){
                        closest = new distanceToEnvRep(distanceToPointFromCurrentPos(x, y, tankerX, tankerY, size), x, y);
                    }
                }
            }
        }
        return closest;
    }

    public static distanceToEnvRep findClosestWell(Cell[][] envRep, int tankerX, int tankerY, int size){
        distanceToEnvRep closest = null;
        for(int x = 0; x < envRep.length; x++){
            for(int y = 0; y < envRep[x].length; y++) {
                if( envRep[x][y] instanceof Well){
                    if( closest == null ){
                        closest = new distanceToEnvRep(distanceToPointFromCurrentPos(x, y, tankerX, tankerY, size), x, y);
                    } else if ( closest.distance > distanceToPointFromCurrentPos(x, y, tankerX, tankerY, size) ){
                        closest = new distanceToEnvRep(distanceToPointFromCurrentPos(x, y, tankerX, tankerY, size), x, y);
                    }
                }
            }
        }
        return closest;
    }

    public static distanceToEnvRep findClosestTask(Cell[][] envRep, int tankerX, int tankerY, int size) {
        distanceToEnvRep closest = null;
        for (int x = 0; x < envRep.length; x++) {
            for (int y = 0; y < envRep[x].length; y++) {
                if (envRep[x][y] instanceof Station) {
                    Station s = (Station) envRep[x][y];
                    if (s.getTask() != null) {
                        //Only want to find stations with a task
                        if( closest == null ){
                            closest = new distanceToEnvRep(distanceToPointFromCurrentPos(x, y, tankerX, tankerY, size), x, y);
                        } else if ( closest.distance > distanceToPointFromCurrentPos(x, y, tankerX, tankerY, size) ){
                            closest = new distanceToEnvRep(distanceToPointFromCurrentPos(x, y, tankerX, tankerY, size), x, y);
                        }
                    }

                }
            }
        }
        return closest;
    }

    public static int updateTankerXPos(int moveDirection){
        switch (moveDirection) {
            case MoveAction.EAST:
            case MoveAction.NORTHEAST:
            case MoveAction.SOUTHEAST:
                return 1;
            case MoveAction.WEST:
            case MoveAction.NORTHWEST:
            case MoveAction.SOUTHWEST:
                return -1;
            default:
                return 0; //Move action is either invalid so won't move or X doesn't change
        }
    }

    public static int updateTankerYPos(int moveDirection){
        switch (moveDirection) {
            case MoveAction.NORTH:
            case MoveAction.NORTHEAST:
            case MoveAction.NORTHWEST:
                return 1;
            case MoveAction.SOUTH:
            case MoveAction.SOUTHEAST:
            case MoveAction.SOUTHWEST:
                return -1;
            default:
                return 0; //Move action is either invalid so won't move or Y doesn't change
        }
    }

    public static Cell[][] updateEnvRep(Cell[][] envRep, Cell[][] view, int tankerX, int tankerY, int size){
        int tankerPosInView = view.length/2;
        for (int x = 0; x<view.length; x++) {
            for (int y = 0; y<view[x].length; y++) {
                int xCoord = coordToEnvIndex(viewIndexToCoord(x-tankerPosInView, tankerX), size);
                int yCoord = coordToEnvIndex(viewIndexToCoord(tankerPosInView-y, tankerY), size);
                if( xCoord < envRep.length && xCoord >= 0 && yCoord < envRep.length && yCoord >= 0 && envRep[xCoord][yCoord] == null || envRep[xCoord][yCoord] instanceof Station ) {
                    //If xCoord and yCoord are in the range update the view
                    envRep[xCoord][yCoord] = view[x][y];
                }
            }
        }
        return envRep;
    }

    public static Cell[][] envRepSetup(int size) {
        //Create a view array of the whole environment
        Cell[][] envRep = new Cell[2*size+1][2*size+1];
        // Generate initial environment
        for (int x = -size; x <= size; x++) {
            for (int y = -size; y <= size; y++) {
                envRep[coordToEnvIndex(x, size)][coordToEnvIndex(y, size)] = null;
            }
        }
        return envRep;
    }

    public static boolean checkMoveToFuelPump(distanceToEnvRep closestFuelPump, int fuelLevel, Cell currentCell) {
        // Check if there is enough fuel to get to the nearest fuel pump so long as you are not on a fuel pump
        return closestFuelPump != null
                && fuelLevel <= Math.ceil(closestFuelPump.distance*1.0015+1)
                && !(currentCell instanceof FuelPump);
    }

    public static boolean checkRefuel(int fuelLevel, Cell currentCell) {
        // Check if you are on a fuel pump and you have less than max fuel
        return currentCell instanceof FuelPump
                && fuelLevel < Tanker.MAX_FUEL;
    }

    public static boolean checkCollectWaste(int wasteCapacity, Cell currentCell) {
        // Check if you are on a station, the station has a task and you have capacity to store the waste
        return currentCell instanceof Station
                && ((Station) currentCell).getTask() != null
                && wasteCapacity > 0;
    }

    public static boolean checkMoveToStationWTask(distanceToEnvRep closestStationWTask, int wasteCapacity, distanceToEnvRep closestFuelPump, int tankerX, int tankerY, int fuelLevel, int size) {
        // Check if there is a nearby station with a task, you have waste capacity
        // and you have enough fuel to get to the station and to the next fuel pump w/o running out
        return closestStationWTask != null
                && wasteCapacity > 0
                && !isPointFurtherThanFuel(closestStationWTask.envX, closestStationWTask.envY,
                closestFuelPump.envX, closestFuelPump.envY,
                tankerX, tankerY, fuelLevel, size);
    }

    public static boolean checkDisposeWaste(Cell currentCell, int wasteLevel) {
        // Check if you are on a well and there is waste to get rid of
        return currentCell instanceof Well
                && wasteLevel > 0;
    }

    public static boolean checkMoveToWell(distanceToEnvRep closestWell, int wasteLevel, distanceToEnvRep closestFuelPump, int tankerX, int tankerY, int fuelLevel, int size ) {
        // Check if there is a nearby well, you have waste to get rid of
        // and you have enough fuel to get to the well and to the next fuel pump w/o running out
        return closestWell != null
                && wasteLevel > 0
                && !isPointFurtherThanFuel(closestWell.envX, closestWell.envY,
                closestFuelPump.envX, closestFuelPump.envY,
                tankerX, tankerY, fuelLevel, size);
    }

    public static boolean checkStationCloserWell(distanceToEnvRep closestStationWTask, distanceToEnvRep closestWell) {
        // Check if the nearest station with a task is nearer than the nearest well
        return closestStationWTask != null
                && closestWell != null
                && closestStationWTask.distance <= closestWell.distance;
    }

    public static boolean checkAtWasteCapacity(int wasteCapacity, distanceToEnvRep closestWell, distanceToEnvRep closestFuelPump, int tankerX, int tankerY, int fuelLevel, int size) {
        // Check if at max waste capacity and then aim to get rid of it if possible
        return wasteCapacity <= 0
                && closestWell != null
                && !isPointFurtherThanFuel(closestWell.envX, closestWell.envY,
                                            closestFuelPump.envX, closestFuelPump.envY,
                                            tankerX, tankerY, fuelLevel, size);
    }

    public static boolean checkCapacityIsGETask(distanceToEnvRep closestStationWTask, int wasteCapacity, Cell[][] envRep) {
        // Check if there is more waste at the station than there is waste capacity
        // As the task will never have more than the max waste capacity you can't have stations that are forever ignored
        return closestStationWTask != null
                && wasteCapacity >= ((Station) envRep[closestStationWTask.envX][closestStationWTask.envY]).getTask().getWasteRemaining();
    }
}
