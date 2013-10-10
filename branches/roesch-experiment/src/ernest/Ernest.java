package ernest;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.vecmath.Vector3f;

import spas.IPlace;
import spas.ISegment;
import spas.ISpas;
import spas.ISpatialMemory;
//import spas.IStimulation;
import spas.Spas;
//import spas.Stimulation;
import imos.IAct;
import imos.IImos;
import imos.Imos;


/**
 * The main Ernest class used to create an Ernest agent in the environment.
 * @author ogeorgeon
 */
public class Ernest implements IErnest 
{
	/** A multiplication factor when we need decimal precision but still use integers. */		
	public static final int INT_FACTOR = 1000;
	
	/** A big value that can represent infinite for diverse purpose. */	
	public static final int INFINITE = 1000 * INT_FACTOR;

	/** Ernest's retina resolution  */
	public static int RESOLUTION_RETINA = 2; //12;
	//public static int CENTER_RETINA = 0; //55; 
		
	/** Ernest's colliculus resolution  */
	//public static int RESOLUTION_COLLICULUS = 24;
	
	/** Ernest's number of rows in the retina */
	public static int ROW_RETINA = 1;
	
	/** The duration during which checked landmarks remain not motivating  */
	//public static int PERSISTENCE = 100;//30; // (50 Ernest 9.3) (in IMOS clock)
	public static int PERSISTENCE = 300;//Ernest 12 (in SPAS clock)
	
	/** 200 Base attractiveness of bundles that are not edible  */
	public static int ATTRACTIVENESS_OF_UNKNOWN =  200;

	/** 400 Top attractiveness of bundles that are edible  */
	public static int ATTRACTIVENESS_OF_FISH  =  400;
	
	/** 300 Top attractiveness of cuddling another fish  */
	public static int ATTRACTIVENESS_OF_CUDDLE  =  300;
	
	/** -350 Attractiveness of bumping in a wall  */
	public static int ATTRACTIVENESS_OF_BUMP  =  -300;//-350;//-500;
	
	/** -200 Attractiveness of hard  */
	public static int ATTRACTIVENESS_OF_HARD  =  -200;
	
	/** 0 Attractiveness of background in the extrapersonal space  */
	public static int ATTRACTIVENESS_OF_BACKGROUND  =  0;
	
	/** A threshold for maturity that reduces exploration after a certain age to make demos nicer */
	public static int MATURITY = 1500; // not used currently.
	
	/** The radius of tactile sensors */
	public static float TACTILE_RADIUS = 1;
	
	/** Ernest physical diameter (for eat and bump) */
	public static float BOUNDING_RADIUS = .4f;
	
	/** A gustatory stimulation */
	public static int STIMULATION_GUSTATORY = 0;
	
	/** A kinematic stimulation */
	public static int STIMULATION_KINEMATIC = 1;
	
	/** A spatial stimulation */
	public static int MODALITY_SPATIAL = 1;
	
	/** A visual stimulation */
	public static int MODALITY_VISUAL = 2;

	/** A tactile stimulation */
	public static int MODALITY_TACTILE = 3;
	
	/** A circadian stimulation */
	public static int STIMULATION_CIRCADIAN = 4;
	
	/** The taste of food */
	public static int STIMULATION_FOOD = 2;
	
	/** Visual stimulation of seeing nothing */
	public static int STIMULATION_VISUAL_UNSEEN = 0xFFFFFF;//255 * 65536 + 255 * 256 + 255;

	/** Color of empty places for desplay */	
	public static int PHENOMENON_EMPTY = 0xFFFFFF;	
//	public static int PHENOMENON_FISH  = 0x9680FF;//255 * 65536 + 255 * 256 + 255;	
//	public static int PHENOMENON_WALL  = 0x008000;//255 * 65536 + 255 * 256 + 255;
//	public static int PHENOMENON_ALGA  = 0x73E600;
//	public static int PHENOMENON_BRICK = 0x00E6A0;

	/** Color unanimated */
	public static int UNANIMATED_COLOR = 0x808080;

	/** Touch empty */
	public static int STIMULATION_TOUCH_EMPTY = 0xB4B4B4;//11842740;
	
	/** Touch soft */
	public static int STIMULATION_TOUCH_SOFT = 0x646464;//100 * 65536 + 100 * 256 + 100 = 6579300
	
	/** Touch hard */
	public static int STIMULATION_TOUCH_WALL = 0x000000;
	
	/** Touch fish */
	public static int STIMULATION_TOUCH_FISH = 0x646465;//100 * 65536 + 100 * 256 + 101 = 6579301
	
	/** Touch other agent */
	public static int STIMULATION_TOUCH_AGENT = 0x646466;
	
