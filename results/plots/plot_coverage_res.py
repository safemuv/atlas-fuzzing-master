#!/usr/bin/python
import csv
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.collections import PatchCollection
from matplotlib.patches import Rectangle
from numpy import genfromtxt

# Load the result files
filename = "/home/jharbin/academic/atlas/atlas-middleware/results/12_05_2020_2353/SPEEDFAULT-ELLA_goalDiscovery.res"
my_data = genfromtxt(filename, delimiter=',')

fault_nums = my_data[:,0]
[index,counts] = np.unique(fault_nums, return_counts=True)

# Get the start end time range for the particular experiment...
starts = my_data[:,2]
ends = my_data[:,3]
lengths = ends - starts

# Need to create a rectange for each of the fault definitions at starts and ends

fig,ax  = plt.subplots(1)

for i in range(0,len(index)):
    v_for_cell = counts[i] / 6
    flines = np.where(fault_nums==i)
    fline = flines[0][0]
    rect = Rectangle((starts[fline],lengths[fline]),lengths[fline],lengths[fline], color=(1,v_for_cell,1))
    ax.add_patch(rect)
    # How to set the colour

plt.ylim(0,2000);
plt.xlim(0,1000);
plt.xlabel("Fault time range");
plt.ylabel("Fault time length");
plt.title("Impact of faults upon missed detections by robot");
plt.show()

