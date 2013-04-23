package imos2;

import java.util.ArrayList;
import java.util.List;
import ernest.ITracer;

/**
 * The Intrinsically Motivated Schema mechanism.
 * @author ogeorgeon
 */

public class Imos implements IImos 
{	
	/** Default maximum length of a schema (For the schema to be chosen as an intention) */
	public final int SCHEMA_MAX_LENGTH = 100;

	/** Default Activation threshold (The weight threshold for higher-level learning with the second learning mechanism). */
	public final int ACTIVATION_THRESH = 1;

	/** Regularity sensibility threshold (The weight threshold for an act to become reliable). */
	private int m_regularitySensibilityThreshold = 6;

	/** A list of all the acts ever created. Aimed to replace schemas and acts*/
	private ArrayList<IInteraction> m_interactions = new ArrayList<IInteraction>(2000);
	
	/** Counter of learned schemas for tracing */
	private int m_nbSchemaLearned = 0;
	
	/** The Tracer. */
	private ITracer<Object> m_tracer = null; //new Tracer("trace.txt");

	/** A representation of the internal state for display in the environment. */
	private String m_internalState = "";
	
	/** Counter of cognitive cycles. */
	private int m_imosCycle = 0;
	
	/**
	 * Constructor
	 */
	public Imos()
	{
	}
	/**
	 * Constructor for the sequential system.
	 * @param regularitySensibilityThreshold  The regularity sensibility threshold.
	 * A lower value favors the faster adoption of possibly less satisfying sequences, 
	 * a higher value favors the slower adoption of possibly more satisfying sequences.
	 */
	public Imos(int regularitySensibilityThreshold)
	{
		m_regularitySensibilityThreshold = regularitySensibilityThreshold;
	}

	public void setRegularityThreshold(int regularityThreshold)
	{
		m_regularitySensibilityThreshold = regularityThreshold;
	}
	
	/**
	 * @param tracer The tracer.
	 */
	public void setTracer(ITracer<Object> tracer)
	{
		m_tracer = tracer;
	}
	
	/**
	 * Get a string description of the imos's internal state for display in the environment.
	 * @return A representation of the imos's internal state
	 */
	public String getInternalState()
	{
		return m_internalState;
	}

	/**
	 * Construct a new interaction or retrieve the interaction if it already exists.
	 * @param label The label of the interaction.
	 * @param satisfaction The interaction's satisfaction (only needed in case this interaction was not 
	 * yet declared in imos).
	 * @return The interaction that was created or that already existed.
	 */
	public IInteraction addInteraction(String label, int satisfaction)
	{
		// Primitive satisfactions are multiplied by 10 internally for rounding issues.   
		// (this value does not impact the agent's behavior)
		IInteraction i = Interaction.createPrimitiveInteraction(label, satisfaction * 10);
		
		int j = m_interactions.indexOf(i);
		if (j == -1)
		{
			// The interaction does not exist
			m_interactions.add(i);
			System.out.println("Define primitive interaction " + i);
		}
		else 
			// The interaction already exists: return a pointer to it.
			i =  m_interactions.get(j);
		
		return i;		
	}

	/**
	 * Add a composite schema and its succeeding act that represent a composite possibility 
	 * of interaction between Ernest and its environment. 
	 * @param preInteraction The context Act.
	 * @param postInteraction The intention Act.
	 * @return The schema made of the two specified acts, whether it has been created or it already existed. 
	 */
    private IInteraction addCompositeInteraction(IInteraction preInteraction, IInteraction postInteraction)
    {
    	IInteraction i = Interaction.createCompositeInteraction(preInteraction, postInteraction);
    	
		int j = m_interactions.indexOf(i);
		if (j == -1)
		{
			// The schema does not exist: create its succeeding act and add it to Ernest's memory
			m_interactions.add(i);
			m_nbSchemaLearned++;
		}
		else
			// The schema already exists: return a pointer to it.
			i =  m_interactions.get(j);
    	
    	// Any alternate interactions of the preInteraction is an alternate interaction of the composite interaction
		Object alternateElmnt = null;
		if (m_tracer != null)
			alternateElmnt = m_tracer.addEventElement("alternate", true);
    	for (IInteraction a: preInteraction.getAlternateInteractions())
    	{
    		boolean newAlternate = i.addAlternateInteraction(a);
			if (m_tracer != null && newAlternate)
				m_tracer.addSubelement(alternateElmnt, "prominent", i + " alternate " + a);
    	}

    	return i;
    }

