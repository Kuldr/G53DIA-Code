
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
        size = Tanker.MAX_FUEL/2; //TODO: 1. Can I pass find this info, 2 can I pass this in by modifying the simulator.
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

        //DEBUG: Gives current tile //System.out.println(view[view.length/2][view.length/2]);
        //DEBUG: Gives the suspected location of the tanker //System.out.println("Suspected tanker position tankerX = " + tankerX + ", tankerY = " + tankerY);

        int tankerPosInView = view.length/2;

        for (int x = 0; x<view.length; x++) {
            for (int y = 0; y<view[x].length; y++) {
                int xCoord = viewIndexToCoord(x-tankerPosInView, tankerX);
                int yCoord = viewIndexToCoord(y-tankerPosInView, tankerY);
                envRep[coordToEnvIndex(xCoord)][coordToEnvIndex(yCoord)] = view[x][y];
            }
        }

        // If fuel tank is low and not at the fuel pump then move
    	// towards the fuel pump
        if ((getFuelLevel() <= MAX_FUEL/2) && !(getCurrentCell(view) instanceof FuelPump)) {
            System.out.println("Moving to fuel station");
            return new MoveTowardsAction(FUEL_PUMP_LOCATION); //TODO: I can't know the position of the tanker if I do this ??
        // If on a fuel pump and fuel tank is not full then refuel
        } else if( getCurrentCell(view) instanceof FuelPump && getFuelLevel() < MAX_FUEL ) {
            System.out.println("Refueling");
            return new RefuelAction();
        // Otherwise, move randomly
        } else {
            System.out.println("Random Movement");
            int move = r.nextInt(8);
            updateTankerPos(move); //TODO: IF AN ACTION FAILS THEN THIS WILL BE WRONG
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
                tankerY++;
                break;
            case MoveAction.SOUTH:
                tankerY--;
                break;
            case MoveAction.EAST:
                tankerX++;
                break;
            case MoveAction.WEST:
                tankerX--;
                break;
            case MoveAction.NORTHEAST:
                tankerX++; tankerY++;
                break;
            case MoveAction.NORTHWEST:
                tankerX--; tankerY++;
                break;
            case MoveAction.SOUTHEAST:
                tankerX++; tankerY--;
                break;
            case MoveAction.SOUTHWEST:
                tankerX--; tankerY--;
                break;
        }
    }

    private int distanceToPoint(){
        return 0;
    }

}

/*TODO: Work out how far away the fuel station is
*       If the fuel station is about to become to far away
*       (Distance to the fuel station * 0.0015 for failable actions)
*       Move towards the fuel station */
