package spas;

import imos.IAct;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Vector3f;

import utils.ErnestUtils;

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
	
	public static int PLACE_BACKGROUND = -1;
	public static int PLACE_SEE = 0;
	public static int PLACE_TOUCH = 1;
	public static int PLACE_FOCUS = 10;
	public static int PLACE_BUMP = 11;
	public static int PLACE_EAT  = 12;
	public static int PLACE_CUDDLE = 13;
	public static int PLACE_PRIMITIVE = 14;
	public static int PLACE_COMPOSITE = 15;
	public static int PLACE_INTERMEDIARY = 16;
	
	public static int SHAPE_CIRCLE = 0;
	public static int SHAPE_TRIANGLE = 1;
	public static int SHAPE_PIE = 2;
	public static int SHAPE_SQUARE = 3;

	/** Ernest's persistence momory  */
	private PersistenceMemory m_persistenceMemory = new PersistenceMemory();
	
	/** Ernest's local space memory  */
	private LocalSpaceMemory m_localSpaceMemory;
	
	/** The list of saliences generated by Ernest's sensory system  */
	List<IPlace> m_placeList = new ArrayList<IPlace>();
	
	//ArrayList<ISegment> m_segmentList = new ArrayList<ISegment>();

	IObservation m_observation;
	
	/** Temporary places.  */
	//ArrayList<IPlace> m_places = new ArrayList<IPlace>();
	
	/** The clock of the spatial system. (updated on each update cycle as opposed to IMOS) */
	private int m_clock;

	public void setTracer(ITracer tracer) 
	{
		m_tracer = tracer;
		m_persistenceMemory.setTracer(tracer);
		m_localSpaceMemory = new LocalSpaceMemory(this, m_tracer);
	}

	/**
	 * The main routine of the Spatial System that is called on each interaction cycle.
	 * Maintain the local space memory.
	 * Construct bundles and affordances.
	 * Maintain the current observation that is used by IMOS. 
	 * @param interactionPlace The place where the ongoing interaction started.
	 * @param observation The current observation.
	 */
	public void step(IObservation observation, ArrayList<IPlace> places) 
	{		
		//m_persistenceMemory.updateCount();
		//m_clock++;
		m_observation = observation;
		
		// translate and rotate the local space memory;
		
		Vector3f memoryTranslation = new Vector3f(observation.getTranslation());
		memoryTranslation.scale(-1);
		m_localSpaceMemory.update(memoryTranslation, - observation.getRotation());

		// refresh local space memory from visual and tactile data. 
		
		//m_places.clear();		
		//addSegmentPlaces(m_segmentList);
		//addTactilePlaces(observation.getTactileStimuli());
		m_localSpaceMemory.refresh(places, observation, m_clock);
		
		// Construct synergies associated with bundles in the peripersonal space.
		
		//synergy(interactionPlace, observation);
		//m_clock++;

	}
	
	public int getValue(int i, int j)
	{
		Vector3f position = new Vector3f(1 - j, 1 - i, 0);
		if (m_localSpaceMemory != null)
			return m_localSpaceMemory.getValue(position);
		else
			return 0xFFFFFF;
	}

	public int getAttention()
	{
		int attention;
		if (m_observation == null || m_observation.getFocusPlace() == null)
			attention = Ernest.UNANIMATED_COLOR;
		else
			attention = m_observation.getFocusPlace().getBundle().getValue();

		return attention;
	}
	
	/**
	 * Set the list of saliences from the list provided by VacuumSG.
	 * @param salienceList The list of saliences provided by VacuumSG.
	 */
	public void setPlaceList(List<IPlace> placeList)
	{
		m_placeList = placeList;
	}
		
	public ArrayList<IPlace> getPlaceList()
	{
		//return m_places;
		return m_localSpaceMemory.getPlaceList();
	}

//	public void count() 
//	{
//		// Update the decay counter in persistence memory
//		m_persistenceMemory.count();
//	}

	public void traceLocalSpace() 
	{
		m_localSpaceMemory.Trace();
	}

//	public void setSegmentList(ArrayList<ISegment> segmentList) 
//	{
//		m_segmentList = segmentList;
//	}
	
