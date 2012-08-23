package ernest;

import imos.IAct;
import imos.IProposition;
import imos.ISchema;
import imos.Imos;
import imos.Proposition;

import java.awt.Color;
import java.util.ArrayList;

import javax.media.j3d.Transform3D;
import javax.swing.JFrame;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import spas.IBundle;
import spas.IObservation;
import spas.IPlace;
import spas.ISpatialMemory;
import spas.LocalSpaceMemory;
import spas.Observation;
import spas.Place;
import spas.Spas;
import utils.ErnestUtils;

/**
 * Implement Ernest 12.0's sensorimotor system.
 * The binary sensorimotor system plus local space memory tracking.
 * @author ogeorgeon
 */
public class Ernest12SensorimotorSystem extends BinarySensorymotorSystem 
{
	/** The observation */
    private IObservation m_observation ;
    
    private int m_satisfaction = 0;
    
    //private ISpatialMemory m_spatialSimulation = new LocalSpaceMemory();
    
	//private JFrame m_frame;

	public IAct enactedAct(IAct act, IObservation observation) 
	{
		// The schema is null during the first cycle
		if (act == null) return null;
		
		// Computes the resulting interaction from the visual observation
		if (m_observation != null)
		{
			m_satisfaction = 0;
	        String rightFeature  = sensePixel(m_observation.getVisualDistance()[0], observation.getVisualDistance()[0]);
	        String leftFeature  = sensePixel(m_observation.getVisualDistance()[1], observation.getVisualDistance()[1]);
	        if (leftFeature.equals(" ") && rightFeature.equals(" "))
	        	{leftFeature = ""; rightFeature = "";}
	    	
	        if (act.getSchema().getLabel().equals(">"))
	        	m_satisfaction += (observation.getStimuli().equals("t") ? 5 : -10);
	        else if (act.getSchema().getLabel().equals("^") || act.getSchema().getLabel().equals("v"))
	        	m_satisfaction -= 3;
	        else
	        	m_satisfaction -= 1;
	        
	        observation.setStimuli(leftFeature + rightFeature + observation.getStimuli());
	        observation.setSatisfaction(m_satisfaction);
		}
        m_observation = observation;
        
 		IAct enactedAct = addInteraction(act.getSchema().getLabel(), observation.getStimuli(), m_satisfaction);
 		
 		clearSimulation();
		//m_spatialSimulation.clearSimulation();
		
		return enactedAct;
	}
	
    private String sensePixel(int previousPixel, int currentPixel) 
    {
            String feature = " ";
            int satisfaction = 0;
            
            // arrived
            if (previousPixel > currentPixel && currentPixel == 0)
            {
                    feature = "x";
                    satisfaction = 10;
            }
            
            // closer
            else if (previousPixel < Ernest.INFINITE && currentPixel < previousPixel)
            {
                    feature = "+";
                    satisfaction = 10;
            }

            // appear
            else if (previousPixel == Ernest.INFINITE && currentPixel < Ernest.INFINITE)
            {
                    feature = "*";
                    satisfaction = 15;
            }
            
            // disappear
            else if (previousPixel < Ernest.INFINITE && currentPixel == Ernest.INFINITE)
            {
                    feature = "o";
                    satisfaction = -15;
            }

            System.out.println("Sensed " + "prev=" + previousPixel + "cur=" + currentPixel + " feature " + feature);
            
            m_satisfaction += satisfaction;

            return feature;
    }
    
