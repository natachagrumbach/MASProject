package covidSimulation;

import java.awt.Color;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

/**
 * This class represents agents who have been infected but and have symptoms.
 * At the end of the time of infection, they become either recovered agents or deceased.
 * @author Natacha
 */
public class InfectedWithSymptomsAgent extends InfectedAgent {
	
	/**
	 * Number of ticks the person stays infected.
	 */
	static int MAX_TIME_OF_INFECTION = 14;
	
	/**
	 * Factor used to compute the contamination probability. It is greater than 1 to increase
	 * the probability, since the agent has symptoms.
	 */
	static double FACTOR_WITH_SYMPTOMS = 1.1;
	
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
	public InfectedWithSymptomsAgent(int goal, int age, boolean atRisk, boolean hasMask) {
		super(goal, age, atRisk, hasMask);
		status = Agent.INFECTED_WITH_SYMPTOMS_STATUS;
		this.nextStatus = status;
		this.countTimeOfInfection = 0;
	}
	
	/**
	 * Returns the color used to display this agent.
	 */
	@Override
	public Color getColor() {
		return Color.RED;
	}
	
	/**
	 * Computes the next status. If the maximum time of infection is reached, it becomes either recovered
	 * or deceased.
	 */
	@Override
	public void computeNextStatus() {
		this.countTimeOfInfection++;
		
		// If the time of infection has been reached, the status changes to recover or deceased
		// according to the probability probRec.
		if (this.countTimeOfInfection == MAX_TIME_OF_INFECTION) {
			double rand = Math.random();
			
			double probaRecovering = Agent.probRec;
			
			// If you are old, the mean probability of recovering should be decreased
			if (this.age > 65) {
				if (this.age < 75)
					probaRecovering = probaRecovering * 0.8;
				else probaRecovering = probaRecovering * 0.7;
			}
			// If you are at increased risk, the mean probability of recovering should be decreased
			if (this.atIncreasedRisk) {
				probaRecovering = probaRecovering * 0.8;
			}
			
			if (rand < probaRecovering) {
				// The next status of the agent is recovered
				this.nextStatus = Agent.RECOVERED_STATUS;
			}
			else {
				// The next status of the agent is deceased
				this.nextStatus = Agent.DECEASED_STATUS;
			}
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

		// The status has changed
		if (this.nextStatus != Agent.INFECTED_WITH_SYMPTOMS_STATUS) {
			context.remove(this);
			Agent a = null;
			if (this.nextStatus == Agent.RECOVERED_STATUS) {
				a = new RecoveredAgent(Agent.RANDOM_GOAL, age, atIncreasedRisk, hasMask);
				context.add(a);
			}
			else if (this.nextStatus == Agent.DECEASED_STATUS) {
				a = new DeceasedAgent();
				context.add(a);
			}
			if (this.nextPosition != null)
				grid.moveTo(a, this.nextPosition.getX(), this.nextPosition.getY());

		}
		else { // The status has not changed
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
		double ownprob = this.probInf * FACTOR_WITH_SYMPTOMS;
		if (this.wearMask())
			ownprob = ownprob * FACTOR_WITH_MASK;
		return ownprob;
	}
}
