package spas;

import imos2.IEnaction;
import ernest.Action;
import ernest.Phenomenon;
import ernest.ITracer;
import ernest.Layout;
import ernest.Observation;

/**
 * 
 * @author Olivier
 */
public interface Simu {
	
	/**
	 * Updates the simulator according to the last enacted act
	 * @param act The last enacted act
	 */
	public void track(IEnaction enaction);
	
	/**
	 * Predict the observation resulting from an action.
	 * @param action The action to simulate
	 * @return the predicted observation
	 */
	public Observation predict(Action action);
	
	/**
	 * Trace the current state of the simulator
	 * @param tracer The tracer
	 */
	public void trace(ITracer tracer);

}
