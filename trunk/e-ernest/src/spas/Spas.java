package spas;

import imos.IAct;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

import ernest.Ernest;
import ernest.ITracer;

/**
 * The spatial system.
 * Maintains the local space map and the persistence memory.
 * @author Olivier
 */
public class Spas implements ISpas 
{
	
	/** The Tracer. */
	private ITracer m_tracer = null; 

	/** Ernest's persistence momory  */
	private PersistenceMemory m_persistenceMemory = new PersistenceMemory();
	
	/** Ernest's local space memory  */
	private LocalSpaceMemory m_localSpaceMemory;
	
	/** The list of saliences generated by Ernest's sensory system  */
	List<IPlace> m_placeList = new ArrayList<IPlace>();

	int m_kinematicStimulation = Ernest.STIMULATION_KINEMATIC_FORWARD;
	int m_gustatoryStimulation = Ernest.STIMULATION_GUSTATORY_NOTHING;
	
	/** The color of attention for display in the environment.  */
	private int mAttention = 0;
	
	public void setTracer(ITracer tracer) 
	{
		m_tracer = tracer;
		m_persistenceMemory.setTracer(tracer);
		m_localSpaceMemory = new LocalSpaceMemory(m_persistenceMemory, m_tracer);
	}

	public IObservation step(IStimulation[] visualStimulations,
			IStimulation[] tactileStimulations, IStimulation kinematicStimulation,
			IStimulation gustatoryStimulation) 
	{
		// Tick the clock
		//m_persistenceMemory.tick();
		
		m_gustatoryStimulation = gustatoryStimulation.getValue();
		m_kinematicStimulation = kinematicStimulation.getValue();

		// Update the local space memory
		//m_localSpaceMemory.update(act, kinematicStimulation);
		
		// Construct the list of primitive bundles and places. 
		
		//List<IPlace> places;
		//places = getPrimitivePlaces(visualStimulations, tactileStimulations); // Places computed by Ernest.
		
		//m_localSpaceMemory.clearFront();
		
		m_localSpaceMemory.addVisualPlaces(visualStimulations);
		m_localSpaceMemory.addTactilePlaces(tactileStimulations);
		
		// Create new bundles and place them in the local space memory.
		
		m_localSpaceMemory.addKinematicPlace(m_kinematicStimulation);
		m_localSpaceMemory.addGustatoryPlace(m_gustatoryStimulation);
		
		// Clean up the local space memory according to the tactile simulations.
		
		adjustLocalSpaceMemory(tactileStimulations);
		
		// Find the most attractive or the most repulsive place in the list (abs value) (The list is never empty)
		
		int maxAttractiveness = 0;
		IPlace focusPlace = null;
		for (IPlace place : m_localSpaceMemory.getPlaceList())
		{
			place.setFocus(false);
			int attractiveness =  place.getAttractiveness(m_persistenceMemory.getClock());
			if (Math.abs(attractiveness) >= Math.abs(maxAttractiveness))
			{
				maxAttractiveness = attractiveness;
				focusPlace = place;
			}
		}

		focusPlace.setFocus(true);
		mAttention = focusPlace.getBundle().getVisualValue();

		//m_localSpaceMemory.clearBackground();
		
		// Trace the focus bundle and the local space memory.
//		if (m_tracer != null && focusPlace != null) 
//		{
//			Object e = m_tracer.addEventElement("focus");
//			m_tracer.addSubelement(e, "salience", getHexColor(mAttention));
//			focusPlace.getBundle().trace(m_tracer, "focus_bundle");
//			m_localSpaceMemory.Trace();
//		}
		
		// Return the new observation.
		
		IObservation observation = new Observation();
		observation.setGustatory(gustatoryStimulation);
		observation.setKinematic(kinematicStimulation);
		observation.setAttractiveness(maxAttractiveness);
		observation.setBundle(focusPlace.getBundle());
		observation.setPosition(focusPlace.getPosition());
		observation.setSpan(focusPlace.getSpan());
		
		return observation;
	}
	
	public IStimulation addStimulation(int type, int value) 
	{
		return m_persistenceMemory.addStimulation(type, value);
	}

	public int getValue(int i, int j)
	{
//		if (i == 1 && j == 0 && m_kinematicStimulation == Ernest.STIMULATION_KINEMATIC_BUMP)
//			return Ernest.STIMULATION_KINEMATIC_BUMP;
//		else if (i == 1 && j == 1 && m_gustatoryStimulation == Ernest.STIMULATION_GUSTATORY_FISH)
//			return Ernest.STIMULATION_GUSTATORY_FISH;
//		else
//		{
			Vector3f position = new Vector3f(1 - j, 1 - i, 0);
			if (m_localSpaceMemory != null)
				return m_localSpaceMemory.getValue(position);
			else
				return 0xFFFFFF;
//		}
	}

	public int getAttention()
	{
		return mAttention;
	}
	
	/**
	 * Set the list of saliences from the list provided by VacuumSG.
	 * @param salienceList The list of saliences provided by VacuumSG.
	 */
	public void setSalienceList(List<IPlace> placeList)
	{
		m_placeList = placeList;
	}
	
	
	/**
	 * Remove the bundles in local space memory that are not consistent with the tactile stimuli.
     * TODO The criteria to decide whether the matching is correct or incorrect need to be learned ! 
	 * @param tactileStimulations The tactile stimuli.
	 */
	private void adjustLocalSpaceMemory(IStimulation[] tactileStimulations)
	{

		// Check right
		IBundle bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_RIGHT);
		if (bundle != null && bundle.getTactileValue() != tactileStimulations[1].getValue())
			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_RIGHT);

		// Check ahead right
		bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_AHEAD_RIGHT);
		if (bundle != null && bundle.getTactileValue() != tactileStimulations[2].getValue())
			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_AHEAD_RIGHT);

		// Check ahead 
		bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_AHEAD);
		if (bundle != null && bundle.getTactileValue() != tactileStimulations[3].getValue())
			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_AHEAD);

		// Check ahead left
		bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_AHEAD_LEFT);
		if (bundle != null && bundle.getTactileValue() != tactileStimulations[4].getValue())
			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_AHEAD_LEFT);

		// Check left
		bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_LEFT);
		if (bundle != null && bundle.getTactileValue() != tactileStimulations[5].getValue())
			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_LEFT);

		// Check here
		bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_HERE);
		if (bundle != null && bundle.getTactileValue() != tactileStimulations[8].getValue())
			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_HERE);

		// Check behind left
		bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_BEHIND_LEFT);
		if (bundle != null && bundle.getTactileValue() != tactileStimulations[6].getValue())
			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_BEHIND_LEFT);

		// Check behind right
		bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_BEHIND_RIGHT);
		if (bundle != null && bundle.getTactileValue() != tactileStimulations[0].getValue())
			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_BEHIND_RIGHT);

		// Check behind
		bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_BEHIND);
		if (bundle != null && bundle.getTactileValue() != tactileStimulations[7].getValue())
			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_BEHIND);

	}

	public void update(Vector3f translation, float rotation) 
	{
		// Clear previous background
		//m_localSpaceMemory.clearBackground();

		m_localSpaceMemory.update(translation, rotation);
	}

	public ArrayList<IPlace> getPlaceList()
	{
		return m_localSpaceMemory.getPlaceList();
	}

	public void tick() 
	{
		m_persistenceMemory.tick();
	}

	public void traceLocalSpace() 
	{
		m_localSpaceMemory.Trace();
	}
}
