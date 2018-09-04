package org.ggp.base.player.gamer.statemachine.huis;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;

public class StateStateMoveGoalVisitObjects implements Cloneable

{

	public MachineState thismachinestate;

	public MachineState thisnextmachinestate;

	public Move thistheMove;

	public double thisgoalFloat;

	public int thisvisitcount;

	public Move getThistheMove()

	{

		return thistheMove;

	}

	public int getThisvisitcount() {

		return thisvisitcount;

	}

	public void setThisvisitcount(int thisvisitcount) {

		this.thisvisitcount = thisvisitcount;

	}

	public void setThistheMove(Move thistheMove)

	{

		this.thistheMove = thistheMove;

	}

	public MachineState getThismachinestate()

	{

		return thismachinestate;

	}

	public void setThismachinestate(MachineState thismachinestate)

	{

		this.thismachinestate = thismachinestate;

	}

	public MachineState getThisnextmachinestate()

	{

		return thisnextmachinestate;

	}

	public void setThisnextmachinestate(MachineState thisnextmachinestate)

	{

		this.thisnextmachinestate = thisnextmachinestate;

	}

	public double getThisgoalFloat()

	{

		return thisgoalFloat;

	}

	public void setThisgoalFloat(double thisgoalFloat)

	{

		this.thisgoalFloat = thisgoalFloat;

	}

	@Override
	public StateStateMoveGoalVisitObjects clone()

	{

		StateStateMoveGoalVisitObjects ob = null;

		try

		{

			ob = (StateStateMoveGoalVisitObjects) super.clone();

			ob.thismachinestate = (MachineState) this.thismachinestate.clone();

			ob.thistheMove = (Move) this.thistheMove.clone();

			ob.thisnextmachinestate = (MachineState) this.thisnextmachinestate
					.clone();

		}

		catch (CloneNotSupportedException e)

		{

			e.printStackTrace();

		}

		return ob;

	}

}
