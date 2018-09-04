package org.ggp.base.player.gamer.statemachine.huis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

/**
 *
 *
 *
 * HuiPureQLPlayer is a general gamer which applies the Q-Learning
 *
 * Algorithm. Because the Q-Learning Algorithm is model-free, which implies that
 *
 * it can learn knowledge from the situation that agent doesn't know the
 *
 * transition mechanism and reward function, Q-Learning just knows the current
 *
 * state of the game and the action set under current state. QL uses exploration
 *
 * and exploitation strategies(eps-greedy). this player may be used to play the
 *
 * zero-sum games etc.
 *
 * fixed epsilon=0.01, fixed learning rate 0.1
 *
 * @author wanghui created in 29-01-2017 (dd-mm-yyyy)
 */

public class HuiPureQLPlayer extends StateMachineGamer

{

	private StateMachine theMachine;

	private Role myRole;

	private Role opponentRole;

	StateStateMoveGoalVisitObjects statestategoalvisitobjects;

	int learningmatch = 30000;

	int pertorecordlearningresult = learningmatch / 10;

	int recordgoal = 0;

	int matchcount = 0;

	int stepcount = 0;

	int alivescore = 0;

	double initiallearningrate = 0.3;

	double discountfactor = 1.0;

	double lambda = 0.7;

	int initialarrlength = 100;

	int initialarrwidth = 16;

	int addarrlength = 100;

	int addarrwidth = 10;

	long getrewardfinishBy = 20;

	StateStateMoveGoalVisitObjects[][] qlarr = new StateStateMoveGoalVisitObjects[initialarrlength][initialarrwidth];

	StateStateMoveGoalVisitObjects[][] qlarrsecond = new StateStateMoveGoalVisitObjects[initialarrlength][initialarrwidth];

	StateStateMoveGoalVisitObjects[] stepsofeachgame;

	Set<GdlSentence> laststatehistory;

	MachineState lastendmachinestate;

	@Override
	public void stateMachineMetaGame(long timeout)

	throws TransitionDefinitionException, MoveDefinitionException,

	GoalDefinitionException

