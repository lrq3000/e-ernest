package ernest;

/**
 * An act is the association of a schema with the feedback the agent receives 
 * when trying to enact that schema. Concretely, an act associates a schema 
 * with a binary feedback status: succeed (S) or fail (F).  This class 
 * represents a default implementation for an Act. 
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
	
	/** The no�me's type */
	private String m_label = "";
	private int m_module = 0;
	private int m_confidence = Ernest.HYPOTHETICAL_NOEME;
	
	public void setSatisfaction(int s)         { m_satisfaction = s; }
	public void setConfidence(int c)           { m_confidence = c; }
	public void setPrescriberSchema(ISchema s) { m_prescriberSchema = s; }
	
	public boolean getStatus()                 { return m_status; }
	public int     getSatisfaction()           { return m_satisfaction; }
	public int     getModule()           	   { return m_module; }
	public int     getConfidence()             { return m_confidence; }
	public ISchema getPrescriberSchema()       { return m_prescriberSchema; }
	public ISchema getSchema()                 { return m_schema; }

	/**
	 * The abstract constructor for any kind of no�me
	 * @param label The no�me's label
	 * @param s The no�me's schema if any
	 * @param status The no�me's status if any: True for success, false for failure
	 * @param type the module
	 * @param confidence The degree of confidence Ernest has in this no�me
	 */
	public Act(String label, ISchema s, boolean status, int satisfaction, int type, int confidence)
	{
		m_label = label;
		m_schema = s;
		m_status = status;
		m_satisfaction = satisfaction;
		m_module = type;
		m_confidence = confidence;
	}
	
	/**
	 * Short constructor for sensorymotor acts 
	 * @param s The act's schema
	 * @param success The act's status
	 * @param satisfaction The act's satisfaction value
	 */
	public Act(ISchema s, boolean success, int satisfaction)
	{
		m_schema = s;
		m_status = success;
		m_satisfaction = satisfaction;
		m_label = String.format("%s%s%s", 
				getStatus() ? "(" : "[", getSchema().getLabel() , getStatus() ? ")" : "]");
		m_module = Ernest.SENSORYMOTOR;

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
		String s = String.format("(S%s %s s=%s)", 
				getSchema().getId() , getLabel(), getSatisfaction());  
		return s;
	}
	
	/**
	 * Acts are equals if they have the same schema and status 
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
			ret = (other.getSchema() == getSchema() &&
				   other.getStatus() == getStatus());
		}
		
		return ret;
	}
	

}
