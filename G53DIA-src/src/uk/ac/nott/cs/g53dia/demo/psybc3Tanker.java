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

        // If fuel tank is lower than the distance to the fuel station and not at the fuel pump then move towards the fuel pump
        if( getFuelLevel() <= Math.ceil(closestFuelPump.distance*1.0015+1) && !(getCurrentCell(view) instanceof FuelPump) ){
            //Need to move towards the fuel station now
            System.out.println("Moving towards fuel station");
            int move = directionToMoveTowards(closestFuelPump.envX, closestFuelPump.envY);
            updateTankerPos(move);
            return new MoveAction(move);
        // If on a fuel pump and fuel tank is not full then refuel
        } else if( getCurrentCell(view) instanceof FuelPump && getFuelLevel() < MAX_FUEL ) {
            System.out.println("Refueling");
            return new RefuelAction();
        // If on a well and have waste dispose of it
        } else if ( getCurrentCell(view) instanceof Well && getWasteLevel() > 0 ) {
            return new DisposeWasteAction();
        // If there is a known well and we have waste to dispose of go to it
        } else if ( closestWell != null && getWasteLevel() > 0 ) {
            int move = directionToMoveTowards(closestWell.envX, closestWell.envY);
            updateTankerPos(move);
            return new MoveAction(move);
        // If on a station with a task and we have spare waste capacity
        } else if( getCurrentCell(view) instanceof Station && ((Station) getCurrentCell(view)).getTask() != null && getWasteCapacity() > 0) {
            return new LoadWasteAction(((Station) getCurrentCell(view)).getTask());
        // If there is a known station with a task go to it and we have spare waste capacity
        } else if( closestStationWTask != null && getWasteCapacity() > 0) {
            int move = directionToMoveTowards(closestStationWTask.envX, closestStationWTask.envY);
            updateTankerPos(move);
            return new MoveAction(move);
        // Otherwise, move diagonally
        } else {
            System.out.println("Diagonal Movement");
            int move = 4;//r.nextInt(8);
            updateTankerPos(move);
            return new MoveAction(move);
        }
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

    private int distanceToPoint(int envIndexX, int envIndexY){
        int dx = Math.abs(envIndexX - coordToEnvIndex(tankerX));
        int dy = Math.abs(envIndexY - coordToEnvIndex(tankerY));

        if( dx >= dy ){
            return dx;
        } else {
            return dy;
        }
    }

    private int directionToMoveTowards(int envIndexX, int envIndexY){
        int dx = envIndexX - coordToEnvIndex(tankerX);
        int dy = envIndexY - coordToEnvIndex(tankerY);

        if( dx < 0 && dy > 0 ){
            return 5;
        } else if ( dx == 0 && dy > 0 ){
            return 0;
        } else if ( dx > 0 && dy > 0 ){
            return 4;
        } else if ( dx < 0 && dy == 0 ){
            return 3;
        } else if ( dx > 0 && dy == 0 ){
            return 2;
        } else if ( dx < 0 && dy < 0 ){
            return 7;
        } else if ( dx == 0 && dy < 0 ){
            return 1;
        } else if ( dx > 0 && dy < 0 ) {
            return 6;
        }

        //Can't reach here its to suppress warning
        return 0;
    }

    private static class distanceToEnvRep{
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
                        closest = new distanceToEnvRep();
                        closest.distance = distanceToPoint(x, y);
                        closest.envX = x;
                        closest.envY = y;
                    } else if ( closest.distance > distanceToPoint(x, y) ){
                        closest.distance = distanceToPoint(x, y);
                        closest.envX = x;
                        closest.envY = y;
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
                        closest = new distanceToEnvRep();
                        closest.distance = distanceToPoint(x, y);
                        closest.envX = x;
                        closest.envY = y;
                    } else if ( closest.distance > distanceToPoint(x, y) ){
                        closest.distance = distanceToPoint(x, y);
                        closest.envX = x;
                        closest.envY = y;
                    }
                }
            }
        }
        return closest;
    }

    private distanceToEnvRep findClosestTask(){
        distanceToEnvRep closest = null;
        for(int x = 0; x < envRep.length; x++){
            for(int y = 0; y < envRep[x].length; y++) {
                if( envRep[x][y] instanceof Station ){
                    Station s = (Station) envRep[x][y];
                    if( s.getTask() != null ){
                        //Only want to find stations with a task
                        System.out.println("THERE IS A TASK");
                        if( closest == null ){
                            closest = new distanceToEnvRep();
                            closest.distance = distanceToPoint(x, y);
                            closest.envX = x;
                            closest.envY = y;
                        } else if ( closest.distance > distanceToPoint(x, y) ){
                            closest.distance = distanceToPoint(x, y);
                            closest.envX = x;
                            closest.envY = y;
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
