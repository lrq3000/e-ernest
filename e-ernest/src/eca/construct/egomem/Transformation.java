package eca.construct.egomem;

import javax.media.j3d.Transform3D;

/**
 * A transformation in spatial memory
 * @author Olivier
 */
public interface Transformation {

	/** Predefined transformations */
	public static Transformation UNKNOWN = TransformationImpl.createOrGet("?");
	public static Transformation IDENTITY = TransformationImpl.createOrGet("<");
	public static Transformation SHIFT_LEFT = TransformationImpl.createOrGet("^");
	public static Transformation SHIFT_RIGHT = TransformationImpl.createOrGet("v");
	
	/**
	 * @return The transformation's label.
	 */
	public String getLabel();
	
	public void setTransform3D(Transform3D transform3D);
	
	public Transform3D getTransform3D();

}