	/**
	 * The agent's self model is hard coded in the interactions.
	 * TODO The phenomenon code, position, and spatial transformation should be learned rather than hard coded.
	 */
	public IAct addInteraction(String schemaLabel, String stimuliLabel, int satisfaction)
	{
		// Create the act in imos
		
		IAct act = m_imos.addInteraction(schemaLabel, stimuliLabel, satisfaction);
		
		// Add the spatial properties ========
		
		if (schemaLabel.equals(">"))
		{
			Transform3D tf = new Transform3D();
			if (stimuliLabel.indexOf("f") >= 0)
			{
				act.setColor(0xFF0000);
				act.setStartPosition(LocalSpaceMemory.DIRECTION_AHEAD);
				//tf.rotZ(0);
				//tf.setTranslation(new Vector3f());
				//act.setTransform(tf);
			}
			else if (stimuliLabel.indexOf("b") >= 0)
			{
				act.setColor(0xFF0000);
				act.setStartPosition(LocalSpaceMemory.DIRECTION_AHEAD);
				//Transform3D tf = new Transform3D();
				//tf.rotZ(0);
				//tf.setTranslation(new Vector3f());
				//act.setTransform(tf);
			}
			else if (stimuliLabel.indexOf("a") >= 0)
			{
				act.setColor(0x73E600);
				act.setStartPosition(LocalSpaceMemory.DIRECTION_AHEAD);
				tf.setTranslation(new Vector3f(-1,0,0));
			}
			else if (stimuliLabel.equals("++t"))
			{
				act.setColor(Ernest.PHENOMENON_FISH);
				act.setStartPosition(new Point3f(4,0,0));
				//Transform3D tf = new Transform3D();
				//tf.rotZ(0);
				tf.setTranslation(new Vector3f(-1,0,0));
				//act.setTransform(tf);
			}
			else if (stimuliLabel.equals(" +t"))
			{
				act.setColor(Ernest.PHENOMENON_FISH);
				act.setStartPosition(new Point3f(3,-3,0));
				//Transform3D tf = new Transform3D();
				//tf.rotZ(0);
				tf.setTranslation(new Vector3f(-1,0,0));
				//act.setTransform(tf);
			}
			else if (stimuliLabel.equals("+ t"))
			{
				act.setColor(Ernest.PHENOMENON_FISH);
				act.setStartPosition(new Point3f(3,3,0));
				//Transform3D tf = new Transform3D();
				//tf.rotZ(0);
				tf.setTranslation(new Vector3f(-1,0,0));
				//act.setTransform(tf);
			}
			else 
			{
				act.setColor(Ernest.PHENOMENON_EMPTY);
				act.setStartPosition(LocalSpaceMemory.DIRECTION_AHEAD);
				//Transform3D tf = new Transform3D();
				//tf.rotZ(0);
				tf.setTranslation(new Vector3f(-1,0,0));
				//act.setTransform(tf);
			}
			act.setTransform(tf);
		}

		if (schemaLabel.equals("<"))
		{
			if (stimuliLabel.indexOf("t") >= 0 || stimuliLabel.equals("  "))
			{
				act.setColor(Ernest.PHENOMENON_EMPTY);
				act.setStartPosition(LocalSpaceMemory.DIRECTION_BEHIND);
				Transform3D tf = new Transform3D();
				tf.rotZ(0);
				tf.setTranslation(new Vector3f(1,0,0));
				act.setTransform(tf);
			}
			else
			{
				act.setColor(Ernest.PHENOMENON_WALL);
				act.setStartPosition(LocalSpaceMemory.DIRECTION_BEHIND);
				Transform3D tf = new Transform3D();
				tf.rotZ(0);
				tf.setTranslation(new Vector3f(0,0,0));
				act.setTransform(tf);
			}
		}

		if (act.getLabel().equals("^* f"))
		{
			act.setColor(Ernest.PHENOMENON_FISH);
			act.setStartPosition(new Point3f(3,3,0));
			Transform3D tf = new Transform3D();
			tf.rotZ(- Math.PI / 2);
			act.setTransform(tf);
		}
		else if (schemaLabel.equals("^"))
		{
			act.setColor(Ernest.PHENOMENON_EMPTY);
			act.setStartPosition(LocalSpaceMemory.DIRECTION_HERE);
			Transform3D tf = new Transform3D();
			tf.rotZ(- Math.PI / 2);
			act.setTransform(tf);
		}
		
		if (act.getLabel().equals("v *f"))
		{
			act.setColor(Ernest.PHENOMENON_FISH);
			act.setStartPosition(new Point3f(3,-3,0));
			Transform3D tf = new Transform3D();
			tf.rotZ(Math.PI / 2);
			act.setTransform(tf);
		}
		else if (schemaLabel.equals("v"))
		{
			act.setColor(Ernest.PHENOMENON_EMPTY);
			act.setStartPosition(LocalSpaceMemory.DIRECTION_HERE);
			Transform3D tf = new Transform3D();
			tf.rotZ(Math.PI / 2);
			act.setTransform(tf);
		}
		
		if (schemaLabel.equals("/") )
		{
			if (stimuliLabel.indexOf("f") >= 0 || stimuliLabel.equals("  "))
			{
				act.setColor(Ernest.PHENOMENON_EMPTY);
				act.setStartPosition(LocalSpaceMemory.DIRECTION_LEFT);
			}
			else
			{
				act.setColor(Ernest.PHENOMENON_WALL);
				act.setStartPosition(LocalSpaceMemory.DIRECTION_LEFT);
			}
		}
		
		if (schemaLabel.equals("-"))
		{
			if (stimuliLabel.indexOf("f") >= 0 || stimuliLabel.equals("  "))
			{
				act.setColor(Ernest.PHENOMENON_EMPTY);
				act.setStartPosition(LocalSpaceMemory.DIRECTION_AHEAD);
			}
			else if (stimuliLabel.indexOf("a") >= 0 )
			{
				act.setColor(Ernest.PHENOMENON_ALGA);
				act.setStartPosition(LocalSpaceMemory.DIRECTION_AHEAD);
			}
			else if (stimuliLabel.indexOf("b") >= 0 )
			{
				act.setColor(Ernest.PHENOMENON_BRICK);
				act.setStartPosition(LocalSpaceMemory.DIRECTION_AHEAD);
			}
			else
			{
				act.setColor(Ernest.PHENOMENON_WALL);
				act.setStartPosition(LocalSpaceMemory.DIRECTION_AHEAD);
			}
		}
		
		if (schemaLabel.equals("\\"))
		{
			act.setStartPosition(LocalSpaceMemory.DIRECTION_RIGHT);
			if (stimuliLabel.indexOf("f") >= 0 || stimuliLabel.equals("  "))
				act.setColor(Ernest.PHENOMENON_EMPTY);
			else
				act.setColor(Ernest.PHENOMENON_WALL);
		}

		return act;
	}

