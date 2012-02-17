package spas;


import imos.IAct;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import utils.ErnestUtils;

import ernest.Ernest;
import ernest.ITracer;

/**
 * The local space structure maintains an awareness of the bundle surrounding Ernest. 
 * @author Olivier
 */
public class LocalSpaceMemory 
{
	
	/** The radius of a location. */
	public static float LOCATION_RADIUS = 0.5f;
	public static float LOCAL_SPACE_MEMORY_RADIUS = 20f;//4f;
	public static float DISTANCE_VISUAL_BACKGROUND = 10f;
	public static float EXTRAPERSONAL_DISTANCE = 1.5f;
	
	/** The Local space structure. */
	private ArrayList<IPlace> m_places = new ArrayList<IPlace>();
	
	IPlace m_focusPlace = null;

	/** The persistence memory. */
	PersistenceMemory m_persistenceMemory;
	
	/** The tracer. */
	ITracer m_tracer;
	
	public final static float DIAG2D_PROJ = (float) (1/Math.sqrt(2));

	public final static Vector3f DIRECTION_HERE         = new Vector3f(0, 0, 0);
	public final static Vector3f DIRECTION_AHEAD        = new Vector3f(1, 0, 0);
	public final static Vector3f DIRECTION_BEHIND       = new Vector3f(-1, 0, 0);
	public final static Vector3f DIRECTION_LEFT         = new Vector3f(0, 1, 0);
	public final static Vector3f DIRECTION_RIGHT        = new Vector3f(0, -1, 0);
	public final static Vector3f DIRECTION_AHEAD_LEFT   = new Vector3f(DIAG2D_PROJ, DIAG2D_PROJ, 0);
	public final static Vector3f DIRECTION_AHEAD_RIGHT  = new Vector3f(DIAG2D_PROJ, -DIAG2D_PROJ, 0);
	public final static Vector3f DIRECTION_BEHIND_LEFT  = new Vector3f(-DIAG2D_PROJ, DIAG2D_PROJ, 0);
	public final static Vector3f DIRECTION_BEHIND_RIGHT = new Vector3f(-DIAG2D_PROJ, -DIAG2D_PROJ, 0);	
	public final static float    SOMATO_RADIUS = 1f;
	
	LocalSpaceMemory(PersistenceMemory persistenceMemory, ITracer tracer)
	{
		m_persistenceMemory = persistenceMemory;
		m_tracer = tracer;
	}

	public void Trace()
	{
		if (m_tracer != null && !m_places.isEmpty())
		{
			Object localSpace = m_tracer.addEventElement("local_space");
			m_tracer.addSubelement(localSpace, "position_8", getHexColor(DIRECTION_HERE));
			m_tracer.addSubelement(localSpace, "position_7", getHexColor(DIRECTION_BEHIND));
			m_tracer.addSubelement(localSpace, "position_6", getHexColor(DIRECTION_BEHIND_LEFT));
			m_tracer.addSubelement(localSpace, "position_5", getHexColor(DIRECTION_LEFT));
			m_tracer.addSubelement(localSpace, "position_4", getHexColor(DIRECTION_AHEAD_LEFT));
			m_tracer.addSubelement(localSpace, "position_3", getHexColor(DIRECTION_AHEAD));
			m_tracer.addSubelement(localSpace, "position_2", getHexColor(DIRECTION_AHEAD_RIGHT));
			m_tracer.addSubelement(localSpace, "position_1", getHexColor(DIRECTION_RIGHT));
			m_tracer.addSubelement(localSpace, "position_0", getHexColor(DIRECTION_BEHIND_RIGHT));
		}
	}
	
