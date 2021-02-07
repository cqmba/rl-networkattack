import json

runData = []
with open("./../../../runData.json", "r") as f:
    runData = json.load(f)

for run in runData:
    del run['rewards']
    print(run)