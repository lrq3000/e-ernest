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
	public static int PLACE_EVOKE_PHENOMENON = 17;
	public static int PLACE_PHENOMENON = 18;
	
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
	
	IObservation m_observation;
	
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
		m_observation = observation;
		
		// translate and rotate the local space memory;
		
		Vector3f memoryTranslation = new Vector3f(observation.getTranslation());
		memoryTranslation.scale(-1);
		//m_localSpaceMemory.update(memoryTranslation, - observation.getRotation());
		m_localSpaceMemory.update(observation.getTranslation(), observation.getRotation());

		// Create and maintain phenomenon places from interaction places. 
		
		m_localSpaceMemory.phenomenon(places, observation, m_clock);
		
		// Construct synergies associated with bundles in the peripersonal space.		
		//synergy(interactionPlace, observation);
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
//	public void setPlaceList(List<IPlace> placeList)
//	{
//		m_placeList = placeList;
//	}
		
	public ArrayList<IPlace> getPlaceList()
	{
		//return m_places;
		return m_localSpaceMemory.getPlaceList();
	}

	public void traceLocalSpace() 
	{
		m_localSpaceMemory.trace();
	}

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

	public ArrayList<IPlace> getPhenomena() 
	{
		return m_localSpaceMemory.getPhenomena();
	}

	public boolean checkAct(IAct act) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public int getValue(Vector3f position) 
	{
		return m_localSpaceMemory.getValue(position);
	}

	public void initSimulation() 
	{
		m_localSpaceMemory.initSimulation();
	}

	public void translateSimulation(Vector3f translation) 
	{
		m_localSpaceMemory.translateSimulation(translation);
	}

	public void rotateSimulation(float angle) 
	{
		m_localSpaceMemory.rotateSimulation(angle);
	}

	public int getValueSimulation(Vector3f position) 
	{
		return m_localSpaceMemory.getValueSimulation(position);
	}	
}
