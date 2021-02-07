# python3
import json
import matplotlib.pyplot as plt

# Add "]}]" to the json if it does not work right away

runData = []
with open("./../../../runDataRandomState1.json", "r") as f:
    runData = json.load(f)

rewardX1 = [x['a'] for x in runData[0]['rewards']]
rewardY1 = [x['b'] for x in runData[0]['rewards']]

lastIteration1 = rewardX1[-1]

rewardX1.extend([x['a'] + lastIteration1 for x in runData[1]['rewards']])
rewardY1.extend([x['b'] for x in runData[1]['rewards']])

for i in range(1, len(rewardY1)):
    rewardY1[i] = rewardY1[i - 1] + rewardY1[i]

runData = []
with open("./../../../runDataRandomState2.json", "r") as f:
    runData = json.load(f)

rewardX2 = [x['a'] for x in runData[0]['rewards']]
rewardY2 = [x['b'] for x in runData[0]['rewards']]

lastIteration2 = rewardX2[-1]

rewardX2.extend([x['a'] + lastIteration2 for x in runData[1]['rewards']])
rewardY2.extend([x['b'] for x in runData[1]['rewards']])

for i in range(1, len(rewardY2)):
    rewardY2[i] = rewardY2[i - 1] + rewardY2[i]

runData = []
with open("./../../../runDataRandomState3.json", "r") as f:
    runData = json.load(f)

rewardX3 = [x['a'] for x in runData[0]['rewards']]
rewardY3 = [x['b'] for x in runData[0]['rewards']]

lastIteration3 = rewardX3[-1]

rewardX3.extend([x['a'] + lastIteration3 for x in runData[1]['rewards']])
rewardY3.extend([x['b'] for x in runData[1]['rewards']])

for i in range(1, len(rewardY3)):
    rewardY3[i] = rewardY3[i - 1] + rewardY3[i]

runData = []
with open("./../../../runDataRandomState4.json", "r") as f:
    runData = json.load(f)

rewardX4 = [x['a'] for x in runData[0]['rewards']]
rewardY4 = [x['b'] for x in runData[0]['rewards']]

lastIteration4 = rewardX4[-1]

rewardX4.extend([x['a'] + lastIteration4 for x in runData[1]['rewards']])
rewardY4.extend([x['b'] for x in runData[1]['rewards']])

for i in range(1, len(rewardY4)):
    rewardY4[i] = rewardY4[i - 1] + rewardY4[i]

plt.plot(rewardX1, rewardY1, 'b',
         rewardX2, rewardY2, 'r',
         rewardX3, rewardY3, 'g',
         rewardX4, rewardY4, 'm')
plt.show()