	{

		// TODO Auto-generated method stub

		theMachine = getStateMachine();

		myRole = getRole();

		List<Role> Roles = theMachine.getRoles();

		for (Role role : Roles)

		{

			if (!role.equals(myRole))

			{

				opponentRole = role;

			}

		}

		if (matchcount % 2 == 0)

		{

			if (qlarr.length == matchcount / 2)

			{

				StateStateMoveGoalVisitObjects[][] newqlarr = new StateStateMoveGoalVisitObjects[qlarr.length

						+ addarrlength][qlarr[0].length];

				for (int i = 0; i < qlarr.length; i++)

				{

					System.arraycopy(qlarr[i], 0, newqlarr[i], 0,

					qlarr[i].length);

				}

				qlarr = new StateStateMoveGoalVisitObjects[newqlarr.length][newqlarr[0].length];

				for (int i = 0; i < newqlarr.length; i++)

				{

					System.arraycopy(newqlarr[i], 0, qlarr[i], 0,

					newqlarr[i].length);

				}

			}

			if (qlarr[0].length < stepcount + 1)

			{

				int newaddarrwidth = stepcount - qlarr[0].length + 1;

				StateStateMoveGoalVisitObjects[][] newqlarr = new StateStateMoveGoalVisitObjects[qlarr.length][qlarr[0].length

						+ newaddarrwidth];

				for (int i = 0; i < qlarr.length; i++)

				{

					System.arraycopy(qlarr[i], 0, newqlarr[i], 0,

					qlarr[i].length);

				}

				qlarr = new StateStateMoveGoalVisitObjects[newqlarr.length][newqlarr[0].length];

				for (int i = 0; i < newqlarr.length; i++)

				{

					System.arraycopy(newqlarr[i], 0, qlarr[i], 0,

					newqlarr[i].length);

				}

			}

			if (matchcount >= 2)

			{

				int secondlaststateflag = 0;

				for (int step = stepsofeachgame.length - 1; step >= 0; step--)

				{

					if (stepsofeachgame[step] != null)

					{

						secondlaststateflag += 1;

						boolean existoldstateactiongoal = false;

						if (secondlaststateflag == 1)

						{

							stepsofeachgame[step].thisnextmachinestate = (MachineState) lastendmachinestate

							.clone();

						}

						else

						{

							stepsofeachgame[step].thisnextmachinestate = (MachineState) stepsofeachgame[step + 1].thismachinestate

							.clone();

						}

						for (int i = 0; i < qlarr.length; i++)

						{

							for (int j = 0; j < qlarr[0].length; j++)

							{

								if (qlarr[i][j] != null)

								{

									if (stepsofeachgame[step].thismachinestate

									.equals(qlarr[i][j].thismachinestate)

											&& stepsofeachgame[step].thisnextmachinestate

													.equals(qlarr[i][j].thisnextmachinestate)

											&& stepsofeachgame[step].thistheMove

											.equals(qlarr[i][j].thistheMove))

									{

										qlarr[i][j] = computeQValue(

												stepsofeachgame[step].thismachinestate,

												stepsofeachgame[step].thisnextmachinestate,

												stepsofeachgame[step].thistheMove,
												i, j,
												qlarr[i][j].thisvisitcount);

										qlarr[matchcount / 2 - 1][step] = null;

										existoldstateactiongoal = true;

									}

								}

							}

						}

						if (!existoldstateactiongoal)

						{

							qlarr[matchcount / 2 - 1][step] = computeQValue(

							stepsofeachgame[step].thismachinestate,

							stepsofeachgame[step].thisnextmachinestate,

							stepsofeachgame[step].thistheMove, -1, -1, 0);

						}

					}

				}

			}

		}

		if (matchcount % 2 == 1)

		{

			if (qlarrsecond.length == matchcount / 2)

			{

				StateStateMoveGoalVisitObjects[][] newqlarr = new StateStateMoveGoalVisitObjects[qlarrsecond.length

						+ addarrlength][qlarrsecond[0].length];

				for (int i = 0; i < qlarrsecond.length; i++)

				{

					System.arraycopy(qlarrsecond[i], 0, newqlarr[i], 0,

					qlarrsecond[i].length);

				}

				qlarrsecond = new StateStateMoveGoalVisitObjects[newqlarr.length][newqlarr[0].length];

				for (int i = 0; i < newqlarr.length; i++)

				{

					System.arraycopy(newqlarr[i], 0, qlarrsecond[i], 0,

					newqlarr[i].length);

				}

			}

			if (qlarrsecond[0].length < stepcount + 1)

			{

				int newaddarrwidth = stepcount - qlarr[0].length + 1;

				StateStateMoveGoalVisitObjects[][] newqlarr = new StateStateMoveGoalVisitObjects[qlarrsecond.length][qlarr[0].length

						+ newaddarrwidth];

				for (int i = 0; i < qlarrsecond.length; i++)

				{

					System.arraycopy(qlarrsecond[i], 0, newqlarr[i], 0,

					qlarrsecond[i].length);

				}

				qlarrsecond = new StateStateMoveGoalVisitObjects[newqlarr.length][newqlarr[0].length];

				for (int i = 0; i < newqlarr.length; i++)

				{

					System.arraycopy(newqlarr[i], 0, qlarrsecond[i], 0,

					newqlarr[i].length);

				}

			}

			if (matchcount >= 1)

			{

				int secondlaststateflag = 0;

				for (int step = stepsofeachgame.length - 1; step >= 0; step--)

				{

					if (stepsofeachgame[step] != null)

					{

						secondlaststateflag += 1;

						boolean existoldstateactiongoal = false;

						if (secondlaststateflag == 1)

						{

							stepsofeachgame[step].thisnextmachinestate = (MachineState) lastendmachinestate

							.clone();

						}

						else

						{

							stepsofeachgame[step].thisnextmachinestate = (MachineState) stepsofeachgame[step + 1].thismachinestate

							.clone();

						}

						for (int i = 0; i < qlarrsecond.length; i++)

						{

							for (int j = 0; j < qlarrsecond[0].length; j++)

							{

								if (qlarrsecond[i][j] != null)

								{

									if (stepsofeachgame[step].thismachinestate

									.equals(qlarrsecond[i][j].thismachinestate)

											&& stepsofeachgame[step].thisnextmachinestate

													.equals(qlarrsecond[i][j].thisnextmachinestate)

											&& stepsofeachgame[step].thistheMove

													.equals(qlarrsecond[i][j].thistheMove))

									{

										qlarrsecond[i][j] = computeQValue(

												stepsofeachgame[step].thismachinestate,

												stepsofeachgame[step].thisnextmachinestate,

												stepsofeachgame[step].thistheMove,

												i,
												j,
												qlarrsecond[i][j].thisvisitcount);

										qlarrsecond[(matchcount - 1) / 2][step] = null;

										existoldstateactiongoal = true;

									}

								}

							}

						}

						if (!existoldstateactiongoal)

						{

							qlarrsecond[(matchcount - 1) / 2][step] = computeQValue(

							stepsofeachgame[step].thismachinestate,

							stepsofeachgame[step].thisnextmachinestate,

							stepsofeachgame[step].thistheMove, -1, -1, 0);

						}

					}

				}

			}

		}

		/*
		 * if (matchcount % 200 == 0)
		 *
		 * {
		 *
		 * String filePath = "P:\\Desktop\\qlearning1.txt";
		 *
		 * File fileResult = new File(filePath);
		 *
		 * FileWriter fw;
		 *
		 * try
		 *
		 * {
		 *
		 * fw = new FileWriter(fileResult);
		 *
		 * // rewrite,clear previous content
		 *
		 * // fw = new FileWriter(fileResult, true);
		 *
		 * // add new content and keep previous content
		 *
		 * BufferedWriter br1 = new BufferedWriter(fw);
		 *
		 * for (int i = 0; i < qlarr.length; i++)
		 *
		 * {
		 *
		 * br1.write("This is the " + i + "th episode to learn qlarr:");
		 *
		 * br1.newLine();
		 *
		 * for (int j = 0; j < qlarr[0].length; j++)
		 *
		 * {
		 *
		 * if (qlarr[i][j] != null)
		 *
		 * {
		 *
		 * br1.write("qlarr[" + i + "][" + j
		 *
		 * + "].thismachinestate:"
		 *
		 * + qlarr[i][j].thismachinestate);
		 *
		 * br1.newLine();
		 *
		 * br1.write("qlarr[" + i + "][" + j
		 *
		 * + "].thistheMove:"
		 *
		 * + qlarr[i][j].thistheMove);
		 *
		 * br1.newLine();
		 *
		 * br1.write("qlarr[" + i + "][" + j
		 *
		 * + "].thisnextmachinestate:"
		 *
		 * + qlarr[i][j].thisnextmachinestate);
		 *
		 * br1.newLine();
		 *
		 * br1.write("qlarr[" + i + "][" + j
		 *
		 * + "].thisgoalFloat:"
		 *
		 * + qlarr[i][j].thisgoalFloat);
		 *
		 * br1.newLine(); br1.write("qlarr[" + i + "][" + j
		 *
		 * + "].thisvisitcount:"
		 *
		 * + qlarr[i][j].thisvisitcount); br1.newLine();
		 *
		 * }
		 *
		 * else
		 *
		 * {
		 *
		 * br1.write("qlarr[" + i + "][" + j + "]: null");
		 *
		 * br1.newLine();
		 *
		 * }
		 *
		 * }
		 *
		 * }
		 *
		 * br1.close();
		 *
		 * }
		 *
		 * catch (IOException e)
		 *
		 * {
		 *
		 * // TODO Auto-generated catch block
		 *
		 * e.printStackTrace();
		 *
		 * }
		 *
		 * }
		 *
		 * if (matchcount % 200 == 0)
		 *
		 * {
		 *
		 * String filePath = "P:\\Desktop\\qlearning2.txt";
		 *
		 * File fileResult = new File(filePath);
		 *
		 * FileWriter fw;
		 *
		 * try
		 *
		 * {
		 *
		 * fw = new FileWriter(fileResult);
		 *
		 * BufferedWriter br1 = new BufferedWriter(fw);
		 *
		 * for (int i = 0; i < qlarrsecond.length; i++)
		 *
		 * {
		 *
		 * br1.write("This is the " + i
		 *
		 * + "th episode to learn qlarrsecond:");
		 *
		 * br1.newLine();
		 *
		 * for (int j = 0; j < qlarrsecond[0].length; j++)
		 *
		 * {
		 *
		 * if (qlarrsecond[i][j] != null)
		 *
		 * {
		 *
		 * br1.write("qlarrsecond[" + i + "][" + j
		 *
		 * + "].thismachinestate:"
		 *
		 * + qlarrsecond[i][j].thismachinestate);
		 *
		 * br1.newLine();
		 *
		 * br1.write("qlarrsecond[" + i + "][" + j
		 *
		 * + "].thistheMove:"
		 *
		 * + qlarrsecond[i][j].thistheMove);
		 *
		 * br1.newLine();
		 *
		 * br1.write("qlarrsecond[" + i + "][" + j
		 *
		 * + "].thisnextmachinestate:"
		 *
		 * + qlarrsecond[i][j].thisnextmachinestate);
		 *
		 * br1.newLine();
		 *
		 * br1.write("qlarrsecond[" + i + "][" + j
		 *
		 * + "].thisgoalFloat:"
		 *
		 * + qlarrsecond[i][j].thisgoalFloat);
		 *
		 * br1.newLine(); br1.write("qlarrsecond[" + i + "][" + j
		 *
		 * + "].thisvisitcount:"
		 *
		 * + qlarrsecond[i][j].thisvisitcount); br1.newLine();
		 *
		 * }
		 *
		 * else
		 *
		 * {
		 *
		 * br1.write("qlarrsecond[" + i + "][" + j + "]: null");
		 *
		 * br1.newLine();
		 *
		 * }
		 *
		 * }
		 *
		 * }
		 *
		 * br1.close();
		 *
		 * }
		 *
		 * catch (IOException e)
		 *
		 * {
		 *
		 * // TODO Auto-generated catch block
		 *
		 * e.printStackTrace();
		 *
		 * }
		 *
		 * }
		 */

		matchcount += 1;

		stepcount = 0;

		stepsofeachgame = new StateStateMoveGoalVisitObjects[initialarrwidth];

	}

