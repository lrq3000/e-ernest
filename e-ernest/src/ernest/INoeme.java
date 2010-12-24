package ernest;

/**
 * A no�me is an element of awareness in the agent's phenomenological experience.  
 * (http://fr.wikipedia.org/wiki/No%C3%A8me_%28philosophie%29)
 * As implemented, it is an element of Ernest's internal context.
 * No�mes can activate schemas when they match the schema's context.
 * No�mes can be pushed into Ernest's situation awareness.
 * Primitive sensorymotor no�mes relate to primitive enaction if they are not inhibited (later).
 * Primitive iconic no�mes relate to the iconic sensory system if they are not inhibited.
 * No�mes are chained through schemas.
 * @author ogeorgeon
 */
public interface INoeme 
{
	
	/**
	 * @return A unique string representation of that no�me.
	 */
	public String getLabel();
	
	/**
	 * @return The type of that no�me. So far, only SENSORYMOTOR or ICON.
	 */
	public int getModule();
}
