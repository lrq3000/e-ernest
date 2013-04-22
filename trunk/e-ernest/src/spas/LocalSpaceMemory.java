package spas;

import java.util.ArrayList;
import java.util.Iterator;

import javax.media.j3d.Transform3D;
import javax.vecmath.Point3f;
import utils.ErnestUtils;
import ernest.Ernest;
import ernest.ITracer;
import imos2.IInteraction;


/**
 * Ernest's spatial memory. 
 * @author Olivier
 */
public class LocalSpaceMemory implements ISpatialMemory, Cloneable
{
	
	/** The radius of a location. */
	public final static float LOCATION_RADIUS = 0.5f;
	public final static float LOCAL_SPACE_MEMORY_RADIUS = 20f;//4f;
	public final static float DISTANCE_VISUAL_BACKGROUND = 10f;
	public final static float EXTRAPERSONAL_DISTANCE = 1.5f;
	public final static float DIAG2D_PROJ = (float) (1/Math.sqrt(2));
	public final static Point3f DIRECTION_HERE         = new Point3f(0, 0, 0);
	public final static Point3f DIRECTION_AHEAD        = new Point3f(1, 0, 0);
	public final static Point3f DIRECTION_BEHIND       = new Point3f(-1, 0, 0);
	public final static Point3f DIRECTION_LEFT         = new Point3f(0, 1, 0);
	public final static Point3f DIRECTION_RIGHT        = new Point3f(0, -1, 0);
	public final static Point3f DIRECTION_AHEAD_LEFT   = new Point3f(DIAG2D_PROJ, DIAG2D_PROJ, 0);
	public final static Point3f DIRECTION_AHEAD_RIGHT  = new Point3f(DIAG2D_PROJ, -DIAG2D_PROJ, 0);
	public final static Point3f DIRECTION_BEHIND_LEFT  = new Point3f(-DIAG2D_PROJ, DIAG2D_PROJ, 0);
	public final static Point3f DIRECTION_BEHIND_RIGHT = new Point3f(-DIAG2D_PROJ, -DIAG2D_PROJ, 0);	
	public final static float    SOMATO_RADIUS = 1f;
	
	//public final static int SIMULATION_INCONSISTENT = -1;
	//public final static int SIMULATION_UNKNOWN = 0;
	//public final static int SIMULATION_CONSISTENT = 1;
	//public final static int SIMULATION_AFFORD = 2;
	//public final static int SIMULATION_REACH = 10;
	//public final static int SIMULATION_REACH2 = 4;
	public final static int SIMULATION_NEWCOMPRESENCE = 11;
	
	/** The duration of persistence in local space memory. */
	public static int PERSISTENCE_DURATION = 5;//50;
	
	/** The Local space structure. */
	private ArrayList<IPlace> m_places = new ArrayList<IPlace>();
	
	//private ISpas m_spas;
	//private Transform3D m_transform = new Transform3D();
		
	/**
	 * Clone spatial memory to perform simulations
	 * TODO clone the places 
	 * From tutorial here: http://ydisanto.developpez.com/tutoriels/java/cloneable/ 
	 * @return The cloned spatial memory
	 */
	public ISpatialMemory clone() 
	{
		LocalSpaceMemory cloneSpatialMemory = null;
		try {
			cloneSpatialMemory = (LocalSpaceMemory) super.clone();
		} catch(CloneNotSupportedException cnse) {
			cnse.printStackTrace(System.err);
		}

		// We must clone the place list because it is passed by reference by default

		ArrayList<IPlace> clonePlaces = new ArrayList<IPlace>();
		for (IPlace place : m_places)
			clonePlaces.add(place.clone());
		cloneSpatialMemory.setPlaceList(clonePlaces);

		//cloneSpatialMemory.m_places = clonePlaces;
		//cloneSpatialMemory.m_clock = m_clock;
		
		return cloneSpatialMemory;
	}

	public void tick()
	{
		for (IPlace p : m_places)
			p.incClock();
	}