	/**
	 * Track the current enaction. 
	 * Use the intended primitive act and the effect.
	 * Generates the enacted primitive act, the top enacted act, and the top remaining act.
	 * @param enaction The current enaction.
	 */
	public void track(IEnaction enaction) 
	{
		m_imosCycle++;		
		
		IInteraction intendedPrimitiveInteraction = enaction.getIntendedPrimitiveInteraction();
		IInteraction enactedPrimitiveInteraction  = null;
		IInteraction topEnactedInteraction        = null;
		IInteraction topRemainingInteraction      = null;

		// If we are not on startup
		if (intendedPrimitiveInteraction != null)
		{
			// Compute the enacted primitive interaction from the move and the effect.
			// Compute the enaction value of interactions that were not yet recorded
			enactedPrimitiveInteraction = addInteraction(intendedPrimitiveInteraction.getLabel().substring(0, 1)+ enaction.getEffect().getLabel(), 0);

			// Compute the top actually enacted interaction
			//topEnactedInteraction = enactedInteraction(enactedPrimitiveInteraction, enaction);
			// TODO compute the actually enacted top interaction.
			topEnactedInteraction = topEnactedInteraction(enactedPrimitiveInteraction, intendedPrimitiveInteraction);
			
			// Update the prescriber hierarchy.
			if (intendedPrimitiveInteraction.equals(enactedPrimitiveInteraction)) 
				topRemainingInteraction = intendedPrimitiveInteraction.updatePrescriber();
			else
			{
				intendedPrimitiveInteraction.terminate();
			}
			
			System.out.println("Enacted primitive interaction " + enactedPrimitiveInteraction );
			System.out.println("Top remaining interaction " + topRemainingInteraction );
			System.out.println("Enacted top interaction " + topEnactedInteraction );
			
		}					
		
		// Update the current enaction
		enaction.setEnactedPrimitiveInteraction(enactedPrimitiveInteraction);
		enaction.setTopEnactedInteraction(topEnactedInteraction);
		enaction.setTopRemainingInteraction(topRemainingInteraction);

		// Trace
		enaction.traceTrack(m_tracer);
	}
	
	/**
	 * Terminate the current enaction.
	 * Use the top intended interaction, the top enacted interaction, the previous learning context, and the initial learning context.
	 * Generates the final activation context and the final learning context.
	 * Record or reinforce the learned interactions. 
	 * @param enaction The current enaction.
	 */
	public void terminate(IEnaction enaction)
	{

		IInteraction intendedTopInteraction = enaction.getTopInteraction();
		IInteraction enactedTopInteraction  = enaction.getTopEnactedInteraction();
		ArrayList<IInteraction> previousLearningContext = enaction.getPreviousLearningContext();
		ArrayList<IInteraction> initialLearningContext = enaction.getInitialLearningContext();

		Object alternateElmnt = null;
		if (m_tracer != null)
			alternateElmnt = m_tracer.addEventElement("alternate", true);

		
		// if we are not on startup
		if (enactedTopInteraction != null)
		{
			// Surprise if the enacted interaction is not that intended
			if (intendedTopInteraction != enactedTopInteraction) 
			{
				m_internalState= "!";
				enaction.setCorrect(false);	
				boolean newAlternate = intendedTopInteraction.addAlternateInteraction(enactedTopInteraction);
				//recordAlternate(initialLearningContext, enactedTopInteraction, intendedTopInteraction);
				if (m_tracer != null && newAlternate)
					m_tracer.addSubelement(alternateElmnt, "prominent", intendedTopInteraction + " alternate " + enactedTopInteraction);

				if (enactedTopInteraction.getPrimitive() && enactedTopInteraction.getPrimitive())
				{
					newAlternate = enactedTopInteraction.addAlternateInteraction(intendedTopInteraction);
					if (m_tracer != null && newAlternate)
						m_tracer.addSubelement(alternateElmnt, "prominent", enactedTopInteraction + " alternate " + intendedTopInteraction);
				}
			}
			
			// learn from the  context and the enacted interaction
			m_nbSchemaLearned = 0;
			System.out.println("Learn from enacted top interaction");
			ArrayList<IInteraction> streamContextList = record(initialLearningContext, enactedTopInteraction);
						
			// learn from the base context and the stream interaction	
			 if (streamContextList.size() > 0) // TODO find a better way than relying on the enacted act being on the top of the list
			 {
				 IInteraction streamInteraction = streamContextList.get(0); // The stream act is the first learned 
				 System.out.println("Streaming " + streamInteraction);
				 if (streamInteraction.getEnactionWeight() > ACTIVATION_THRESH)
				 {
					System.out.println("Learn from stream interaction");
					record(previousLearningContext, streamInteraction);
				 }
			 }

//			// learn from the current context and the actually enacted act			
//			//if (topEnactedAct != performedAct)
//			{
//				System.out.println("Learn from enacted");
//				//List<IAct> streamContextList2 = m_episodicMemory.record(initialLearningContext, topEnactedAct);
//				List<IInteraction> streamContextList2 = record(initialLearningContext, enactedTopInteraction);
//				// learn from the base context and the streamAct2
//				if (streamContextList2.size() > 0)
//				{
//					IInteraction streamAct2 = streamContextList2.get(0);
//					System.out.println("Streaming2 " + streamAct2 );
//					if (streamAct2.getEnactionWeight() > ACTIVATION_THRESH)
//						//m_episodicMemory.record(previousLearningContext, streamAct2);
//						record(previousLearningContext, streamAct2);
//				}
//			}	
			
			//enaction.setFinalContext(topEnactedAct, performedAct, streamContextList);			
			enaction.setFinalContext(enactedTopInteraction, enactedTopInteraction, streamContextList);			
		}
		//enaction.setNbActLearned(m_episodicMemory.getLearnCount());
		enaction.setNbActLearned(m_nbSchemaLearned);
		enaction.traceTerminate(m_tracer);

	}

