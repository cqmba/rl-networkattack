# python3
from src.main.python.Plotter import Plotter

plotter = Plotter()

plotter.load("./../../../runData.json", "r")
plotter.plot()
