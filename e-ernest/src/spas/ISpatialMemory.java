package spas;

import imos.IAct;

import java.util.ArrayList;

import javax.media.j3d.Transform3D;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import ernest.ITracer;

/**
 * A Spatial Memory is a memory of a spatial configuration
 * @author Olivier
 */
public interface ISpatialMemory 
{
	/**
	 * @return A clone of this spatial memory
	 */
	public ISpatialMemory clone();
	
	/**
	 * Simulates an act in spatial memory to check its consistency with the current state of spatial memory.
	 * TODO Create simulated phenomena to check for internal consistency of composite acts.
	 * @param act The act to simulate
	 * @param spas A reference to spas in order to access the bundles.
	 */
	public int runSimulation(IAct act, ISpas spas);	
	//public boolean simulate(IAct act, boolean doubt);
	
	/**
	 * Tick this spatial memory's clock (to compute decay)
	 */
	public void tick();
	
	/**
	 * Trace the content of this spatial memory
	 * @param tracer The tracer
	 */
	public void trace(ITracer tracer);
	
	/**
	 * Add a place in this spatial memory
	 * @param position This place's position
	 * @param type This place's type
	 * @return The created place
	 */
	public IPlace addPlace(Point3f position, int type);

	/**
	 * Apply a geometrical transformation caused by an act to this spatial memory
	 * @param act The act that generates the transformation
	 */
	public void transform(IAct act);
	
	/**
	 * Remove places that are older than the decay laps
	 */
	public void clear();

	/**
	 * @return The list of places in this spatial memory
	 */
	public ArrayList<IPlace> getPlaceList();
	
	/**
	 * Set the list of places 
	 * (used to clone spatial memory)
	 * @param places The list of places
	 */
	public void setPlaceList(ArrayList<IPlace> places);

	/**
	 * @return The value of a place for display and trace.
	 */
	public int getValue(Vector3f position);

	/**
	 * Compute a compresence of places in this spatial memory
	 * @param observation The observation 
	 * @param spas A reference to spas to create bundles
	 */
	public void copresence(ISpas spas);
	
	/**
	 * Delete the simulation places in this spatial memory
	 */
	public void clearSimulation();

	/**
	 * @param The transformation of spatial memory relatively to the agent.
	 */
	public void setTransform(Transform3D transform);
	
	/**
	 * @return The transformation of spatial memory relatively to the agent.
	 */
	public Transform3D getTransform();

	public void transform(Transform3D transform);
	
	/**
	 * Get the first place found at a given position
	 * @param position The position
	 * @return The place
	 */
	//public IPlace getPlace(Vector3f position);
	
}