//	/**
//	 * Add places from segments provided by Vacuum_SG.
//	 * Create or recognize the associated bundle.
//	 * @param segmentList The list of segments.
//	 */
//	private void addSegmentPlaces(ArrayList<ISegment> segmentList)
//	{
//		for (ISegment segment : segmentList)
//		{
//			IBundle b = m_persistenceMemory.seeBundle(segment.getValue());
//			if (b == null)
//				//b = m_persistenceMemory.addBundle(segment.getValue(), Ernest.STIMULATION_TOUCH_EMPTY, Ernest.STIMULATION_KINEMATIC_FORWARD, Ernest.STIMULATION_GUSTATORY_NOTHING);
//				b = m_persistenceMemory.addBundle(segment.getValue(), Ernest.STIMULATION_TOUCH_EMPTY);
//			IPlace place = new Place(b,segment.getPosition());
//			place.setSpeed(segment.getSpeed());
//			place.setSpan(segment.getSpan());
//			//place.setFirstPosition(segment.getFirstPosition()); // First and second are in the trigonometric direction (counterclockwise). 
//			//place.setSecondPosition(segment.getSecondPosition());
//			place.setFirstPosition(segment.getSecondPosition()); 
//			place.setSecondPosition(segment.getFirstPosition());
//			if (segment.getRelativeOrientation() == Ernest.INFINITE)
//			{
//				Vector3f relativeOrientation = new Vector3f(segment.getFirstPosition());
//				relativeOrientation.sub(segment.getSecondPosition());
//				place.setOrientation(ErnestUtils.polarAngle(relativeOrientation));
//			}
//			else				
//				place.setOrientation(segment.getRelativeOrientation());
//			place.setUpdateCount(m_persistenceMemory.getUpdateCount());
//			// Long segments are processed only for display (background).
//			if (segment.getWidth() < 1.1f)
//				place.setType(Spas.PLACE_SEE);
//			else 
//				place.setType(Spas.PLACE_BACKGROUND);
//			m_places.add(place);			
//		}
//	}
//
//	/**
//	 * Add places in the peripersonal space associated with tactile bundles.
//	 * TODO Handle a tactile place behind the agent (last place connected to first place).
//	 * @param tactileStimulations The list of visual stimulations.
//	 */
//	private void addTactilePlaces(int[] tactileStimulations)
//	{
//
//		int tactileStimulation = tactileStimulations[0];
//		int span = 1;
//		float theta = - 3 * (float)Math.PI / 4; 
//		float sumDirection = theta;
//		float spanf = (float)Math.PI / 4;
//		
//		for (int i = 1 ; i <= 7; i++)
//		{
//			theta += (float)Math.PI / 4;
//			if ((i < 7) && tactileStimulations[i] == tactileStimulation)
//			{
//				// measure the salience span and average direction
//				span++;
//                sumDirection += theta;
//                spanf += (float)Math.PI / 4;
//			}
//			else 
//			{	
//				if (tactileStimulation != Ernest.STIMULATION_TOUCH_EMPTY)
//				{
//					// Create a tactile bundle.
//					float direction = sumDirection / span;
//					Vector3f position = new Vector3f((float)(Ernest.TACTILE_RADIUS * Math.cos((double)direction)), (float)(Ernest.TACTILE_RADIUS * Math.sin((double)direction)), 0f);
//					float firstDirection = direction - spanf/ 2;
//					Vector3f firstPosition = new Vector3f((float)(Ernest.TACTILE_RADIUS * Math.cos((double)firstDirection)), (float)(Ernest.TACTILE_RADIUS * Math.sin((double)firstDirection)), 0f);
//					float secondDirection = direction + spanf/ 2;
//					Vector3f secondPosition = new Vector3f((float)(Ernest.TACTILE_RADIUS * Math.cos((double)secondDirection)), (float)(Ernest.TACTILE_RADIUS * Math.sin((double)secondDirection)), 0f);
//					
//					// See in that direction ====
//					IPlace place = seePlace(direction);
//					
//					if (place == null)
//					{
//						// Nothing seen: create a tactile bundle and place it here.
//						//IBundle b = m_persistenceMemory.addBundle(Ernest.STIMULATION_VISUAL_UNSEEN, tactileStimulation, Ernest.STIMULATION_KINEMATIC_FORWARD, Ernest.STIMULATION_GUSTATORY_NOTHING);
//						IBundle b = m_persistenceMemory.addBundle(Ernest.STIMULATION_VISUAL_UNSEEN, tactileStimulation);
//						place = addOrReplacePlace(b, position);
//						place.setFirstPosition(firstPosition);
//						place.setSecondPosition(secondPosition);
//						place.setSpan(spanf);
//						place.setSpeed(new Vector3f(0,0,.01f)); // (Keeping the speed "null" generates errors in the Local Space Memory display).
//						place.setUpdateCount(m_persistenceMemory.getUpdateCount());
//						place.setType(Spas.PLACE_TOUCH);
//					}
//					else
//					{
//						if (place.getBundle().getTactileValue() == tactileStimulation )//&&
//							//place.getFrontDistance() < Ernest.TACTILE_RADIUS + .1f) // vision now provides distance
//						{
//							// A bundle is seen with the same tactile value: This is it!
//							place.getBundle().setLastTimeBundled(m_persistenceMemory.getClock());
//							// move the visual place to the tactile radius.
//							place.setPosition(position); // Position is more precise with tactile perception, especially for long walls.
//							place.setFirstPosition(firstPosition);
//							place.setSecondPosition(secondPosition);
//							place.setSpan(spanf);
//							place.setType(Spas.PLACE_TOUCH);
//							//place.setUpdateCount(m_persistenceMemory.getUpdateCount());
//						}
//						else if (place.getBundle().getTactileValue() == Ernest.STIMULATION_TOUCH_EMPTY )//&& 
//								//place.getFrontDistance() < Ernest.TACTILE_RADIUS + .1f)
//						{
//							// A bundle is seen in the same position with no tactile value.
//							
//							// Update the place and the bundle
//							//IBundle b = m_persistenceMemory.addBundle(place.getBundle().getVisualValue(), tactileStimulation, Ernest.STIMULATION_KINEMATIC_FORWARD, Ernest.STIMULATION_GUSTATORY_NOTHING);
//							IBundle b = m_persistenceMemory.addBundle(place.getBundle().getVisualValue(), tactileStimulation);
//							place.setBundle(b);
//							
//							//place.getBundle().setTactileValue(tactileStimulation.getValue());
//							//place.getBundle().setLastTimeBundled(m_persistenceMemory.getClock());
//							place.setPosition(position);							
//							place.setFirstPosition(firstPosition);
//							place.setSecondPosition(secondPosition);
//							place.setSpan(spanf);
//							place.setType(Spas.PLACE_TOUCH);
//							//place.setUpdateCount(m_persistenceMemory.getUpdateCount());
//						}
//					}
//				}
//				// look for the next bundle
//				if (i < 7)
//				{
//					tactileStimulation = tactileStimulations[i];
//					span = 1;
//					spanf = (float)Math.PI / 4;
//					sumDirection = theta;
//				}
//			}
//		}
//	}

