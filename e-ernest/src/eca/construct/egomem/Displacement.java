package eca.construct.egomem;

import javax.media.j3d.Transform3D;

import eca.construct.Area;

/**
 * A transformation in spatial memory
 * @author Olivier
 */
public interface Displacement {

	/**
	 * @return The transformation's label.
	 */
	public String getLabel();
	
	/**
	 * @return The 3D transformation
	 */
	public Transform3D getTransform3D();
	
	public void setTransform3D(Transform3D t);

	public Area getPreArea();

	public Area getPostArea();

}