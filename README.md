# ggp-rl
Assessing the Potential of Classical Q-learning in General Game Playing

This is an exploration of Classical Q-learning in General Game Playing. HuiPureQLPlayers is implemented based on classical Q-learning using fixed epsilon. One enhancement is HuiSampleQLPlayer by using a dynamic epsilon strategy, the other one(HuiQLMontePlayer) is enhanced by combining online knowledge(Monte Carlo Search) to offline learning(Q-learning)

HuiMCTSPlayer is implemented using Monte Carlo Tree Search Algorithm.

More Details, please refer to https://arxiv.org/abs/1802.05944

All these players should be integreted to GGP_Base project to run, more information, please see https://github.com/ggp-org/ggp-base
