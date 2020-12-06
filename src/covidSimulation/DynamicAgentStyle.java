package covidSimulation;

import java.awt.Color;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

/**
 * Class used to display the agents dynamically.
 * Takes into account the color of the agent.
 * @author Natacha
 *
 */
public class DynamicAgentStyle extends DefaultStyleOGL2D {

	/**
	 * Returns the color attribute of the agent.
	 */
	@Override
	public Color getColor(Object o) {
		return ((Agent)o).getColor();			
	}
	
	/**
	 * Creates small circle to represent the agents.
	 */
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
	    if (spatial == null) {
	      spatial = shapeFactory.createCircle(7, 7);
	    }
	    return spatial;
	}
}