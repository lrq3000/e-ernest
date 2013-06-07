package spas;

import imos2.Act;

import javax.media.j3d.Transform3D;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import utils.ErnestUtils;

/**
 * A place is a location in the local space that marks something.
 * @author Olivier
 */
public class PlaceImpl implements Place 
{
	private Act act;
	private Point3f position = new Point3f();
	private Vector3f orientation = new Vector3f(1,0,0);	
	private int displayCode;
	private int clock = 0;

	/**
	 * Create a new place 
	 * (The provided position is cloned so the place can be moved without changing the provided position).
	 * @param act The interaction at this place.
	 * @param position This place's position.
	 */
	public PlaceImpl(Act act, Point3f position){
		this.act = act;
		this.position.set(position);
	}
	
	public Act getAct() {
		return this.act;
	}

	public Point3f getPosition() {
		return this.position;
	}
	
	/**
	 * Clone a place
	 * Warning: the bundle and act that this place contain are not cloned 
	 * @return The cloned place
	 */
	public Place clone(){
		PlaceImpl clonePlace = null;
		try {
			clonePlace = (PlaceImpl) super.clone();
		} catch(CloneNotSupportedException cnse) {
			cnse.printStackTrace(System.err);
		}

		// We must clone the objects because they are passed by reference by default
		clonePlace.setPosition(this.position);
		clonePlace.setOrientation(this.orientation);

		return clonePlace;
	}
	
	public void transform(Transform3D transform)
	{
		transform.transform(this.position);
		transform.transform(this.orientation);
	}		
	
	public float getDirection() 
	{
		return ErnestUtils.polarAngle(new Vector3f(this.position));
	}

	public float getDistance() 
	{
		return this.position.distance(new Point3f());
	}
	public void setPosition(Point3f position) 
	{
		// Create a new instance of the vector so it can be used to clone this place.
		this.position = new Point3f(position);
	}

	public void setClock(int clock) 
	{
		this.clock = clock;
	}

	public int getClock() 
	{
		return this.clock;
	}

	public void setOrientation(float orientation) 
	{
		//m_orientationAngle = orientation;
		this.orientation.set((float) Math.cos(orientation), (float) Math.sin(orientation), 0);
	}
	


	public float getOrientationAngle() 
	{
		//return m_orientationAngle;
		return ErnestUtils.polarAngle(this.orientation);
	}
	
	public void setValue(int value) 
	{
		this.displayCode = value;
	}

	public int getValue() 
	{
		return this.displayCode;
	}

	public void setOrientation(Vector3f orientation) 
	{
		// Create a new instance of the vector so it can be used to clone this place.
		this.orientation = new Vector3f(orientation);
	}

	public Vector3f getOrientation() 
	{
		return orientation;
	}

	public void incClock() 
	{
		this.setPosition(new Point3f(this.position.x * 1.1f, this.position.y * 1.1f, 0f));
		this.clock++;
	}

	/**
	 * Places are equal if they have the same act and location and clock
	 */
	public boolean equals(Object o){
		boolean ret = false;
		
		if (o == this)
			ret = true;
		else if (o == null)
			ret = false;
		else if (!o.getClass().equals(this.getClass()))
			ret = false;
		else
		{
			Place other = (Place)o;
			ret  = (this.act.equals(other.getAct()) 
					&& this.position.epsilonEquals(other.getPosition(), .1f)
					&& (this.clock == other.getClock()));
			//ret = isInCell(other.getPosition()) && other.getClock() == getClock() && other.getType() == getType();
		}		
		return ret;
	}

	public boolean isInCell(Point3f position){
		boolean ret;
		// Is in the same cell.
		ret = (Math.round(this.position.x) == Math.round(position.x)) && (Math.round(this.position.y) == Math.round(position.y)); 
		
		// Is in the same cell in egocentric polar referential.
		
		// Does not work for the cell behind !!
//		if (m_position.length() < .5f && position.length() < .5f)
//			ret = true;
//		else if (Math.round(ErnestUtils.polarAngle(m_position) / (float)Math.PI * 4) ==
// 			     Math.round(ErnestUtils.polarAngle(  position) / (float)Math.PI * 4) &&
// 			     (Math.round(m_position.length()) == Math.round(position.length())))
//			ret = true;
//		else 
//			ret = false;
		
		return ret;		
	}

//	public boolean from(Vector3f position) 
//	{
//		boolean from = false;
//		Vector3f compare = new Vector3f(this.position);
//		
//		// TODO Should take the differential speed of mobile objects into account.
//		// (not the speed due to Ernest's movement but the part that is due to the object's movement)
//		//if (m_speed != null) compare.sub(m_speed);
//		
//		compare.sub(position);
//		if (compare.length() < .2f) 
//			from = true;
//		
//		return from;
//	}


}
