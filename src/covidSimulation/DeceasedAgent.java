package covidSimulation;

import java.awt.Color;

import repast.simphony.context.Context;
import repast.simphony.util.ContextUtils;

/**
 * This class represents the "deceased" agents.
 * @author Natacha
 *
 */
public class DeceasedAgent extends Agent {
		
	/**
	 * This agent will disappear after 3 ticks.
	 */
	int timeAppearance;
	
	/**
	 * Default constructor.
	 */
	public DeceasedAgent() {
		super(Agent.RANDOM_GOAL, 0, true, false); // the goal, the age, the mask and the atIncreasedRisk are not used any more
		status = Agent.DECEASED_STATUS;
		this.timeAppearance = 3;
		
		incrementCountTotalDeaths();
	}
	
	/**
	 * Returns the display color of this agent.
	 */
	public Color getColor() {
		return Color.BLACK;
	}
	
	/**
	 * At each step, the time Appearance decreases.
	 */
	@Override
	public void computeNextStatus() {
		timeAppearance--;
	}
	
	/**
	 * A deceased agent does not move anymore. It remains in the context during timeAppearance time steps.
	 */
	@Override	
	public void computeNextPositionAndApply() {
		
		// The agent "disappears" at the end of timeAppearance.
		if (timeAppearance == 0) {
			Context context = ContextUtils.getContext(this);
			context.remove(this);
		}

	}

}
