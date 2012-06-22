package imos;

import javax.vecmath.Vector3f;

import spas.LocalSpaceMemory;
import utils.ErnestUtils;

import ernest.Ernest;


/**
 * An act is the association of a schema with the feedback the agent receives 
 * when trying to enact that schema.  
 * The term Act and the term interaction are used indifferently.  
 * @author mcohen
 * @author ogeorgeon
 */
public class Act implements IAct
{
	/** The act's status. True = Success, False = Failure */
	private boolean m_status = false;
	
	/** The act's satisfaction value. Represents Ernest's satisfaction to enact the act */
	private int m_satisfaction = 0;
	
	/** The act's schema */
	private ISchema m_schema = null;
	
	/** The schema that prescribes this act during enaction */
	private ISchema m_prescriberSchema = null;
	
	/** the label */
	private String m_label = "";
	
	/** Ernest's confidence in this act. Only RELIABLE acts generate higher-level learning  */
	private int m_confidence = Imos.HYPOTHETICAL;
	
	/** The act is activated for enaction  */
	private int m_activation = 0;
	
	/** The length of the act's schema  */
	private int m_length = 1;
	
	private int m_phenomenon = Ernest.PHENOMENON_EMPTY;
	private Vector3f m_startPosition = new Vector3f();
	private Vector3f m_endPosition = new Vector3f();
	private Vector3f m_translation = new Vector3f();
	private float m_rotation = 0;
	
	/**
	 * Constructor for a succeeding act. 
	 * @param s The act's schema
	 * @return the created act
	 */
	public static IAct createCompositeSucceedingAct(ISchema s)
	{
		if (s.isPrimitive())
		{
			System.out.println("Error creating a composite act. Schema " + s + " is not a composite schema.");
			return null;
		}
		else
		{
			String label = "(" + s.getLabel() +")";
			int satisfaction = s.getContextAct().getSatisfaction() + s.getIntentionAct().getSatisfaction();
		
			return new Act(label, s, true, satisfaction, Imos.HYPOTHETICAL);
		}
	}
	
	/**
	 * Constructor for a failing act. 
	 * @param s The act's schema.
	 * @param satisfaction The failing satisfaciton.
	 * @return the created act.
	 */
	public static IAct createCompositeFailingAct(ISchema s, int satisfaction)
	{
		if (s.isPrimitive())
		{
			System.out.println("Error creating a composite act. Schema " + s + " is not a composite schema.");
			return null;
		}
		else
		{
			String label = "[" + s.getLabel() +"]";
			// The failing act is RELIABLE because its schema had to be reliable to be enacted and 
			// making it possible to experience its failure.
			return new Act(label, s, false, satisfaction, Imos.RELIABLE);
		}
	}
	
	/**
	 * Create an act.
	 * @param label The act's label.
	 * @param s The act's schema. 
	 * @param status The act's status: True for success, false for failure.
	 * @param satisfaction The act's satisfaction value.
	 * @param confidence The degree of confidence Ernest has in this act.
	 * @return The created act.
	 */
	public static IAct createAct(String label, ISchema s, boolean status, int satisfaction, int confidence)
	{
		return new Act(label, s, status, satisfaction, confidence);
	}
	
	/**
	 * The abstract constructor for a no�me
	 * @param label The no�me's label
	 * @param s The no�me's schema if any
	 * @param status The no�me's status if any: True for success, false for failure
	 * @param type the module
	 * @param confidence The degree of confidence Ernest has in this no�me
	 */
	protected Act(String label, ISchema s, boolean status, int satisfaction, int confidence)
	{
		m_label = label;
		m_schema = s;
		m_status = status;
		m_satisfaction = satisfaction;
		m_confidence = confidence;
		if (s == null)
			m_length = 1;
		else 
			m_length = s.getLength();
	}
	
	public void setSatisfaction(int s)         
	{ 
		m_satisfaction = s; 
	}
	
	public void setConfidence(int c)           
	{ 
		m_confidence = c; 
	}
	
	public void setPrescriberSchema(ISchema s) 
	{ 
		m_prescriberSchema = s; 
	}
	
	public void setActivation(int a)           
	{ 
		m_activation = a; 
	}
	
	public boolean getStatus()                 
	{ 
		return m_status; 
	}
	