	/**
	 * Add places in the background associated with visual bundles.
	 * @param visualStimulations The list of visual stimulations.
	 */
	public void addVisualPlaces(IStimulation[] visualStimulations)
	{
		IStimulation stimulation = visualStimulations[0];
		int span = 1;
		float theta = - 11 * (float)Math.PI / 24; 
		float sumDirection = theta;
		float spanf = (float)Math.PI / 12;
		float sumDistance = visualStimulations[0].getPosition().length();
		for (int i = 1 ; i <= Ernest.RESOLUTION_RETINA; i++)
		{
			theta += (float)Math.PI / 12;
			if ((i < Ernest.RESOLUTION_RETINA) && visualStimulations[i].equals(stimulation))
			{
				// measure the salience span and average direction
				span++;
                sumDirection += theta;
                spanf += (float)Math.PI / 12;
                sumDistance += visualStimulations[i].getPosition().length();
			}
			else 
			{	
				// Create or recognize a visual bundle.
				IBundle b = m_persistenceMemory.seeBundle(stimulation.getValue());
				if (b == null)
					b = m_persistenceMemory.addBundle(stimulation.getValue(), Ernest.STIMULATION_TOUCH_EMPTY, Ernest.STIMULATION_KINEMATIC_FORWARD, Ernest.STIMULATION_GUSTATORY_NOTHING);
				// Record the place in the visual background.
				//IPlace place = new Place(b,DISTANCE_VISUAL_BACKGROUND, sumDirection / span, spanf);
				IPlace place = new Place(b,sumDistance / span, sumDirection / span, spanf);
				m_places.add(place);
				// look for the next bundle
				
				if (i < Ernest.RESOLUTION_RETINA)
				{
					stimulation = visualStimulations[i];
					span = 1;
					spanf = (float)Math.PI / 12;
					sumDirection = theta;
					sumDistance = visualStimulations[i].getPosition().length();
				}
			}
		}
	}
	
	/**
	 * Add places from segments provided by Vacuum_SG.
	 * Create or recognize the associated bundle.
	 * @param segmentList The list of segments.
	 */
	public void addSegmentPlaces(ArrayList<ISegment> segmentList)
	{
		for (ISegment segment : segmentList)
		{
//			if (segment.getWidth() < 1)
//			{
				// Short segments are seen as segments.
				IBundle b = m_persistenceMemory.seeBundle(segment.getValue());
				if (b == null)
					b = m_persistenceMemory.addBundle(segment.getValue(), Ernest.STIMULATION_TOUCH_EMPTY, Ernest.STIMULATION_KINEMATIC_FORWARD, Ernest.STIMULATION_GUSTATORY_NOTHING);
				IPlace place = new Place(b,segment.getPosition());
				place.setSpeed(segment.getSpeed());
				place.setSpan(segment.getSpan());
				place.setFirstPosition(segment.getSecondPosition()); // somehow inverted
				place.setSecondPosition(segment.getFirstPosition());
				place.setUpdateCount(m_persistenceMemory.getUpdateCount());
				if (segment.getWidth() < 1)
					place.setType(Spas.PLACE_SEE);
				else
					place.setType(Spas.PLACE_BACKGROUND);
				m_places.add(place);			
//			}
//			else
//			{
//				// Long segments are seen as two points.
//				IBundle b = m_persistenceMemory.seeBundle(segment.getValue());
//				if (b == null)
//					b = m_persistenceMemory.addBundle(segment.getValue(), Ernest.STIMULATION_TOUCH_EMPTY, Ernest.STIMULATION_KINEMATIC_FORWARD, Ernest.STIMULATION_GUSTATORY_NOTHING);
//				IPlace place = new Place(b,segment.getPosition());
//				place.setSpeed(segment.getSpeed());
//				place.setSpan(segment.getSpan());
//				place.setFirstPosition(segment.getFirstPosition()); // somehow inverted
//				Vector3f firstWall = new Vector3f(segment.getSecondPosition());
//				firstWall.sub(segment.getFirstPosition());
//				firstWall.normalize();
//				firstWall.scale(.3f);
//				firstWall.add(segment.getFirstPosition());
//				place.setSecondPosition(firstWall);
//				place.setUpdateCount(m_persistenceMemory.getUpdateCount());
//				place.setType(Spas.PLACE_SEE);
//				m_places.add(place);			
//
//				IPlace s = new Place(b,segment.getPosition());
//				s.setSpeed(segment.getSpeed());
//				s.setSpan(segment.getSpan());
//				s.setFirstPosition(segment.getSecondPosition()); // somehow inverted
//				Vector3f secondWall = new Vector3f(segment.getFirstPosition());
//				secondWall.sub(segment.getSecondPosition());
//				secondWall.normalize();
//				secondWall.scale(.3f);
//				secondWall.add(segment.getSecondPosition());
//				s.setSecondPosition(secondWall);
//				s.setUpdateCount(m_persistenceMemory.getUpdateCount());
//				s.setType(Spas.PLACE_BACKGROUND);
//				m_places.add(s);			
//			}
		}
	}