	/** Kinematic Stimulation move forward */	
	public static int STIMULATION_KINEMATIC_FORWARD = 0xFFFFFF;//255 * 65536 + 255 * 256 + 255 = 16777215

	/** Kinematic Stimulations bump */
	public static int STIMULATION_KINEMATIC_BUMP = 0xFF0000;//255 * 65536 = 16711680
		
	/** Kinematic Stimulations turn left toward empty square */
	public static int STIMULATION_KINEMATIC_LEFT_EMPTY = 2;
		
	/** Kinematic Stimulations turn left toward wall */
	public static int STIMULATION_KINEMATIC_LEFT_WALL = 3;
		
	/** Kinematic Stimulations turn right toward empty square */
	public static int STIMULATION_KINEMATIC_RIGHT_EMPTY = 4;
		
	/** Kinematic Stimulations turn right toward wall */
	public static int STIMULATION_KINEMATIC_RIGHT_WALL = 5;
		
	/** Gustatory Stimulation nothing */	
	public static int STIMULATION_GUSTATORY_NOTHING = 0xFFFFFF;//255 * 65536 + 255 * 256 + 255;

	/** Gustatory Stimulation fish */	
	public static int STIMULATION_GUSTATORY_FISH = 0xFFFF00;//255 * 65536 + 255 * 256;
	
	/** Social Stimulation cuddle */	
	public static int STIMULATION_GUSTATORY_CUDDLE = 0xFF8080;
	
	/** Social Stimulation nothing */	
	public static int STIMULATION_SOCIAL_NOTHING = 0x000000;
	
	/** Ernest's current enaction */
	private IEnaction m_enaction = new Enaction();
	
	/** Ernest's spatial system. */
	private ISpas m_spas = new Spas();

	/** Ernest's Intrinsically motivated Schema Mechanism. */
	private IImos m_imos = new Imos();
	
	/** Ernest's decisional Mechanism. */
	private IDecider m_decider = new DeciderImos(m_imos, m_spas);
	
	/** Ernest's tracing system. */
	private ITracer m_tracer = null;
	
	/**
	 * Set Ernest's fundamental learning parameters.
	 * @param regularityThreshold The Regularity Sensibility Threshold.
	 * @param maxSchemaLength The Maximum Schema Length
	 */
	public void setParameters(int regularityThreshold, int maxSchemaLength) 
	{
		m_imos.setRegularityThreshold(regularityThreshold);
		m_decider.setMaxSchemaLength(maxSchemaLength);
	}

	/**
	 * Let the environment set the tracer.
	 * @param tracer The tracer.
	 */
	public void setTracer(ITracer tracer) 
	{ 
		m_tracer = tracer;
		m_imos.setTracer(m_tracer); 
		m_spas.setTracer(m_tracer);
		m_decider.setTracer(m_tracer);
	}

	/**
	 * Get a description of Ernest's internal state (to display in the environment).
	 * @return A representation of Ernest's internal state
	 */
	public String internalState() 
	{
		return m_imos.getInternalState();
	}
		
	public String step(IEffect effect) 
	{
		m_enaction.setEffect(effect);
		
		// Start a new interaction cycle.
		if (m_tracer != null)
		{
            m_tracer.startNewEvent(m_imos.getCounter());
			m_tracer.addEventElement("clock", m_imos.getCounter() + "");
		}                
		
		// track the enaction 
		
		m_imos.track(m_enaction);
		m_spas.track(m_enaction);			
		
		// Decision cycle
		if (m_enaction.isOver())
		{
			m_imos.terminate(m_enaction);
			m_enaction = m_decider.decide(m_enaction);
		}

		// Carry out the current enaction
		
		m_decider.carry(m_enaction);
		
		return m_enaction.getIntendedPrimitiveAct().getSchema().getLabel();		
	}

	public int getValue(int i, int j)
	{
		return m_spas.getValue(i,j);
	}
	public IAct addInteraction(String schemaLabel, String stimuliLabel, int satisfaction)
	{
		return m_imos.addInteraction(schemaLabel, stimuliLabel, satisfaction);
	}

	public ArrayList<IPlace> getPlaceList()
	{
		return m_spas.getPlaceList();
	}

	public int getCounter() 
	{
		if (m_imos == null)
			return 0;
		else
			return m_imos.getCounter();
	}

	public int getUpdateCount() 
	{
		return m_spas.getClock();
	}

	public ISpatialMemory getSpatialSimulation() 
	{
		return m_spas.getSpatialMemory();
	}

//	public void setFrame(JFrame frame) 
//	{
//		m_sensorymotorSystem.setFrame(frame);
//	}
}