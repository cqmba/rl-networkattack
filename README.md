# ICA Project Winterterm 20/21

## Prerequisites 
JVM Version 9+  
Probably atleast 4GB RAM available to the JVM, 8GB to be sure  
During execution, the states, MDP and the results may be serialized to disk. 
This can use storage space up to 100 MB and depending on your storage medium (HDD or SSD) I/O operations may take several minutes (up to 30 minutes for a slow HDD, but expect somewhere between 5-20 minutes).

## Dependencies and Installation
```
git clone -b master git@gitlab.tubit.tu-berlin.de:j.ackerschewski/ICA-2.git
```
Currently, no further dependencies are necessary as long as all dependencies listed in the pom.xml can be fulfilled by maven.

## Computation
The following steps have to be executed sequentially:
 * run the class `run.Simulation` as main class (This will result in a file `mdp.ser` that contains the MDP of the simulation in a serialized form)
 
    Estimated computation time: **45 minutes** 
    * measured on i7-8550U CPU @ 1.80GHz × 8 with 16GB RAM, 8GB available to JVM, SSD
    * 35 minutes to compute the states (33049)
    * 5 minutes generate Actions
    * 1:30 minute generate transitions
    * 5 minutes generate MDP (this step includes writing to storage)
 * run the class `q_learning.QLearnerNetwork` as a main class (This will expect the file `mdp.ser` and apply the Q-Learning. Most parameters can be changed for this step, yet the defaults can be applied.)  
 
 The third step results in logging output to terminal containing the optimal policy which was learned.
 Detailed run data is printed to a file, which can be used by our python scripts for evaluation:  

 `runData.json` - contains 
 * all parameters used for the run including a description, the rewards, the policy rewards, the optimal policy and if enabled, Q
 * rewards - a list of Integer (current iteration) and Double (reward of this iteration) pairs `List<Pair<Integer, Double>>`
 * policy - a list of State & Action object pairs `List<Pair<S, A>>` 
 * policyReward - the cumulative reward
 * byte[] Q - not included by default and discouraged to use (may produce errors when saving/loading the file)
 
 ## Changing run parameters
 The following parameters for Q-Learning can be set in the class `q_learning.QLearnerNetwork` and are explained in the code in the class `q_learning.utils.Parameter`:  
 `learningRateMaxCount`
 `learningRateStartValue`
 `learningRateEndValue`
 `learningRateSlope`
 `epsilonStartValue`
 `epsilonEndValue`
 `epsilonSlope`
 `discountFactor`
 `seed`
 `error`
 `ne`
 `r_plus`
 `iterations`
 `initialIterations`
 `additionalInformation`
 `saveQ` 
 
 The report also lists our settings for the different evaluations.
 
 ## Evaluation with Python
 scripts are located at `src/main/python`:  
 `analyzeRunData.py` -   
 `Plotter.py` - to plot different runs into a single figure  
 `readParams.py` - read out the parameters that were used for a particular run  
 `readPolicy.py` - read out the policy that was used for a particular run  
 
 To execute a script e.g. `readPolicy` cd into the python directory and change the filename in the script to the correct relative filename. Afterwards execute: 
 `python readPolicy.py`
 
 ## Recreating evaluation of random actions (mean, std, ...)
 
Check out branch random-transition-eval:
```
git fetch
git checkout -b random-transition-eval origin/random-transition-eval
```