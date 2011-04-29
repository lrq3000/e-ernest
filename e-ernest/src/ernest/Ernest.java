package ernest;

import java.awt.Color;

import tracing.ITracer;

/**
 * The main Ernest class used to create an Ernest agent in the environment.
 * @author ogeorgeon
 */
public class Ernest implements IErnest 
{
	/** A big value that can represent infinite for diverse purpose. */
	public static final int INFINITE = 1000;
	
	/** Color of regular wall  */
	public static Color COLOR_WALL   = new Color(0, 128, 0); // Color.getHSBColor(1/3f, 1f, 0.5f)
	public static Color COLOR_WATER  = new Color(150, 128, 255); // Color.getHSBColor(1/3f, 1f, 0.5f)
	public static Color COLOR_TOUCH_EMPTY  = new Color(140,140, 140); 
	public static Color COLOR_TOUCH_ALGA   = new Color(70, 70, 70); 
	public static Color COLOR_TOUCH_FISH  = new Color(70, 70, 70); 
	public static Color COLOR_TOUCH_WALL  = new Color(0, 0, 0);
	
	/** Ernest's retina resolution  */
	public static int RESOLUTION_RETINA = 12;
	public static int CENTER_RETINA = 55;
		
	/** Ernest's colliculus resolution  */
	public static int RESOLUTION_COLLICULUS = 24;
	
	/** Ernest's number of rows in the retina */
	public static int ROW_RETINA = 1;
	
	/** Hypothetical act (Cannot be chosen as an intention. Cannot support higher-level learning). */
	public static final int HYPOTHETICAL = 1;

	/** Reliable act (Can be chosen as an intention and can support higher-level learning). */
	public static final int RELIABLE = 2;

	/** Regularity sensibility threshold (The weight threshold for an act to become reliable). */
	public static int REG_SENS_THRESH = 5;

	/** Activation threshold (The weight threshold for higher-level learning with the second learning mechanism). */
	public static int ACTIVATION_THRESH = 1;

	/** Maximum length of a schema (For the schema to be chosen as an intention) */
	public static int SCHEMA_MAX_LENGTH = INFINITE;
	
	/** The duration during which checked landmarks remain not motivating  */
	public static int PERSISTENCE = 20; // (50 Ernest 9.3)
	
	/** 200 Base attractiveness of bundles that are not edible  */
	public static int BASE_MOTIVATION =  200;

	/** 400 Top attractiveness of bundles that are edible  */
	public static int TOP_MOTIVATION  =  400;
	
	/** A threshold for maturity that reduces exploration after a certain age to make demos nicer */
	public static int MATURITY = 1500; // not used currently.
	
	/** A gustatory stimulation */
	public static int STIMULATION_GUSTATORY = 0;
	
	/** A kinematic stimulation */
	public static int STIMULATION_KINEMATIC = 1;
	
	/** A visual stimulation */
	public static int STIMULATION_VISUAL = 2;
	
	/** A tactile stimulation */
	public static int STIMULATION_TACTILE = 3;
	
	/** A circadian stimulation */
	public static int STIMULATION_CIRCADIAN = 4;
	
	/** The taste of nothing */
	public static int STIMULATION_TASTE_NOTHING = 0;

	/** The taste of fish */
	public static int STIMULATION_TASTE_FISH = 1;

	/** The taste of food */
	public static int STIMULATION_FOOD = 2;
	
	/** Feeling empty */
	public static int STIMULATION_TOUCH_EMPTY = 0;
	
	/** Feeling soft */
	public static int STIMULATION_TOUCH_ALGA = 1;
	
	/** Feeling hard */
	public static int STIMULATION_TOUCH_FISH = 2;
	
	/** Feeling hard */
	public static int STIMULATION_TOUCH_WALL = 3;
	
	/** 0. Kinematic succeed */	
	public static int STIMULATION_KINEMATIC_SUCCEED = 0;
	
	/** 1. Bumping wall */	
	public static int STIMULATION_KINEMATIC_FAIL = 1;
	
	public static int STIMULATION_CIRCADIAN_DAY = 0;
	
	/** Ernest's primitive schema currently enacted */
	private IAct m_primitiveAct = null;
	