	/**
	 * Add places in the peripersonal space associated with tactile bundles.
	 * @param tactileStimulations The list of visual stimulations.
	 */
	public void addTactilePlaces(IStimulation[] tactileStimulations)
	{

		IStimulation tactileStimulation = tactileStimulations[0];
		int span = 1;
		float theta = - 3 * (float)Math.PI / 4; 
		float sumDirection = theta;
		float spanf = (float)Math.PI / 4;
		
		for (int i = 1 ; i <= 7; i++)
		{
			theta += (float)Math.PI / 4;
			if ((i < 7) && tactileStimulations[i].equals(tactileStimulation))
			{
				// measure the salience span and average direction
				span++;
                sumDirection += theta;
                spanf += (float)Math.PI / 4;
			}
			else 
			{	
				if (tactileStimulation.getValue() != Ernest.STIMULATION_TOUCH_EMPTY)
				{
					// Create a tactile bundle.
					float direction = sumDirection / span;
					Vector3f position = new Vector3f((float)(Ernest.TACTILE_RADIUS * Math.cos((double)direction)), (float)(Ernest.TACTILE_RADIUS * Math.sin((double)direction)), 0f);
					float firstDirection = direction - spanf/ 2;
					Vector3f firstPosition = new Vector3f((float)(Ernest.TACTILE_RADIUS * Math.cos((double)firstDirection)), (float)(Ernest.TACTILE_RADIUS * Math.sin((double)firstDirection)), 0f);
					float secondDirection = direction + spanf/ 2;
					Vector3f secondPosition = new Vector3f((float)(Ernest.TACTILE_RADIUS * Math.cos((double)secondDirection)), (float)(Ernest.TACTILE_RADIUS * Math.sin((double)secondDirection)), 0f);
					// See in that direction.
					IPlace place = seePlace(direction);
					if (place == null)
					{
						// Nothing seen: create a tactile bundle and place it here.
						IBundle b = m_persistenceMemory.addBundle(Ernest.STIMULATION_VISUAL_UNSEEN, tactileStimulation.getValue(), Ernest.STIMULATION_KINEMATIC_FORWARD, Ernest.STIMULATION_GUSTATORY_NOTHING);
						place = addOrReplacePlace(b, position);
						place.setFirstPosition(firstPosition);
						place.setSecondPosition(secondPosition);
						place.setSpan(spanf);
						place.setSpeed(new Vector3f(0,0,1)); // (Keeping the speed "null" generates errors in the Local Space Memory display).
						place.setUpdateCount(m_persistenceMemory.getUpdateCount());
						if (place.getType() == Spas.PLACE_SEE)
							place.setType(Spas.PLACE_TOUCH);
					}
					else
					{
						if (place.getBundle().getTactileValue() == tactileStimulation.getValue() &&
							place.getDistance() < Ernest.TACTILE_RADIUS + .1f) // vision now provides distance
						{
							// A bundle is seen with the same tactile value: This is it!
							place.getBundle().setLastTimeBundled(m_persistenceMemory.getClock());
							// move the visual place to the tactile radius.
							place.setPosition(position); // Position is more precise with tactile perception, especially for long walls.
							place.setFirstPosition(firstPosition);
							place.setSecondPosition(secondPosition);
							place.setSpan(spanf);
							if (place.getType() == Spas.PLACE_SEE)
								place.setType(Spas.PLACE_TOUCH);
							//place.setUpdateCount(m_persistenceMemory.getUpdateCount());
						}
						else if (place.getBundle().getTactileValue() == Ernest.STIMULATION_TOUCH_EMPTY && 
								place.getDistance() < Ernest.TACTILE_RADIUS + .1f)
						{
							// A bundle is seen in the same position with no tactile value.
							
							// Update the place and the bundle
							IBundle b = m_persistenceMemory.addBundle(place.getBundle().getVisualValue(), tactileStimulation.getValue(), Ernest.STIMULATION_KINEMATIC_FORWARD, Ernest.STIMULATION_GUSTATORY_NOTHING);
							place.setBundle(b);
							
							//place.getBundle().setTactileValue(tactileStimulation.getValue());
							//place.getBundle().setLastTimeBundled(m_persistenceMemory.getClock());
							place.setPosition(position);							
							place.setFirstPosition(firstPosition);
							place.setSecondPosition(secondPosition);
							place.setSpan(spanf);
							if (place.getType() == Spas.PLACE_SEE)
								place.setType(Spas.PLACE_TOUCH);
							//place.setUpdateCount(m_persistenceMemory.getUpdateCount());
						}
					}
				}
				// look for the next bundle
				if (i < 7)
				{
					tactileStimulation = tactileStimulations[i];
					span = 1;
					spanf = (float)Math.PI / 4;
					sumDirection = theta;
				}
			}
		}
	}
	
