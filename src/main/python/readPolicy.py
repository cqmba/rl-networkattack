import json

runData = []
with open("./../../../results.normal/runData50.json", "r") as f:
    runData = json.load(f)

for run in runData:
    del run['rewards']
    print("policy length (actions done):", len(run['policy']) - 1)
    print("policy reward:", run['policyReward'])
    policy = run['policy']
    prev_actor = ""
    for pair in policy:
        if 'b' in pair:
            action = pair['b']
            if prev_actor != action['currentActor']:
                prev_actor = action['currentActor']
                print("Active Host:", prev_actor, "\tTarget:", action['target'], "\tAction:", action['action'])
            else :
                print("\t\t\tTarget:", action['target'], "\tAction:", action['action'])