	/** Ernest's episodic memory. */
	private EpisodicMemory m_episodicMemory = new EpisodicMemory();

	/** Ernest's static system. */
	private StaticSystem m_staticSystem = new StaticSystem();

	/** Ernest's attentional system. */
	private IAttentionalSystem m_attentionalSystem = new AttentionalSystem(m_episodicMemory, m_staticSystem);
	
	/** Ernest's sensorymotor system. */
	private ISensorymotorSystem m_sensorymotorSystem;

	/** Ernest's tracing system. */
	private ITracer m_tracer = null;

	/**
	 * Set Ernest's fundamental learning parameters.
	 * Use null to leave a value unchanged.
	 * @param regularityThreshold The Regularity Sensibility Threshold.
	 * @param activationThreshold The Activation Threshold.
	 * @param schemaMaxLength The Maximum Schema Length
	 */
	public void setParameters(Integer regularityThreshold, Integer activationThreshold, Integer schemaMaxLength) 
	{
		if (regularityThreshold != null)
			REG_SENS_THRESH = regularityThreshold.intValue();
		
		if (activationThreshold != null)
			ACTIVATION_THRESH = activationThreshold.intValue();
		
		if (schemaMaxLength != null)
			SCHEMA_MAX_LENGTH = schemaMaxLength.intValue();
	}

	/**
	 * Let the environment set the sensorymotor system.
	 * @param sensor The sensorymotor system.
	 */
	public void setSensorymotorSystem(ISensorymotorSystem sensor) 
	{
		m_sensorymotorSystem = sensor;
		m_sensorymotorSystem.init(m_episodicMemory, m_staticSystem, m_attentionalSystem, m_tracer);
	};
	
	/**
	 * Let the environment set the tracer.
	 * @param tracer The tracer.
	 */
	public void setTracer(ITracer tracer) 
	{ 
		m_tracer = tracer;
		m_attentionalSystem.setTracer(m_tracer); 
		m_episodicMemory.setTracer(m_tracer);
		m_staticSystem.setTracer(m_tracer);
	}

	/**
	 * Provide access to Ernest's episodic memory
	 * (The environment can populate Ernest's episodic memory with inborn composite schemas) 
	 * @return Ernest's episodic memory. 
	 */
    public EpisodicMemory getEpisodicMemory()
    {
    	return m_episodicMemory;
    }

	/**
	 * Get a description of Ernest's internal state (to display in the environment).
	 * @return A representation of Ernest's internal state
	 */
	public String internalState() 
	{
		return m_attentionalSystem.getInternalState();
	}
		
	/**
	 * Ernest's main process.
	 * (All environments return at least a boolean feedback from Ernest's actions) 
	 * @param status The status received as a feedback from the previous primitive enaction.
	 * @return The next primitive schema to enact.
	 */
	public String step(boolean status) 
	{
		// Determine the primitive enacted act from the enacted schema and the data sensed in the environment.
		
		IAct enactedPrimitiveAct = m_sensorymotorSystem.enactedAct(m_primitiveAct, status);
		
		// Let Ernest decide for the next primitive schema to enact.
		
		m_primitiveAct = m_attentionalSystem.step(enactedPrimitiveAct);
		
		// Return the schema to enact.
		
		return m_primitiveAct.getSchema().getLabel();
	}

	/**
	 * Ernest's main process.
	 * (All environments return at least a boolean feedback from Ernest's actions) 
	 * @param status The status received as a feedback from the previous primitive enaction.
	 * @return The next primitive schema to enact.
	 */
	public String step(int[][] matrix) 
	{
		// Determine the primitive enacted act from the enacted schema and the data sensed in the environment.
		
		IAct enactedPrimitiveAct = m_sensorymotorSystem.enactedAct(m_primitiveAct, matrix);
		
		// Let Ernest decide for the next primitive schema to enact.
		
		m_primitiveAct = m_attentionalSystem.step(enactedPrimitiveAct);
		
		// Return the schema to enact.
		
		return m_primitiveAct.getSchema().getLabel();
	}

	public IObservation getObservation()
	{
		if (m_sensorymotorSystem != null)
			return m_sensorymotorSystem.getObservation();
		else
			return null;
	}

}