	public void trace(ITracer tracer)
	{
		if (tracer != null && !m_places.isEmpty())
		{
			Object localSpace = tracer.addEventElement("local_space");
			tracer.addSubelement(localSpace, "position_8", ErnestUtils.hexColor(getValue(DIRECTION_HERE)));
			tracer.addSubelement(localSpace, "position_7", ErnestUtils.hexColor(getValue(DIRECTION_BEHIND)));
			tracer.addSubelement(localSpace, "position_6", ErnestUtils.hexColor(getValue(DIRECTION_BEHIND_LEFT)));
			tracer.addSubelement(localSpace, "position_5", ErnestUtils.hexColor(getValue(DIRECTION_LEFT)));
			tracer.addSubelement(localSpace, "position_4", ErnestUtils.hexColor(getValue(DIRECTION_AHEAD_LEFT)));
			tracer.addSubelement(localSpace, "position_3", ErnestUtils.hexColor(getValue(DIRECTION_AHEAD)));
			tracer.addSubelement(localSpace, "position_2", ErnestUtils.hexColor(getValue(DIRECTION_AHEAD_RIGHT)));
			tracer.addSubelement(localSpace, "position_1", ErnestUtils.hexColor(getValue(DIRECTION_RIGHT)));
			tracer.addSubelement(localSpace, "position_0", ErnestUtils.hexColor(getValue(DIRECTION_BEHIND_RIGHT)));
		}
	}
	
	public IPlace addPlace(IInteraction interaction, Point3f position)
	{
		IPlace place = new Place(interaction, position);	
		m_places.add(place);
		return place;
	}
	
	public void transform(Transform3D transform)
	{
		if (transform != null)
			for (IPlace p : m_places)
				p.transform(transform);
	}
	
	/**
	 * Get the value at a given position.
	 * (The last place found in the list of places that match this position)
	 * (Used to display in the trace)
	 * @param position The position of the location.
	 * @return The bundle.
	 */
	public int getValue(Point3f position)
	{
		int value = Ernest.UNANIMATED_COLOR;
		for (IPlace p : m_places)
		{
			if (p.isInCell(position) && p.getType() == Place.ENACTION_PLACE)
				if (value != 0x73E600 && value != 0x00E6A0)
				value = p.getValue();
		}	
		return value;
	}

	/**
	 * Clear a position in the local space memory.
	 * @param position The position to clear.
	 */
	public void clearPlace(Point3f position)
	{
		for (Iterator it = m_places.iterator(); it.hasNext();)
		{
			IPlace l = (IPlace)it.next();
			if (l.isInCell(position))
				it.remove();
		}		
	}
	
	/**
	 * Clear the places farther than DISTANCE_VISUAL_BACKGROUND.
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
	 * Clear all the places older than PERSISTENCE_DURATION.
	 */
	public void decay()
	{
		for (Iterator it = m_places.iterator(); it.hasNext();)
		{
			IPlace p = (IPlace)it.next();
//			if (p.getType() == Place.ENACTION_PLACE || p.getType() == Place.EVOKED_PLACE )
			{
				//if (p.getClock() < m_clock - PERSISTENCE_DURATION +1) // -1
				if (p.getClock() > PERSISTENCE_DURATION ) // -1
					it.remove();
			}
//			else
//			{
//				//if (p.getClock() < m_clock - PERSISTENCE_DURATION)
//				if (p.getClock() > PERSISTENCE_DURATION)
//					it.remove();
//				else if (p.getType() == Place.AFFORD || p.getType() == Place.UNKNOWN)// || p.getType() == Place.SIMULATION_PLACE)
//					it.remove();
//			}
		}
	}
	
	/**
	 * @return The list of places in Local Spave Memory
	 */
	public ArrayList<IPlace> getPlaceList()
	{
		return m_places;
	}
	
	public void setPlaceList(ArrayList<IPlace> places) 
	{
		m_places = places;
	}
		
//	public void setTransform(Transform3D transform) 
//	{
//		m_transform = transform;
//	}
//
//	public Transform3D getTransform() 
//	{
//		return m_transform;
//	}
}
