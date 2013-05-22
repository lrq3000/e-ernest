package imos2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ernest.ActionImpl;
import ernest.Action;
import ernest.Primitive;
import ernest.ITracer;
import ernest.Observation;
import ernest.PrimitiveImpl;
import spas.ISpas;

/**
 * This is the regular decider for Ernest 7 that does not use spatial memory.
 * @author Olivier
 */
public class Decider implements IDecider 
{
	private IImos imos;
	private ISpas spas;
	private ITracer tracer;

	/**
	 * @param imos The sequential system
	 * @param spas The spatial system
	 */
	public Decider(IImos imos, ISpas spas)
	{
		this.imos = imos;
		this.spas = spas;
	}

	public void setTracer(ITracer tracer)
	{
		this.tracer = tracer;
	}
	
	public IEnaction decide(IEnaction enaction) 
	{
		IEnaction newEnaction = new Enaction();
		//Act nextTopIntention = null;
		
		System.out.println("New decision ================ ");

		// Choose the next action
		ArrayList<IProposition> propositions = this.imos.propose(enaction);	
		weightActions(propositions);
		Action action = selectAction();

		// Predict the next observation
		Observation  observation = this.spas.predict(action);
		
		// Construct the intended act
//		Primitive nextPrimitive = PrimitiveImpl.getInteraction(action, observation.getAspect());
//		if (nextPrimitive== null)
//			nextTopIntention = action.getActs().get(0);
//		else
//			nextTopIntention = this.imos.addAct(nextPrimitive, observation.getArea());
		Act nextTopIntention = ActImpl.getAct(action, observation);

		System.out.println("Act " + nextTopIntention.getLabel());
		newEnaction.setTopInteraction(nextTopIntention);
		newEnaction.setTopRemainingInteraction(nextTopIntention);
		newEnaction.setPreviousLearningContext(enaction.getInitialLearningContext());
		newEnaction.setInitialLearningContext(enaction.getFinalLearningContext());
		
		return newEnaction;
	}
	
	/**
	 * Weight the actions according to the proposed interactions
	 */
	private void weightActions(ArrayList<IProposition> propositions){
		
		// Reset the weight of actions.
		for (Action m : ActionImpl.getACTIONS())
			m.setPropositionWeight(0);
		
		// Proposed interactions that correspond to an identified action support this action.
		for (IProposition p: propositions)
			if (p.getAct().getAction() != null)
				p.getAct().getAction().addPropositionWeight(p.getWeight());	
		
		// trace weighted actions 
		Object decisionElmt = null;
		if (this.tracer != null){
			decisionElmt = this.tracer.addEventElement("Actions", true);
			for (Action a : ActionImpl.getACTIONS()){
				String actslabel = "";
					for (Act act : a.getActs())
						actslabel += (" " + act.getLabel());
				System.out.println("Propose action " + a.getLabel() + " with weight " + a.getPropositionWeight());
				this.tracer.addSubelement(decisionElmt, "Action", a.getLabel() + " proposition weight " + a.getPropositionWeight() + actslabel);
			}
		}
	}
	
	/**
	 * Select an interaction from the list of proposed interactions
	 * @return The selected action.
	 */
	protected Action selectAction()
	{
		// Sort the propositions by weight.
		// Oddly, i could not directly cast ACTIONS.values() to List<Action>
		List<Action> actions = new ArrayList<Action>();
		for (Action a : ActionImpl.getACTIONS())
			actions.add(a);
		Collections.sort(actions);

		// Pick the most weighted action
		Action selectedAction = actions.get(0);
		
		System.out.println("Select:" + selectedAction.getLabel());

		// Trace the selected interaction
		if (this.tracer != null){			
			Object selectionElmt = this.tracer.addEventElement("selection", true);
			this.tracer.addSubelement(selectionElmt, "selected_action", selectedAction.getLabel());
		}
		
		return selectedAction;
	}

	public void carry(IEnaction enaction)
	{
		Act intendedPrimitiveInteraction = enaction.getTopRemainingInteraction().prescribe();
		enaction.setIntendedPrimitiveInteraction(intendedPrimitiveInteraction);
		enaction.setStep(enaction.getStep() + 1);
		enaction.traceCarry(this.tracer);
	}
}
