package covidSimulation;

import java.awt.Color;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

/**
 * A person who has recovered can not anymore be infected because she has developped antibodies against Covid.
 * @author Natacha
 */
public class RecoveredAgent extends Agent {
	
	/**
	 * Constructor
	 * @param goal the goal of the agent
	 * @param age the age of the agent
	 * @param atRisk a boolean equal to true if this agent has an increased risk due to medical conditions
	 * @param hasMask a boolean equal to true if the agent wears a mask
	 */
	public RecoveredAgent(int goal, int age, boolean atRisk, boolean hasMask) {
		super(goal, age, atRisk, hasMask);
		status = Agent.RECOVERED_STATUS;
		this.nextStatus = status;
	}
	
	/**
	 * Returns the color used to display this agent.
	 */
	@Override
	public Color getColor() {
		return Color.GREEN;
	}
	
	/**
	 * Computes the next status. An agent with the status recovered can not any more be infected
	 * thus, this method is empty.
	 */
	@Override
	public void computeNextStatus() {
		// Empty because an agent with the status recovered can not any more be infected
	}
	
	/**
	 * Computes the next position according to the goal and moves the agent on the grid at the next position). 
	 */
	@Override	
	public void computeNextPositionAndApply() {	
		// Computes next position randomly and stores it in newPosition.
		computeNextPosition();

		// Move the position of this agent
		Context context = ContextUtils.getContext(this);
		Grid<Agent> grid = (Grid<Agent>) context.getProjection("grid");
		if (this.nextPosition != null)
			grid.moveTo(this, this.nextPosition.getX(), this.nextPosition.getY());
	}

}
