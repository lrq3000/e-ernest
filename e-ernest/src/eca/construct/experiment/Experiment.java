package eca.construct.experiment;

import eca.construct.Appearance;
import eca.construct.egomem.Displacement;
import eca.ss.enaction.Act;

/**
 * An experiment is an Action performed on an Appearance.
 * Experiments record the resulting acts, 
 * so as to predict what act will result from an Action performed on an Appearance. 
 * @author Olivier
 */
public interface Experiment {
	
	/**
	 * @return This experiment's label.
	 */
	public String getLabel();
	
	/**
	 * @param act the act to add to this experiment 
	 */
	public void incActCounter(Act act);
	
	/**
	 * @param displacement The displacement to record to this experiment
	 */
	public void incDisplacementCounter(Displacement displacement);
	
	public void incPostAppearanceCounter(Appearance appearance);

	/**
	 * @return The act most probably resulting from this experiment.
	 */
	public Act predictAct();

	/**
	 * @return The displacement most probably resulting from this experiment
	 */
	public Displacement predictDisplacement();
	
	public Appearance predictPostAppearance();
	
	/**
	 * @return True if this experiment has been made. 
	 */
	public boolean isTested();

}