	public void updateSpas(IAct primitiveAct, IAct topAct)
	{
		// Add this act into spatial memory
		
		m_spas.tick();
		if (primitiveAct != null)
		{
			// Apply the spatial transformation to spatial memory
			m_spas.followUp(primitiveAct);
			
			// Place the act in spatial memory
			
			IPlace place = m_spas.addPlace(new Point3f(topAct.getEndPosition()), Place.EVOKE_PHENOMENON);
			place.setValue(topAct.getColor());
			place.setUpdateCount(m_spas.getClock());
			place.setAct(topAct);
			
			// TODO place intermediary acts of higher level acts too?
			if (!topAct.getSchema().isPrimitive())
			{
				IPlace place2 = m_spas.addPlace(new Point3f(primitiveAct.getEndPosition()), Place.EVOKE_PHENOMENON);
				place2.setValue(primitiveAct.getColor());
				place2.setUpdateCount(m_spas.getClock());
				place2.setAct(primitiveAct);
			}
			// Update the spatial system to construct phenomena ==
			
			IObservation observation = new Observation();
			observation.setPrimitiveAct(primitiveAct);
			m_spas.step(observation);
		}

	}
	
	public boolean checkConsistency(IAct act) 
	{
		ISpatialMemory simulationMemory = m_spas.getSpatialMemory().clone();
		int status = simulationMemory.runSimulation(act, m_spas);
		return (status == LocalSpaceMemory.SIMULATION_UNKNOWN || status == LocalSpaceMemory.SIMULATION_CONSISTENT || status == LocalSpaceMemory.SIMULATION_AFFORD);

		//return true;
	}
	