//	/**
//	 * Find the closest place whose span overlaps this direction.
//	 * @param direction The direction in which to look at.
//	 * @return The place.
//	 */
//	public IPlace seePlace(float direction)
//	{
//		IPlace place = null;
//
//		for (IPlace p : m_places)
//		{
//			float firstAngle = ErnestUtils.polarAngle(p.getFirstPosition());
//			float secondAngle = ErnestUtils.polarAngle(p.getSecondPosition());
//			if (firstAngle < secondAngle)
//			{
//				// Does not overlap direction -PI
//				if (direction > firstAngle + 0.1f && direction < secondAngle - .05f && 
//					p.getBundle().getVisualValue() != Ernest.STIMULATION_VISUAL_UNSEEN &&
//					p.attractFocus(m_clock))
//						if (place == null || p.getDistance() < place.getDistance())
//							place = p;
//			}
//			else
//			{
//				// Overlaps direction -PI
//				if (direction > firstAngle + .1f || direction < secondAngle - .1f &&
//					p.getBundle().getVisualValue() != Ernest.STIMULATION_VISUAL_UNSEEN &&
//					p.attractFocus(m_clock))
//						if (place == null || p.getDistance() < place.getDistance())
//							place = p;				
//			}
//		}
//		return place;
//	}
//	
//	public IPlace addOrReplacePlace(IBundle bundle, Vector3f position)
//	{
//		// The initial position must be cloned so that 
//		// the position can be moved without changing the position used for intialization.
//		Vector3f pos = new Vector3f(position);
//		
//		IPlace p = new Place(bundle, pos);
//		p.setUpdateCount(m_clock);
//		
//		int i = m_places.indexOf(p);
//		if (i == -1)
//			// The place does not exist
//			m_places.add(p);
//		else 
//		{
//			// The place already exists: return a pointer to it.
//			p =  m_places.get(i);
//			p.setBundle(bundle);
//		}
//		return p;
//	}
	