	/**
	 * Learn from an enacted interaction after a given context.
	 * Returns the list of learned acts that are based on reliable subacts. The first act of the list is the stream act.
	 * @param contextList The list of acts that constitute the context in which the learning occurs.
	 * @param enactedInteraction The intention.
	 * @return A list of the acts created from the learning. The first act of the list is the stream act if the first act of the contextList was the performed act.
	 */
	private ArrayList<IInteraction> record(List<IInteraction> contextList, IInteraction enactedInteraction)
	{
		
		Object learnElmnt = null;
		if (m_tracer != null)
		{
			//Object propositionElmt = m_tracer.addSubelement(decision, "proposed_moves");
			learnElmnt = m_tracer.addEventElement("learned", true);
		}
		
		ArrayList<IInteraction> newContextList= new ArrayList<IInteraction>(20);
		
		if (enactedInteraction != null)
		{
			for (IInteraction preInteraction : contextList)
			{
				// Build a new interaction with the context pre-interaction and the enacted post-interaction 
				IInteraction newInteraction = addCompositeInteraction(preInteraction, enactedInteraction);
				newInteraction.setEnactionWeight(newInteraction.getEnactionWeight() + 1);
				System.out.println("learned " + newInteraction);
				if (m_tracer != null)	
					m_tracer.addSubelement(learnElmnt, "interaction", newInteraction.toString());
			
				// The new interaction belongs to the context 
				// if its pre-interaction and post-interaction have passed the regularity threshold
				if ((preInteraction.getEnactionWeight()     > m_regularitySensibilityThreshold) &&
  				    (enactedInteraction.getEnactionWeight() > m_regularitySensibilityThreshold))
				{
					newContextList.add(newInteraction);
					// System.out.println("Reliable schema " + newSchema);
				}
			}
		}
		return newContextList; 
	}

//	/**
//	 * Record the alternate interaction of the intended interaction.
//	 * @param contextList The list of acts that constitute the context in which the learning occurs.
//	 * @param enactedInteraction The enacted interaction.
//	 * @param intendedInteraction The intended interaction.
//	 */
//	private void recordAlternate(List<IInteraction> contextList, IInteraction enactedInteraction, IInteraction intendedInteraction)
//	{
//		
//		Object alternateElmnt = null;
//		if (m_tracer != null)
//			alternateElmnt = m_tracer.addEventElement("alternate", true);
//		
//		for (IInteraction preInteraction : contextList)
//		{
//			// retrieve the activated interaction that proposed the intended interaction 
//			IInteraction activatedInteraction = addCompositeInteraction(preInteraction, intendedInteraction);
//			
//			activatedInteraction.addAlternateInteraction(enactedInteraction);
//			System.out.println("Activated " + activatedInteraction);
//			if (m_tracer != null)
//				m_tracer.addSubelement(alternateElmnt, "activated", activatedInteraction.toString() + " alternate " + enactedInteraction);
//		}
//	}

	public ArrayList<IInteraction> getInteractions()
	{
		return m_interactions;
	}

	public int getCounter() 
	{
		return m_imosCycle;
	}

	public int getRegularityThreshold() 
	{
		return m_regularitySensibilityThreshold;
	}
	
	/**
	 * Recursively construct the current actually enacted act. 
	 *  (may construct extra intermediary schemas but that's ok because their weight is not incremented)
	 * @param enactedInteraction The enacted interaction.
	 * @param intendedInteraction The intended interaction.
	 * @return the actually enacted interaction
	 */
	private IInteraction topEnactedInteraction(IInteraction enactedInteraction, IInteraction intendedInteraction)
	{
		IInteraction topEnactedInteraction = null;
		IInteraction prescriberInteraction = intendedInteraction.getPrescriber();
		
		if (prescriberInteraction == null)
			// top interaction
			topEnactedInteraction = enactedInteraction;
		else
		{
			// The i was prescribed
			if (prescriberInteraction.getStep() == 0)
			{
				// enacted the prescriber's pre-interaction 
				//topEnactedInteraction = enactedAct(prescriberSchema, a);
				topEnactedInteraction = topEnactedInteraction(enactedInteraction, prescriberInteraction);
			}
			else
			{
				// enacted the prescriber's post-interaction
				IInteraction interaction = addCompositeInteraction(prescriberInteraction.getPreInteraction(), enactedInteraction);
				topEnactedInteraction = topEnactedInteraction(interaction, prescriberInteraction);
				//topEnactedInteraction = enactedAct(prescriberSchema, enactedSchema.getSucceedingAct());
			}
		}
			
		return topEnactedInteraction;
	}
	

}
