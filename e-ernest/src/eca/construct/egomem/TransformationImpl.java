package eca.construct.egomem;

import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.Transform3D;

public class TransformationImpl implements Transformation {

	private static Map<String , Transformation> TRANSFORMATIONS = new HashMap<String , Transformation>() ;

	private String label;
	private Transform3D transform3D = new Transform3D();
	
	/**
	 * Create or get an action from its label.
	 * @param label The action's label
	 * @return The created or retrieved action.
	 */
	public static Transformation createOrGet(String label){
		if (!TRANSFORMATIONS.containsKey(label))
			TRANSFORMATIONS.put(label, new TransformationImpl(label));			
		return TRANSFORMATIONS.get(label);
	}
	
	private TransformationImpl(String label){
		this.label = label;
	}
	

	public String getLabel() {
		return label;
	}
	
	public void setTransform3D(Transform3D transform3D){
		this.transform3D.set(transform3D);
	}
	
	public Transform3D getTransform3D(){
		return this.transform3D;
	}

	/**
	 * Transformations are equal if they have the same label. 
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
			Transformation other = (Transformation)o;
			ret = (other.getLabel().equals(this.label));
		}
		
		return ret;
	}

}
