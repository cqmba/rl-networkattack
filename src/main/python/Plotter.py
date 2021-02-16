import json
import matplotlib.pyplot as plt


class Plotter:
    def __init__(self):
        self.x_data = []
        self.y_data = []
        self.colors = []
        self.x_params = "Iterations"
        self.y_params = "Reward"
        self.legend = []

    def load(self, filename, color):
        run_data = []
        with open(filename, "r") as f:
            run_data = json.load(f)

        reward_x = []
        reward_y = []
        last_iteration = 0
        for data in run_data:
            reward_x.extend([x['a'] + last_iteration for x in data['rewards']])
            reward_y.extend([y['b'] for y in data['rewards']])

            last_iteration = reward_x[-1]

        for i in range(1, len(reward_y)):
            reward_y[i] = reward_y[i - 1] + reward_y[i]

        self.x_data = self.x_data + [reward_x]
        self.y_data = self.y_data + [reward_y]
        self.colors = self.colors + [[color]]
        for data in run_data:
            par = data['parameter']
            del par['additionalInformation']
            del par['saveQ']
            del par['seed']
            del par['error']
            self.legend = self.legend + [(str(par), color)]

    def plot(self):
        fig, ax = plt.subplots(figsize=(10, 8))
        fig.subplots_adjust(bottom=0.2)
        for i in range(len(self.x_data)):
            ax.plot(self.x_data[i], self.y_data[i], label="data " + str(i), color=self.colors[i][0])
        ax.set_xlabel(self.x_params)
        ax.set_ylabel(self.y_params)
        #for t in self.legend:
        #    ax.text(0, -100000, t[0])
        ax.legend()
        plt.show()