	public int getSatisfaction()           
	{ 
		return m_satisfaction; 
	}
	
	public int getConfidence()             
	{ 
		return m_confidence; 
	}
	
	public ISchema getPrescriberSchema()       
	{ 
		return m_prescriberSchema; 
	}
	
	public ISchema getSchema()                 
	{ 
		return m_schema; 
	}
	
	public int getActivation()             
	{ 
		return m_activation; 
	}
	
	public int getLength()                 
	{ 
		return m_length; 
	}

	/**
	 * @return The act's string representation
	 */
	public String getLabel()
	{
		return m_label;
	}
	
	public String toString()
	{
		String s= m_label;
		//if (m_schema != null)
		//	s = String.format("(S%s %s s=%s)", getSchema().getId() , getLabel(), getSatisfaction());  
		return s;
	}
	
	/**
	 * Acts are equal if they have the same label. 
	 */
	public boolean equals(Object o)
	{
		boolean ret = false;
		
		if (o == this)
			ret = true;
		else if (o == null)
			ret = false;
		else if (!o.getClass().equals(this.getClass()))
			ret = false;
		else
		{
			IAct other = (IAct)o;
			ret = (//other.getSchema() == getSchema() &&
				   //other.getStatus() == getStatus() &&
				   other.getLabel().equals(getLabel()));
		}
		
		return ret;
	}
	
	/**
	 * The greatest act is that that has the greatest activation. 
	 */
	public int compareTo(IAct a) 
	{
		return new Integer(a.getActivation()).compareTo(m_activation);
	}

	public void setPhenomenon(int phenomenon) 
	{
		m_phenomenon = phenomenon;
	}

	public int getPhenomenon() 
	{
		return m_phenomenon;
	}

	public void setEndPosition(Vector3f position) 
	{
		if (m_schema.isPrimitive())
			m_endPosition.set(position);
	}

	public Vector3f getEndPosition() 
	{
		if (m_schema.isPrimitive())
			return m_endPosition;
		else
			return (m_schema.getIntentionAct().getEndPosition());
	}

//	public void setTranslation(Vector3f translation) 
//	{
//		m_translation = translation;
//	}

	public Vector3f getTranslation() 
	{
		Vector3f translation = new Vector3f();
		if (m_schema.isPrimitive())
		{
			translation.set(m_endPosition);
			translation.sub(m_startPosition);
		}
		else
		{
			translation.set(m_schema.getContextAct().getTranslation());
			translation.add(m_schema.getIntentionAct().getTranslation());
		}
		return translation;
	}

	public void setRotation(float rotation) 
	{
		m_rotation = rotation;
	}

	public float getRotation() 
	{
		float rotation;
		if (m_schema.isPrimitive())
			rotation = m_rotation;
		else
			rotation = m_schema.getContextAct().getRotation() + m_schema.getIntentionAct().getRotation();
		return rotation;
	}

	public void setStartPosition(Vector3f position) 
	{
		m_startPosition.set(position);
	}

	/**
	 * Computes the start position of this act
	 * The start position of its intention
	 * rotated to the opposite direction of its context
	 * plus the translation of its context
	 */
	public Vector3f getStartPosition() 
	{
		Vector3f startPosition = new Vector3f(m_startPosition);
		if (m_schema.isPrimitive())
			startPosition.set(m_startPosition);
		else
		{
			startPosition.set(m_schema.getIntentionAct().getStartPosition());
			ErnestUtils.rotate(startPosition, - m_schema.getContextAct().getRotation());
			startPosition.add(m_schema.getContextAct().getTranslation());
		}
		return startPosition;
	}

	public boolean concernOnePlace() 
	{
		boolean concernOnePlace = false;
		
		if (m_schema.isPrimitive())
			concernOnePlace = true;
		else
		{
			if (m_schema.getContextAct().getStartPosition().equals(LocalSpaceMemory.DIRECTION_HERE))
				concernOnePlace = true;
			Vector3f t = new Vector3f(m_schema.getIntentionAct().getEndPosition());
			t.sub(m_schema.getContextAct().getTranslation());
			t.sub(m_schema.getIntentionAct().getTranslation());
			if (m_schema.getContextAct().getStartPosition().equals(t))
				concernOnePlace = true;
		}
		return concernOnePlace;
	}
}
