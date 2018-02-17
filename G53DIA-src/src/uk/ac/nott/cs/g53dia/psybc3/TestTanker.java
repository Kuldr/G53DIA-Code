package uk.ac.nott.cs.g53dia.psybc3;

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
public class TestTanker extends Tanker {

    public TestTanker() {
	this(new Random());
    }

    public TestTanker(Random r) {
	this.r = r;
    }

    /*
     * The following is a simple demonstration of how to write a
     * tanker. The code below is very stupid and simply moves the
     * tanker randomly until the fuel tank is half full, at which
     * point it returns to a fuel pump to refuel.
     */
    public Action senseAndAct(Cell[][] view, long timestep) {

        // If fuel tank is low and not at the fuel pump then move
    	// towards the fuel pump
        if ((getFuelLevel() <= MAX_FUEL/2) && !(getCurrentCell(view) instanceof FuelPump)) {
            //System.out.println("Moving to fuel station");
            return new MoveTowardsAction(FUEL_PUMP_LOCATION);
        // If on a fuel pump and fuel tank is not full then refuel
        } else if( getCurrentCell(view) instanceof FuelPump && getFuelLevel() < MAX_FUEL ) {
            //System.out.println("Refueling");
            return new RefuelAction();
        // Otherwise, move randomly
        } else {
            //System.out.println("Random Movement");
            return new MoveAction(r.nextInt(8));
        }
    }

}

/*TODO: Work out how far away the fuel station is
*       If the fuel station is about to become to far away
*       (Distance to the fuel station * 0.0015 for failable actions)
*       Move towards the fuel station */