	@Override
	public Move stateMachineSelectMove(long timeout)

	throws TransitionDefinitionException, MoveDefinitionException,

	GoalDefinitionException

	{

		// TODO Auto-generated method stub

		// We get the current start time

		long start = System.currentTimeMillis();

		List<Move> moves = theMachine.getLegalMoves(getCurrentState(), myRole);

		Move selection = moves.get(new Random().nextInt(moves.size()));

		double maxqscore = -1;

		boolean enableepsilongreedy = false;

		enableepsilongreedy = enableEpsilonGreedy(matchcount);

		if (matchcount % 2 == 0)

		{

			if (!enableepsilongreedy)

			{// epsilon=1/matchcount how to use a new epsilon as

				// pow(1/matchcount,q)//q=0.5*(cos(x*pi*power(learningmatch,-1))+1);

				for (int i = 0; i < qlarrsecond.length; i++)

				{

					for (int j = 0; j < qlarrsecond[0].length; j++)

					{

						if (qlarrsecond[i][j] != null)

						{

							if (getCurrentState().equals(

							qlarrsecond[i][j].thismachinestate))

							{

								if (maxqscore < qlarrsecond[i][j].thisgoalFloat)

								{

									if (moves

									.contains(qlarrsecond[i][j].thistheMove))

									{

										selection = qlarrsecond[i][j].thistheMove

										.clone();

										maxqscore = qlarrsecond[i][j].thisgoalFloat;

									}

								}

							}

						}

					}

				}

			}

			else

			{

				int learntactionsize = 1;

				Move[] learntaction = new Move[learntactionsize];

				Move[] templearntaction = new Move[learntactionsize];

				for (int i = 0; i < qlarrsecond.length; i++)

				{

					for (int j = 0; j < qlarrsecond[0].length; j++)

					{

						if (qlarrsecond[i][j] != null)

						{

							if (getCurrentState().equals(

							qlarrsecond[i][j].thismachinestate))

							{

								learntaction[learntactionsize - 1] = qlarrsecond[i][j].thistheMove

								.clone();

								System.arraycopy(learntaction, 0,

								templearntaction, 0,

								learntaction.length);

								learntactionsize += 1;

								learntaction = new Move[learntactionsize];

								System.arraycopy(templearntaction, 0,

								learntaction, 0,

								templearntaction.length);

								templearntaction = new Move[learntactionsize];

							}

						}

					}

				}

				// boolean allcomputed = false;

				for (int se = 0; se < moves.size(); se++)

				{

					boolean same = false;

					for (int le = 0; le < learntaction.length; le++)

					{

						if (moves.get(se).equals(learntaction[le]))

						{

							same = true;

							break;

						}

					}

					if (!same)

					{

						// allcomputed = true;

						selection = moves.get(se);

						break;

					}

				}

			}

		}

		if (matchcount % 2 == 1)

		{

			if (!enableepsilongreedy)

			{// epsilon=1/matchcount how to use a new epsilon as

				// pow(1/matchcount,q)//q=0.5*(cos(x*pi*power(learningmatch,-1))+1);

				for (int i = 0; i < qlarr.length; i++)

				{

					for (int j = 0; j < qlarr[0].length; j++)

					{

						if (qlarr[i][j] != null)

						{

							if (getCurrentState().equals(

							qlarr[i][j].thismachinestate))

							{

								if (maxqscore < qlarr[i][j].thisgoalFloat)

								{

									if (moves.contains(qlarr[i][j].thistheMove))

									{

										selection = qlarr[i][j].thistheMove

										.clone();

										maxqscore = qlarr[i][j].thisgoalFloat;

									}

								}

							}

						}

					}

				}

			}

			else

			{

				int learntactionsize = 1;

				Move[] learntaction = new Move[learntactionsize];

				Move[] templearntaction = new Move[learntactionsize];

				for (int i = 0; i < qlarr.length; i++)

				{

					for (int j = 0; j < qlarr[0].length; j++)

					{

						if (qlarr[i][j] != null)

						{

							if (getCurrentState().equals(

							qlarr[i][j].thismachinestate))

							{

								learntaction[learntactionsize - 1] = qlarr[i][j].thistheMove

								.clone();

								System.arraycopy(learntaction, 0,

								templearntaction, 0,

								learntaction.length);

								learntactionsize += 1;

								learntaction = new Move[learntactionsize];

								System.arraycopy(templearntaction, 0,

								learntaction, 0,

								templearntaction.length);

								templearntaction = new Move[learntactionsize];

							}

						}

					}

				}

				// boolean allcomputed = false;

				for (int se = 0; se < moves.size(); se++)

				{

					boolean same = false;

					for (int le = 0; le < learntaction.length; le++)

					{

						if (moves.get(se).equals(learntaction[le]))

						{

							same = true;

							break;

						}

					}

					if (!same)

					{

						// allcomputed = true;

						selection = moves.get(se);

						break;

					}

				}

			}

		}

		if (stepsofeachgame.length == stepcount)

		{

			StateStateMoveGoalVisitObjects[] newstepsofeachgame = new StateStateMoveGoalVisitObjects[stepsofeachgame.length

					+ addarrwidth];

			System.arraycopy(stepsofeachgame, 0, newstepsofeachgame, 0,

			stepsofeachgame.length);

			stepsofeachgame = new StateStateMoveGoalVisitObjects[newstepsofeachgame.length];

			System.arraycopy(newstepsofeachgame, 0, stepsofeachgame, 0,

			newstepsofeachgame.length);

		}

		StateStateMoveGoalVisitObjects eachstep = new StateStateMoveGoalVisitObjects();

		eachstep.thisgoalFloat = 0;

		eachstep.thismachinestate = (MachineState) getCurrentState().clone();

		// using currentstate as nextstate to avoid null pointer error while

		// call clone(), the real nextstate will be given to the objects after

		// current match ends.

		eachstep.thisnextmachinestate = (MachineState) getCurrentState()

		.clone();

		eachstep.thistheMove = (Move) selection.clone();

		eachstep.thisvisitcount = 1;

		stepsofeachgame[stepcount] = (StateStateMoveGoalVisitObjects) eachstep

		.clone();

		stepcount += 1;

		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop

		- start));

		return selection;

	}

	private boolean enableEpsilonGreedy(int matchcount2)

	{

		// TODO Auto-generated method stub

		double epsilon = 0.01;

		/*
		 * if (matchcount2 >= learningmatch)
		 *
		 * {// while matchcount2 >= learningmatch, we absolutely believe QTable
		 *
		 * return false;
		 *
		 * }
		 */

		// epsilon = 0.01;
		// epsilon from 0.5 to 0
		/*
		 * epsilon = (double) 0.5
		 *
		 * (Math.cos(matchcount2 * Math.PI
		 *
		 * Math.pow(2 * learningmatch, -1)) + 1) - 0.5;
		 */

		// epsilon from 1 to 0

		/*
		 * epsilon = (double) 0.5
		 *
		 * (Math.cos(matchcount2 * Math.PI
		 *
		 * Math.pow(learningmatch, -1)) + 1);
		 */

		double randomNumber;

		randomNumber = Math.random();

		if (randomNumber >= 0 && randomNumber <= epsilon)

		{

			return true;

		}

		else

		{

			return false;

		}

	}

	private StateStateMoveGoalVisitObjects computeQValue(
			MachineState machineState,

			MachineState nextmachinestate, Move selection, int qlarrrow,

			int qlarrcolumn, int visitcount) throws MoveDefinitionException,

	TransitionDefinitionException, GoalDefinitionException

	{

		// TODO Auto-generated method stub

		// Q(state,action)= Q(state,action) + alpha * (R(state,action) + gamma *

		// Max(next state, all actions) - Q(state,action))

		double newqvalue = -1;

		double oldqvalue = -1;

		double rewardscore = -1;

		double maxqvalueofnextstate = -1;

		if (qlarrrow == -1)

		{// no record in Q table

			oldqvalue = 0;

		}

		else

		{// there is a record in Q table

			oldqvalue = getOldQValue(qlarrrow, qlarrcolumn);

		}

		// rewardscore = getRewardValue(machineState, selection);

		rewardscore = getRewardValueaLive(machineState, nextmachinestate,

		selection);

		maxqvalueofnextstate = getMaxNextStateQvalue(machineState,

		nextmachinestate, selection);

		// computeQValue

		double learningrate = 0.3;

		// double learningrate = updateLearningRate(visitcount);

		double eligibity = updateEligibilityTrace(visitcount);

		newqvalue = (double) oldqvalue
				+ learningrate
				* eligibity
				* (rewardscore + discountfactor * maxqvalueofnextstate - oldqvalue);

		// newqvalue = (double) (1 - learningrate) * oldqvalue + learningrate
		//
		// * (rewardscore + discountfactor * maxqvalueofnextstate);

		StateStateMoveGoalVisitObjects StateStateMoveGoalVisitObjects = new StateStateMoveGoalVisitObjects();

		StateStateMoveGoalVisitObjects.thisgoalFloat = newqvalue;

		StateStateMoveGoalVisitObjects.thismachinestate = (MachineState) machineState

		.clone();

		StateStateMoveGoalVisitObjects.thisnextmachinestate = (MachineState) nextmachinestate

		.clone();

		StateStateMoveGoalVisitObjects.thistheMove = (Move) selection.clone();

		StateStateMoveGoalVisitObjects.thisvisitcount = visitcount + 1;

		return StateStateMoveGoalVisitObjects;

	}

	private double updateEligibilityTrace(int visitcount) {
		double elgibilitytrace = 0;
		if (visitcount == 0) {
			elgibilitytrace = elgibilitytrace + 1;
		} else {
			elgibilitytrace = discountfactor * lambda * (elgibilitytrace + 1);
		}
		for (int i = 0; i < visitcount; visitcount--) {
			elgibilitytrace = discountfactor * lambda * (elgibilitytrace + 1);
		}
		return elgibilitytrace;
	}

	private double updateLearningRate(int visitcount) {
		// TODO Auto-generated method stub

		visitcount = visitcount + 1;

		double newlearningrate = (double) initiallearningrate
				* (1 / (Math.pow(visitcount, -0.5)));
		// known from paper Learning Rates for Q-learning
		return newlearningrate;
	}

	private double getRewardValueaLive(MachineState machineState,

	MachineState nextmachinestate, Move selection)

	throws GoalDefinitionException, MoveDefinitionException,

	TransitionDefinitionException

	{

		// TODO Auto-generated method stub

		double reward = -1;

		if (!theMachine.isTerminal(nextmachinestate))

		{

			reward = alivescore;

		}

		else if (theMachine.isTerminal(nextmachinestate))

		{

			reward = theMachine.getGoal(nextmachinestate, opponentRole);

		}

		return reward;

	}

	private double getMaxNextStateQvalue(MachineState machineState,

	MachineState nextmachinestate, Move selection)

	throws MoveDefinitionException, TransitionDefinitionException,

	GoalDefinitionException

	{

		// TODO Auto-generated method stub

		double maxqvalue = 0;

		if (!theMachine.isTerminal(nextmachinestate))

		{

			List<Move> movesofnextstate = theMachine.getLegalMoves(

			nextmachinestate, opponentRole);

			if (matchcount % 2 == 0)

			{

				for (int i = 0; i < qlarr.length; i++)

				{

					for (int j = 0; j < qlarr[0].length; j++)

					{

						if (qlarr[i][j] != null)

						{

							if (nextmachinestate

							.equals(qlarr[i][j].thismachinestate))

							{

								if (movesofnextstate

								.contains(qlarr[i][j].thistheMove))

								{

									if (maxqvalue < qlarr[i][j].thisgoalFloat)

									{

										maxqvalue = qlarr[i][j].thisgoalFloat;

										// System.out

										// .println("Find the maxqvalue.");

									}

								}

							}

						}

					}

				}

			}

			else

			{

				for (int i = 0; i < qlarrsecond.length; i++)

				{

					for (int j = 0; j < qlarrsecond[0].length; j++)

					{

						if (qlarrsecond[i][j] != null)

						{

							if (nextmachinestate

							.equals(qlarrsecond[i][j].thismachinestate))

							{

								if (movesofnextstate

								.contains(qlarrsecond[i][j].thistheMove))

								{

									if (maxqvalue < qlarrsecond[i][j].thisgoalFloat)

									{

										maxqvalue = qlarrsecond[i][j].thisgoalFloat;

										// System.out

										// .println("Find the maxqvalue.");

									}

								}

							}

						}

					}

				}

			}

		}

		else if (theMachine.isTerminal(nextmachinestate))

		{

			maxqvalue = theMachine.getGoal(nextmachinestate, opponentRole);

		}

		return maxqvalue;

	}

	private double getOldQValue(int qlarrrow, int qlarrcolumn)

	{

		// TODO Auto-generated method stub

		if (matchcount % 2 == 0)

		{

			return qlarr[qlarrrow][qlarrcolumn].thisgoalFloat;

		}

		else

		{

			return qlarrsecond[qlarrrow][qlarrcolumn].thisgoalFloat;

		}

	}

	private int[] depth = new int[1];

	int performDepthChargeFromMove(MachineState theState, Move myMove)

	{

		StateMachine theMachine = getStateMachine();

		try

		{

			MachineState finalState = theMachine.performDepthCharge(

			theMachine.getRandomNextState(theState, getRole(), myMove),

			depth);

			return theMachine.getGoal(finalState, getRole());

		}

		catch (Exception e)

		{

			e.printStackTrace();

			return 0;

		}

	}

	@Override
	public void stateMachineStop() throws GoalDefinitionException

	{

		// TODO Auto-generated method stub

		laststatehistory = getMatch().getMostRecentState();

		lastendmachinestate = getStateMachine()

		.getMachineStateFromSentenceList(laststatehistory);

		recordgoal += getStateMachine().getGoal(getCurrentState(), myRole);

		if (pertorecordlearningresult == matchcount) {
			int opponentgoal = pertorecordlearningresult * 100 - recordgoal;
			String filePath = "P:\\Desktop\\qlearningresult.txt";
			File fileResult = new File(filePath);
			FileWriter fw;
			try {
				fw = new FileWriter(fileResult, true);
				BufferedWriter br1 = new BufferedWriter(fw);
				br1.write("SampleHuiQL winscore:" + recordgoal + "during"
						+ pertorecordlearningresult + "matches");
				br1.newLine();
				System.out.println("SampleHuiQL winscore:" + recordgoal
						+ "during" + pertorecordlearningresult + "matches");
				br1.write("opponent winscore:" + opponentgoal + "during"
						+ pertorecordlearningresult + "matches");
				System.out.println("opponent winscore:" + opponentgoal
						+ "during" + pertorecordlearningresult + "matches");
				br1.newLine();
				br1.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			pertorecordlearningresult += learningmatch / 10;
		}

		// System.gc();

	}

	@Override
	public void stateMachineAbort()

	{

		// TODO Auto-generated method stub

	}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException

	{

		// TODO Auto-generated method stub

	}

	@Override
	public String getName()

	{

		// TODO Auto-generated method stub

		return getClass().getSimpleName();

	}

	@Override
	public DetailPanel getDetailPanel()

	{

		return new SimpleDetailPanel();

	}

	@Override
	public StateMachine getInitialStateMachine()

	{

		// TODO Auto-generated method stub

		return new CachedStateMachine(new ProverStateMachine());

	}

}
