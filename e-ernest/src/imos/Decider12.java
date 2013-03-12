package imos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import spas.ISpas;

/**
 * Decider for Ernest 12.
 * @author Olivier
 */
public class Decider12 extends Decider 
{
	Decider12(IImos imos, ISpas spas)
	{
		super(imos,spas);
	}
	
	/**
	 * Select an intention act from a given activation list.
	 * The selected act receives an activation value 
	 * @param activationList The list of acts that in the sequential context that activate episodic memory.
	 * @param propositionList The list of propositions made by the spatial system.
	 * @return The selected act.
	 */
	protected IAct selectAct(List<IAct> activationList)
	{
		List<IActProposition> actPropositions = new ArrayList<IActProposition>();	
		List<IProposition> schemaPropositions = new ArrayList<IProposition>();	
		
		Object decision = null;
		Object activations = null;
		Object inconsistences = null;
		if (m_tracer != null)
		{
			decision = m_tracer.addEventElement("decision", true);
			activations = m_tracer.addSubelement(decision, "activated_schemas");
			//inconsistences = m_tracer.addEventElement("inconsistences", true);
		}

		// Primitive acts receive a default proposition for themselves
		for(IAct a : m_imos.getActs())
		{
			if (a.getSchema().isPrimitive())
			{
				IActProposition p = new ActProposition(a, 0, 0);
				if (!actPropositions.contains(p))
					actPropositions.add(p);
			}       
		}

		// Browse all the existing schemas 
		for (ISchema s : m_imos.getSchemas())
		{
			if (!s.isPrimitive())
			{
				// Activate the schemas that match the context 
				boolean activated = false;
				for (IAct contextAct : activationList)
				{
					if (s.getContextAct().equals(contextAct))
					{
						activated = true;
						if (m_tracer != null)
							m_tracer.addSubelement(activations, "schema", s + " intention " + s.getIntentionAct() + ((s.getIntentionAct().getConfidence() == Imos.RELIABLE ) ? " reliable" : " unreliable" ));
						//System.out.println("Activate " + s + " s=" + s.getIntentionAct().getSatisfaction());
					}
				}
				
				// Activated schemas propose their intention
				if (activated)
				{
					IAct proposedAct = s.getIntentionAct();
					// The weight is the proposing schema's weight multiplied by the proposed act's satisfaction
					int w = s.getWeight() * proposedAct.getSatisfaction();
					//int w = s.getWeight();
                    // The expectation is the proposing schema's weight signed with the proposed act's status  
                    int e = s.getWeight();// * (s.getIntentionAct().getStatus() ? 1 : -1);
                    //int e = 0;
					
					// If the intention is consistent with spatial memory 
					//if (checkConsistency(proposedAct))
					{
					
						// If the intention is reliable then a proposition is constructed
						if ((proposedAct.getConfidence() == Imos.RELIABLE ) &&						 
							(proposedAct.getSchema().getLength() <= m_maxSchemaLength ))
						{
							//IProposition p = new Proposition(s.getIntentionAct().getSchema(), w, e);
							IActProposition p = new ActProposition(proposedAct, w, e);
		
							int i = actPropositions.indexOf(p);
							if (i == -1)
								actPropositions.add(p);
							else
								actPropositions.get(i).update(w, e);
						}
						// If the intention is not reliable
						// if the intention's schema has not passed the threshold then  
						// the activation is propagated to the intention's schema's context
						else
						{
							// Expect the value of the intention's schema's intention
							//e = proposedAct.getSchema().getIntentionAct().getSatisfaction();
							
							if (!proposedAct.getSchema().isPrimitive())
							{
								// only if the intention's intention is positive (this is some form of positive anticipation)
								if (proposedAct.getSchema().getIntentionAct().getSatisfaction() > 0)
								{
									//IProposition p = new Proposition(proposedAct.getSchema().getContextAct().getSchema(), w, e);
									IActProposition p = new ActProposition(proposedAct.getSchema().getContextAct(), w, e);
									int i = actPropositions.indexOf(p);
									if (i == -1)
										actPropositions.add(p);
									else
										actPropositions.get(i).update(w, e);
								}
							}
						}
					}//
//					else
//					{
//						if (m_tracer != null)
//							m_tracer.addSubelement(inconsistences, "inconsistence", proposedAct.getLabel() );
//					}
				}
			}
		}
		
		
		// Add the propositions from the spatial system 
		
//		for (IActProposition proposition : propositionList)
//		{
//			int i = proposals.indexOf(proposition);
//			if (i == -1)
//				proposals.add(proposition);
//			else
//				proposals.get(i).update(proposition.getWeight(), proposition.getExpectation());
//		}

		// Log the propositions
		
		//System.out.println("Propose: ");
		Object proposalElmt = null;
		if (m_tracer != null)
			proposalElmt = m_tracer.addSubelement(decision, "proposed_acts");
		
		for (IActProposition p : actPropositions)
		{
			if (m_tracer != null)
				m_tracer.addSubelement(proposalElmt, "act", p.toString());
			//System.out.println(p);
		}
		
		// TODO Update the expected satisfaction of each proposed schema based on the local map anticipation
		
		//Construct a list of schemaPropositions from the list of actPropositions.
		
		for (IActProposition actProposition : actPropositions)
		{
			//int w = actProposition.getWeight() * (actProposition.getAct().getSatisfaction() + actProposition.getExpectation());
			//int e = actProposition.getWeight();
			int w = actProposition.getWeight();
			int e = actProposition.getExpectation();
			IProposition schemaProposition = new Proposition(actProposition.getAct().getSchema(), w, e, actProposition.getAct());
			int i = schemaPropositions.indexOf(schemaProposition);
			if (i == -1)
				schemaPropositions.add(schemaProposition);
			else
				schemaPropositions.get(i).update(w, e, actProposition.getAct());
		}
		
		// sort by weighted proposition...
		Collections.sort(schemaPropositions);
		
		Object proposition = null;
		if (m_tracer != null)
			proposition = m_tracer.addSubelement(decision, "proposed_schemas");
		
		for (IProposition p : schemaPropositions)
		{
			if (m_tracer != null)
				m_tracer.addSubelement(proposition, "schema", p.toString());
			//System.out.println(p);
		}
		
		// count how many are tied with the highest weighted proposition
		int count = 0;
		int wp = schemaPropositions.get(0).getWeight();
		for (IProposition p : schemaPropositions)
		{
			if (p.getWeight() != wp)
				break;
			count++;
		}

		// pick one at random from the top of the proposal list
		// count is equal to the number of proposals that are tied...

		IProposition p = null;
		//if (DETERMINISTIC)
			p = schemaPropositions.get(0); // Always take the first
		//else
		//	p = schemaPropositions.get(m_rand.nextInt(count)); // Break the tie at random
				
		//IAct a = m_sensorimotorSystem.anticipateInteraction(p.getSchema(), p.getExpectation(), m_acts);
		IAct a = p.getAct();
		
		a.setActivation(p.getWeight());
		
		System.out.println("Select:" + a);

		if (m_tracer != null)
			m_tracer.addSubelement(decision, "select", a.toString());

		return a ;
	}
}