package imos2;

import eca.Act;


/**
 * A proposition to enact an interaction. 
 * @author ogeorgeon
 */
public interface IProposition extends Comparable<IProposition> 
{
	/**
	 * @return The interaction proposed by this proposition.
	 */
	public Act getAct();
	
	/**
	 * @return The proposition's weight.
	 */
	public int getWeight();
	
	/**
	 * @param w The weight to add to the proposition.
	 */
	public void addWeight(int w);
		
	/**
	 * Two propositions are equal if they propose the same interaction. 
	 */
	public boolean equals(Object o);	
}