	public void addKinematicPlace(int kinematicValue)
	{
		// Find the place in front of Ernest.
		IPlace frontPlace = null;
		for (IPlace place : m_places)
			if (place.isFrontal() && place.getBundle().getVisualValue() != Ernest.STIMULATION_VISUAL_UNSEEN && place.getSpan() > Math.PI/6 + 0.01f && place.attractFocus(m_persistenceMemory.getUpdateCount()))
				frontPlace = place;
		
		// Associate kinematic stimulation to the front bundle.

		if (kinematicValue == Ernest.STIMULATION_KINEMATIC_BUMP)
		{
			if (frontPlace != null)
			{
				// Add bump interaction to the bundle at this place.
				//frontPlace.setType(Spas.PLACE_KINEMATIC);
				m_persistenceMemory.addKinematicValue(frontPlace.getBundle(), kinematicValue);
				
				// Add a Bump place.
				Vector3f pos = new Vector3f(LocalSpaceMemory.DIRECTION_AHEAD);
				pos.scale(Ernest.BOUNDING_RADIUS);
				IPlace k = new Place(frontPlace.getBundle(), pos);
				k.setType(Spas.PLACE_BUMP);
				k.setFirstPosition(pos);
				k.setSecondPosition(pos);
				k.setUpdateCount(m_persistenceMemory.getUpdateCount());
				m_places.add(k);
			}
		}
	}
	
	public void addGustatoryPlace(int gustatoryValue)
	{
		IPlace frontPlace = getPlace(LocalSpaceMemory.DIRECTION_AHEAD);
		IBundle frontBundle = null;
		if (frontPlace != null) frontBundle = frontPlace.getBundle();
		IPlace herePlace = getPlace(LocalSpaceMemory.DIRECTION_HERE);
		IBundle hereBundle = null;
		if (herePlace != null) hereBundle = herePlace.getBundle();

		// Associate the tactile stimulation with the fish gustatory stimulation
		
		if (gustatoryValue == Ernest.STIMULATION_GUSTATORY_FISH)
		{
			// Discrete environment. The fish bundle is the hereBundle.
			if (hereBundle != null)
			{
				// Add eat interaction to the bundle at this place
				if (hereBundle.getTactileValue() == Ernest.STIMULATION_TOUCH_FISH)
				{
					m_persistenceMemory.addGustatoryValue(hereBundle, gustatoryValue);
					clearPlace(LocalSpaceMemory.DIRECTION_HERE); // The fish is eaten
				}
			}
			
			// Continuous environment. The fish bundle is the frontBundle
			if (frontBundle != null)
			{
				// Add eat interaction to the bundle at this place
				if (frontBundle.getTactileValue() == Ernest.STIMULATION_TOUCH_FISH)
				{
					m_persistenceMemory.addGustatoryValue(frontBundle, gustatoryValue);
					clearPlace(LocalSpaceMemory.DIRECTION_AHEAD); // The fish is eaten
				}
			}
			// Add an eat place.
			Vector3f pos = new Vector3f(LocalSpaceMemory.DIRECTION_AHEAD);
			pos.scale(Ernest.BOUNDING_RADIUS);
			IPlace k = new Place(hereBundle, pos);
			k.setFirstPosition(pos);
			k.setSecondPosition(pos);
			k.setType(Spas.PLACE_EAT);
			k.setUpdateCount(m_persistenceMemory.getUpdateCount());
			m_places.add(k);
		}
		
		// Associate the tactile stimulation with the cuddle stimulation
		
		if (gustatoryValue == Ernest.STIMULATION_GUSTATORY_CUDDLE)
		{
			if (frontBundle != null)
			{
				//frontPlace.setType(Spas.PLACE_CUDDLE);

				// Add cuddle interaction to the bundle at this place
				if (frontBundle.getTactileValue() == Ernest.STIMULATION_TOUCH_AGENT)
					m_persistenceMemory.addGustatoryValue(frontBundle, gustatoryValue);

			}
			// Add a cuddle place.
			Vector3f pos = new Vector3f(LocalSpaceMemory.DIRECTION_AHEAD);
			pos.scale(Ernest.BOUNDING_RADIUS);
			IPlace k = new Place(hereBundle, pos);
			k.setType(Spas.PLACE_CUDDLE);
			k.setFirstPosition(pos);
			k.setSecondPosition(pos);
			k.setUpdateCount(m_persistenceMemory.getUpdateCount());
			m_places.add(k);
		}		
	}
	
