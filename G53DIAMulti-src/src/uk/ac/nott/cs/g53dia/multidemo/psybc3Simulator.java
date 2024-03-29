package uk.ac.nott.cs.g53dia.multidemo;
import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.Random;

public class psybc3Simulator {

    /**
     * Time for which execution pauses so that GUI can update.
     * Reducing this value causes the simulation to run faster.
     */
    private static int DELAY = 0;
	
    /**
     * Number of timesteps to execute
     */
    private static int DURATION = 10000;

    /*
        Commented out GUI components and added in system out read outs to allow for headless running of the experiments
     */

    public static void main(String[] args) {
        // Set the seed for reproducible behaviour
        // Random without seeding//Random r = new Random();
        Random r = new Random();

        // Create an environment
        Environment env = new Environment(Tanker.MAX_FUEL/2, r);

        //Create a fleet
//        Fleet fleet = new singleFleet(r);
        Fleet fleet = new multiFleet(r);

        // Create a GUI window to show the fleet
//        TankerViewer tv = new TankerViewer(fleet);
//        tv.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        // Start executing the tankers in the Fleet
        while (env.getTimestep() < DURATION) {
            // Advance the environment timestep
            env.tick();
            // Update the GUI
//            tv.tick(env);
            if( env.getTimestep() % 10 == 0 ) {
                System.out.println("Fleet Size " + ((multiFleet) fleet).getFleetSize() + ": " + env.getTimestep());
            }

            for (Tanker t:fleet) {
            	// Get the current view of the tanker
            	Cell[][] view = env.getView(t.getPosition(), Tanker.VIEW_RANGE);
            	// Let the tanker choose an action
            	Action a = t.senseAndAct(view, env.getTimestep());
            	// Try to execute the action
            	try {
            	    a.execute(env, t);
            	} catch (OutOfFuelException ofe) {
            	    System.err.println(ofe.getMessage() + " at timestep: " + env.getTimestep());
            	    System.exit(-1);
            	} catch (ActionFailedException afe) {
            	    System.err.println(afe.getMessage() + " at timestep: " + env.getTimestep());
            	}
            }
            try {
                Thread.sleep(DELAY);
            } catch (Exception e) { }
        }
        System.out.println("Final Score w/ Fleet Size " + ((multiFleet) fleet).getFleetSize() + " = " + fleet.getScore());
    }
}