	/**
	 * Tells the interaction that is likely to result from the enaction of this schema.
	 * If the schema has no succeeding or failing act defined, 
	 * then pick a random interaction attached to this schema.
	 * TODO Simulate the action to get a better anticipation.
	 * @param s The schema. 
	 * @return The anticipated resulting interaction.
	 */
	public IAct anticipateInteraction(ISchema s, int e, ArrayList<IAct> acts)
	{
		IAct anticipateInteraction = null;
		boolean status = (e >= 0);
		anticipateInteraction = (status ? s.getSucceedingAct() : s.getFailingAct());
		
		// if the schema has no succeeding or failing act, then pick an act randomly
		if (anticipateInteraction==null)
		{
			for (IAct a : acts)
			{
				//if (a.getSchema().equals(s) && (a.getStatus() == true))
				if (a.getSchema().equals(s) )
					anticipateInteraction = a;
			}
		}
		return anticipateInteraction;
	}

	/**
	 * Propose all acts that are afforded by the spatial context
	 * and primitive acts that inform about unknown places.
	 */
	public ArrayList<IProposition> getPropositionList(ArrayList<IAct> acts)
	{
		ArrayList<IProposition> propositionList = new ArrayList<IProposition>();
		int  PHENOMENA_WEIGHT = 10;
		int UNKNOWN_WEIGHT = 10;
		
		Object activations = null;
		if (m_tracer != null)
			activations = m_tracer.addEventElement("copresence_propositions", true);

		//m_spatialSimulation.clearSimulation();

		// Simulate all acts in spatial memory. 
		
		for (IAct a : acts)
		{
			//m_spas.initSimulation();
			//ISpatialMemory spatialSimulation = m_spas.getSpatialMemory().clone();

			//m_spatialSimulation = m_spas.getSpatialMemory();
			if (a.getConfidence() == Imos.RELIABLE && a.getSchema().getLength() <= 4)
			{
				//System.out.println("Simulate: " + a.toString());
				//int consistence = m_spatialSimulation.runSimulation(a, m_spas);
				
				int consistence = runSimulation(a);
				
				// Create a proposition for acts that are afforded by the spatial situation
				if (consistence == LocalSpaceMemory.SIMULATION_AFFORD)
				{
					int w = PHENOMENA_WEIGHT * a.getSatisfaction();
					IProposition p = new Proposition(a.getSchema(), w, PHENOMENA_WEIGHT * (a.getStatus() ? 1 : -1));
					propositionList.add(p);
					if (m_tracer != null)
						m_tracer.addSubelement(activations, "afforded", p.toString());
				}

				// Create a proposition for acts that inform the spatial situation
				if (consistence == LocalSpaceMemory.SIMULATION_UNKNOWN)
				{
					if (a.getSchema().getLabel().equals("-") || a.getSchema().getLabel().equals("/") || a.getSchema().getLabel().equals("\\"))
					{
						IProposition p = new Proposition(a.getSchema(), UNKNOWN_WEIGHT, UNKNOWN_WEIGHT * (a.getStatus() ? 1 : -1));
						propositionList.add(p);
						if (m_tracer != null)
							m_tracer.addSubelement(activations, "poke", p.toString());
					}
				}
				
				// Create a proposition for acts that reach a situation where another act is afforded.
				// TODO make it work !
				if (consistence == LocalSpaceMemory.SIMULATION_REACH)
				{
					int w = PHENOMENA_WEIGHT * (a.getSatisfaction() + 50);
					IProposition p = new Proposition(a.getSchema(), w, PHENOMENA_WEIGHT * (a.getStatus() ? 1 : -1));
					propositionList.add(p);
					if (m_tracer != null)
						m_tracer.addSubelement(activations, "reach", p.toString());
				}
				if (consistence == LocalSpaceMemory.SIMULATION_REACH2)
				{
					int w = PHENOMENA_WEIGHT * (a.getSatisfaction() + 100);
					IProposition p = new Proposition(a.getSchema(), w, PHENOMENA_WEIGHT * (a.getStatus() ? 1 : -1));
					propositionList.add(p);
					if (m_tracer != null)
						m_tracer.addSubelement(activations, "reach", p.toString());
				}
			}
						
//			if (m_frame != null) 
//			{
//				m_frame.repaint(); 
//				ErnestUtils.sleep(500);
//			}
		}	
		return propositionList;
	}	
	
//	public void setFrame(JFrame frame)
//	{
//		m_frame = frame;
//	}

}