	/**
	 * Add a new place to the local space memory if it does not yet exist.
	 * Replace the bundle if it already exists.
	 * @param bundle The bundle in this location.
	 * @param distance The distance of this place.
	 * @param direction The direction of this place.
	 * @return The new or already existing location.
	 */
//	public IPlace addPlace(IBundle bundle, float distance, float direction)
//	{
//		Vector3f position = new Vector3f((float)(distance * Math.cos((double)direction)), (float)(distance * Math.sin((double)direction)), 0f);
//		return addPlace(bundle, position);
//	}
	
	public IPlace addPlace(IBundle bundle, Vector3f position)
	{
		// The initial position must be cloned so that 
		// the position can be moved without changing the position used for intialization.
		Vector3f p = new Vector3f(position);
		
		IPlace place = new Place(bundle, p);
		
		m_places.add(place);
		return place;
	}
	
	public IPlace addOrReplacePlace(IBundle bundle, Vector3f position)
	{
		// The initial position must be cloned so that 
		// the position can be moved without changing the position used for intialization.
		Vector3f pos = new Vector3f(position);
		
		IPlace p = new Place(bundle, pos);
		p.setUpdateCount(m_persistenceMemory.getUpdateCount());
		
		int i = m_places.indexOf(p);
		if (i == -1)
			// The place does not exist
			m_places.add(p);
		else 
		{
			// The place already exists: return a pointer to it.
			p =  m_places.get(i);
			p.setBundle(bundle);
		}
		return p;
	}
	
	/**
	 * Update the local space memory according to the enacted interaction.
	 * @param act The enacted act.
	 * @param kinematicStimulation The kinematic stimulation.
	 */
	public void update(IAct act, IStimulation kinematicStimulation)
	{
		if (act != null)
		{
			if (act.getSchema().getLabel().equals(">") && kinematicStimulation.getValue() != Ernest.STIMULATION_KINEMATIC_BUMP)
				translate(new Vector3f(-1f, 0,0));
			else if (act.getSchema().getLabel().equals("^"))
				rotate(- (float)Math.PI / 4);
			else if (act.getSchema().getLabel().equals("v"))
				rotate((float)Math.PI / 4);
		}
	}
	
	/**
	 * Update the local space memory according to the agent's moves.
	 * @param translation The translation value (provide the opposite value from the agent's movement).
	 * @param rotation The rotation value (provide the opposite value from the agent's movement).
	 */
	public void update(Vector3f translation, float rotation)
	{
		translate(translation);
		rotate(rotation);
	}
	
	/**
	 * Rotate the local space of the given angle.
	 * @param angle The angle (provide the opposite angle from the agent's movement).
	 */
	private void rotate(float angle)
	{
		for (IPlace l : m_places)
		{
			l.rotate(angle);
		}		
	}

	/**
	 * Translate the local space of the given distance.
	 * Remove locations that are left behind.
	 * @param distance The distance (provide the opposite value from the agent's movement).
	 */
	private void translate(Vector3f translation)
	{
		for (IPlace p : m_places)
			p.translate(translation);
			
		for (Iterator it = m_places.iterator(); it.hasNext();)
		{
			IPlace l = (IPlace)it.next();
			if (l.getPosition().length() > LOCAL_SPACE_MEMORY_RADIUS)
			//if (l.getPosition().x < - LOCAL_SPACE_MEMORY_RADIUS)
				it.remove();
		}		
	}
	
	/**
	 * Find the closest place whose span overlaps this direction.
	 * @param direction The direction in which to look at.
	 * @return The place.
	 */
	private IPlace seePlace(float direction)
	{
		IPlace place = null;

		for (IPlace p : m_places)
		{
			if (p.getDirection() - p.getSpan() / 2 < direction - Math.PI/12 + 0.1 && 
				p.getDirection() + p.getSpan() / 2 > direction + Math.PI/12 - 0.1 &&
				p.getBundle().getVisualValue() != Ernest.STIMULATION_VISUAL_UNSEEN &&
				p.attractFocus(m_persistenceMemory.getUpdateCount()))
				if (place == null || p.getDistance() < place.getDistance())
					place = p;
		}
		return place;
	}
	
