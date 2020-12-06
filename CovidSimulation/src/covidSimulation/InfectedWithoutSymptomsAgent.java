package covidSimulation;

import java.awt.Color;

import repast.simphony.context.Context;
import repast.simphony.query.space.grid.MooreQuery;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

/**
 * This class represents agents who have been infected but that have no symptoms.
 * At the end of the time of infection, they become recovered agents.
 * @author Natacha
 */
public class InfectedWithoutSymptomsAgent extends InfectedAgent {
	
	/**
	 * Number of ticks the person stays infected.
	 */
	static int MAX_TIME_OF_INFECTION = 7;
	
	/**
	 * Factor used to compute the contamination probability. It is less than 1 to decrease
	 * the probability, since the agent has no symptoms.
	 */
	static double FACTOR_WITHOUT_SYMPTOMS = 0.9;
	
	/**
	 * Count the time of infection. When MAX_TIME_OF_INFECTION is reached,
	 * the agent recovers.
	 */
	int countTimeOfInfection;
	
	/**
	 * Constructor
	 * @param goal the goal of the agent
	 * @param age the age of the agent
	 * @param atRisk a boolean equal to true if this agent has an increased risk due to medical conditions
	 * @param hasMask a boolean equal to true if the agent wears a mask
	 */
	public InfectedWithoutSymptomsAgent(int goal, int age, boolean atRisk, boolean hasMask) {
		super(goal, age, atRisk, hasMask);
		status = Agent.INFECTED_WITHOUT_SYMPTOMS_STATUS;
		this.nextStatus = status;
		this.countTimeOfInfection = 0;
	}
	
	/**
	 * Returns the color used to display this agent.
	 */
	@Override
	public Color getColor() {
		return Color.PINK;
	}
	
	/**
	 * Computes the next status. If the maximum time of infection is reached, it becomes recovered status.
	 */
	@Override
	public void computeNextStatus() {
		this.countTimeOfInfection++;
		
		
		if (this.countTimeOfInfection == MAX_TIME_OF_INFECTION) {
			// If the time of infection has been reached, the status changes to recovered.
			this.nextStatus = Agent.RECOVERED_STATUS;
		}
	}
	
	/**
	 * Computes the next position according to the goal, modifies the status (by removing the
	 * current agent of the context, creating a new agent with the right status and adding it
	 * to the context at the next position). 
	 */
	@Override	
	public void computeNextPositionAndApply() {
		Context context = ContextUtils.getContext(this);
		Grid<Agent> grid = (Grid<Agent>) context.getProjection("grid");
		
		// Computes next position randomly and stores it in newPosition.
		computeNextPosition();

		// Set the new status by removing this agent from the context and creating a new one.
		if (this.nextStatus != Agent.INFECTED_WITHOUT_SYMPTOMS_STATUS) {
			context.remove(this);
			Agent a = null;
			if (this.nextStatus == Agent.RECOVERED_STATUS) {
				a = new RecoveredAgent(Agent.RANDOM_GOAL, age, atIncreasedRisk,hasMask);
				context.add(a);
			}

			// Move the agent to his new position
			if (this.nextPosition != null)
				grid.moveTo(a, this.nextPosition.getX(), this.nextPosition.getY());
		}
		else { // The status has not changed
			// Move the agent to his new position
			if (this.nextPosition != null)
				grid.moveTo(this, this.nextPosition.getX(), this.nextPosition.getY());
		}
	}
	
	/**
	 * Returns the more realistic contamination probability by taking into account the fact
	 * that this agent has symptoms and wears or not the mask :  probInf * FACTOR_WITH_SYMPTOMS
	 * if he does not wear the mask,  probInf * FACTOR_WITH_SYMPTOMS * FACTOR_WITH_MASK 
	 * if he wears the mask.
	 * @return the more realistic contamination probability for this infected agent
	 */
	@Override
	public double computeContaminationProb() {
		double ownprob = Agent.probInf * FACTOR_WITHOUT_SYMPTOMS;
		if (this.wearMask())
			ownprob = ownprob * FACTOR_WITH_MASK;
		return ownprob;
	}
}
