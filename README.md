# ICA Project Winterterm 20/21

## Prerequisites  

* **Java 9+**  
* **8GB RAM** available to the JVM should be sufficient, but atleast 4GB    
* During execution, the MDP and the learning results will be stored to disk (**~ 800 MB**) 
    * depending on your storage medium (HDD or SSD) I/O operations may take several minutes.
* on a medium powerful laptop expect around 70 minutes of total computation time with 2 seperate steps

## Dependencies and Installation

```
git clone -b master git@gitlab.tubit.tu-berlin.de:j.ackerschewski/ICA-2.git
```
Currently, no further dependencies are necessary as long as all dependencies listed in the pom.xml can be fulfilled by maven.

## Computation  

The following steps have to be executed sequentially:
 * run the class `run.Simulation` as main class (This will result in a file `mdp.ser` that contains the MDP of the simulation in a serialized form)
 
    Estimated computation time: **45 minutes** depending on your CPU & RAM
    * measured on i7-8550U CPU @ 1.80GHz × 8 with 16GB RAM, 8GB available to JVM, SSD
    * 35 minutes to compute the states (33049)
    * 5 minutes generate Actions
    * 1:30 minute generate transitions
    * 5 minutes generate MDP (this step includes writing to storage)
    
    Since the MDP is now serialized, you may change the learning parameters any time between runs and just need to load the precomputed MDP in the next step.
    
 * run the class `q_learning.QLearnerNetwork` as a main class (This will expect the file `mdp.ser` and apply the Q-Learning. Most parameters can be changed for this step but use the defaults for now.)  
    
    Estimated computation time: **25 minutes to load & 500k iterations** depending on your CPU & RAM
    * measured on i7-8550U CPU @ 1.80GHz × 8 with 16GB RAM, 8GB available to JVM, SSD
    * 4 minutes loading on an SSD, up to 15 minutes on a HDD
    * 23 seconds per 10k iterations or **260k iterations per 10 minutes**  
 
 The second step results in logging output to terminal containing the optimal policy which was learned.
 Detailed run data is printed to a file, which can be used by our python scripts for evaluation:  

 `runData.json` - contains: 
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
`analyzeRunData.py` - Uses the Plotter.py to plot with matplotlib  
`Plotter.py` - to plot different runs into a single figure (see analyzeRunData)  
`readParams.py` - read out the parameters that were used for a particular run  
`readPolicy.py` - read out the policy that was used for a particular run

The Scrips require matplotlib and json libraries to work.
To execute a script e.g. `readPolicy` cd into the python directory and change the filename in the script to the correct relative filename. Afterwards execute:
`python readPolicy.py`
 
 
## Evaluation of random action statistics (mean, std, ...)  
 
Please consider that due to the nature of randomness our results might not be reproducible.
Since we included a seed in our code, you should however still be able to reproduce them.
 
Check out branch random-transition-eval:
```
git fetch
git checkout -b random-transition-eval origin/random-transition-eval
```
The dependencies are different in this branch so they may need to be resolved again with maven.

To run the evaluation with self transitions enabled, run `run.Simulation` as a main class without modifications. 
This may take a while (several hours possibly) since it was developed without optimization in mind as it only needed to be executed once. A progressbar will show the current progress.

To run the evaluation without self transitions you need to compute the MDP first. 
Since this was done before refactoring on the main branch, it might be necessary to recompute the MDP:
Change the lines 50 to 58 of `run.Simulation` to the following:
```
private static final boolean SELF_TRANSITION_DISABLED = true;

    public static void main(String[] args) throws IOException {
        System.out.println("Starting simulation");
        setupWorld();
        Set<State> states = computeStates();
        MDPSerializer.computeMDP(states);
        //chooseRandomStatesUntilEnd(500000);
    }
```
Now run `run.Simulation` as a main class. This will serialize the MDP as `mdp.ser`.
Next run `q_learning.LearnRandomGreedy` as a main class, again a progressbar is included but this process should be faster.