	/**
	 * Get the bundle at a given position.
	 * @param position The position of the location.
	 * @return The bundle.
	 */
	public IBundle getBundle(Vector3f position)
	{
		IBundle b = null;
		for (IPlace p : m_places)
		{
			if (p.isInCell(position) && p.attractFocus( m_persistenceMemory.getUpdateCount()))
				b = p.getBundle();
		}	

		return b;
	}

	/**
	 * Get the first place found at a given position.
	 * @param position The position of the location.
	 * @return The place.
	 */
	public IPlace getPlace(Vector3f position)
	{
		IPlace place = null;
		for (IPlace p : m_places)
		{
			if (p.isInCell(position) && p.attractFocus(m_persistenceMemory.getUpdateCount()))
				place = p;
		}
		return place;
	}

	/**
	 * Clear a location in the local space memory.
	 * @param position The position to clear.
	 */
	public void clearPlace(Vector3f position)
	{
		for (Iterator it = m_places.iterator(); it.hasNext();)
		{
			IPlace l = (IPlace)it.next();
			if (l.isInCell(position))
				it.remove();
		}		
	}
	
	/**
	 * Clear the places in the visual background.
	 * @param position The position to clear.
	 */
	public void clearBackground()
	{
		for (Iterator it = m_places.iterator(); it.hasNext();)
		{
			IPlace l = (IPlace)it.next();
			if (l.getDistance() > DISTANCE_VISUAL_BACKGROUND - 1)
				it.remove();
		}
	}
	
	/**
	 * Clear the places in front (but not below Ernest) 
	 * (will be replaced by new seen places).
	 * @param position The position to clear.
	 */
	public void clearFront()
	{
		for (Iterator it = m_places.iterator(); it.hasNext();)
		{
			IPlace l = (IPlace)it.next();
			if (l.getDirection() > - Math.PI/2 && l.getDirection() < Math.PI/2 &&
				l.getDistance() > 1)
				it.remove();
		}
	}
	
	public void clear()
	{
		for (Iterator it = m_places.iterator(); it.hasNext();)
		{
			IPlace p = (IPlace)it.next();
			if (p.getType() == Spas.PLACE_FOCUS) p.setType(Spas.PLACE_SEE);
			if (p.getUpdateCount() < m_persistenceMemory.getUpdateCount() - 10)
				it.remove();
		}

		//m_places.clear();
	}
	
	/**
	 * Get the color of a given position.
	 * @param position The position.
	 * @return The Hexadecimal color code.
	 */
	public String getHexColor(Vector3f position) 
	{
		int value = getValue(position);
		if (value == Ernest.STIMULATION_VISUAL_UNSEEN)
		{
			Vector3f farPosition = new Vector3f(position);
			farPosition.scale(2f);
			//Vector3f farPosition = new Vector3f();
			//farPosition.scale(2f, position);
			value = getValue(farPosition);
		}
		return ErnestUtils.hexColor(value);
		//return getHexColor(getValue(position));
	}

	/**
	 * Get the value of the first bundle found in a given position.
	 * or STIMULATION_VISUAL_UNSEEN if no bundle found in that position.
	 * @param position The position.
	 * @return The value.
	 */
	public int getValue(Vector3f position)
	{
		int value = Ernest.STIMULATION_VISUAL_UNSEEN;

		IBundle b = getBundle(position);
		if (b != null)
			value = b.getValue();
		return value;
	}
	
	public ArrayList<IPlace> getPlaceList()
	{
		return m_places;
	}
	
//	public ArrayList<IPlace> getPersistentPlaceList()
//	{
//		ArrayList<IPlace> places = new ArrayList<IPlace>();
//		for (IPlace p : m_places)
//		{
//			if (p.getType() == Spas.PLACE_PERSISTENT)
//				places.add(p);
//		}
//		return places;
//	}
	
	public void setFocusPlace(IPlace focusPlace)
	{
		m_focusPlace = focusPlace;
	}

	public IPlace getFocusPlace()
	{
		return m_focusPlace;
	}
}