//	/**
//	 * Associate affordances to the bundle ahead.
//	 * @param interactionPlace The place where the ongoing interaction started.
//	 * @param observation The current observation.
//	 */
//	private void synergy(IPlace interactionPlace, IObservation observation)
//	{
//		int synergyValue= observation.getGustatoryValue();
//		//if (observation.getKinematicValue() == Ernest.STIMULATION_KINEMATIC_BUMP)
//		//	synergyValue = observation.getKinematicValue();
//		if (synergyValue == Ernest.STIMULATION_GUSTATORY_NOTHING)
//			synergyValue = observation.getKinematicValue();
//		
//		//if (synergyValue != Ernest.STIMULATION_GUSTATORY_NOTHING)
//		//{
//			//IPlace focusPlace = observation.getFocusPlace();
//			IPlace focusPlace = getPlace(LocalSpaceMemory.DIRECTION_AHEAD);
//			IBundle focusBundle = null;
//			if (focusPlace != null) focusBundle = focusPlace.getBundle();
//	
//			if (focusBundle != null)
//			{
//				// Add the affordance to the bundle
//				Vector3f relativePosition = new Vector3f(interactionPlace.getPosition());
//				relativePosition.sub(new Vector3f(.4f, 0,0));
//				ErnestUtils.rotate(relativePosition, - focusPlace.getOrientation());
//				int attractiveness = Ernest.ATTRACTIVENESS_OF_UNKNOWN;
//				if (synergyValue == Ernest.STIMULATION_GUSTATORY_FISH) attractiveness = Ernest.ATTRACTIVENESS_OF_FISH;
//				if (synergyValue == Ernest.STIMULATION_GUSTATORY_CUDDLE) attractiveness = Ernest.ATTRACTIVENESS_OF_CUDDLE;
//				if (synergyValue == Ernest.STIMULATION_KINEMATIC_BUMP) attractiveness = Ernest.ATTRACTIVENESS_OF_BUMP;
//				focusBundle.addAffordance(observation.getPrimitiveAct(), interactionPlace, relativePosition, focusPlace.getOrientation(), attractiveness, synergyValue);
//			}
//			
//			if (synergyValue != Ernest.STIMULATION_GUSTATORY_NOTHING)
//			{
//				// Mark the synergy place.
//				Vector3f pos = new Vector3f(LocalSpaceMemory.DIRECTION_AHEAD);
//				pos.scale(Ernest.BOUNDING_RADIUS);
//				IPlace k = m_localSpaceMemory.addPlace(focusBundle, pos);
//				k.setFirstPosition(pos);
//				k.setSecondPosition(pos);
//				k.setType(Spas.PLACE_BUMP);
//				k.setUpdateCount(m_clock);
//			}
//	}
		
//	/**
//	 * Get the first place found at a given position.
//	 * @param position The position of the location.
//	 * @return The place.
//	 */
//	public IPlace getPlace(Vector3f position)
//	{
//		IPlace place = null;
//		for (IPlace p : m_places)
//		{
//			if (p.attractFocus(m_clock))
//			{
//				//if (p.isInCell(position) && p.attractFocus(m_persistenceMemory.getUpdateCount()))
//				Vector3f compare = new Vector3f(p.getPosition());
//				compare.sub(position);
//				if (compare.length() < 1f)
//					place = p;
//			}
//		}
//		return place;
//	}
//
//	/**
//	 * Clear a location in the local space memory.
//	 * @param position The position to clear.
//	 */
//	public void clearPlace(Vector3f position)
//	{
//		for (Iterator it = m_places.iterator(); it.hasNext();)
//		{
//			IPlace l = (IPlace)it.next();
//			if (l.isInCell(position))
//				it.remove();
//		}		
//	}
		
//	public void clear()
//	{
////		for (Iterator it = m_places.iterator(); it.hasNext();)
////		{
////			IPlace p = (IPlace)it.next();
////			if (p.getType() == Spas.PLACE_FOCUS) p.setType(Spas.PLACE_SEE);
////			if (p.getUpdateCount() < m_persistenceMemory.getUpdateCount() - 10)
////				it.remove();
////		}
//
//		m_places.clear();
//	}

	public IPlace getFocusPlace() 
	{
		return m_localSpaceMemory.getFocusPlace();
	}

	public IPlace addPlace(Vector3f position, int type, int shape) 
	{
		IPlace place = m_localSpaceMemory.addPlace(null, position);
		place.setFirstPosition(position);
		place.setSecondPosition(position);
		place.setType(type);
		place.setShape(shape);
		place.setUpdateCount(m_clock);
		
		return place;
	}

	public IBundle seeBundle(int value) 
	{
		return m_persistenceMemory.seeBundle(value);
	}

	public IBundle addBundle(int visualValue, int tactileValue) 
	{
		return m_persistenceMemory.addBundle(visualValue, tactileValue, m_clock);
	}

	public int getClock() 
	{
		return m_clock;
	}

	public IPlace addPlace(IBundle bundle, Vector3f position) 
	{
		return m_localSpaceMemory.addPlace(bundle, position);
	}

	public void tick() 
	{
		m_clock++;
	}	
}
