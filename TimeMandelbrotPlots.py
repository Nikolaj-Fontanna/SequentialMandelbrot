import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
from scipy.optimize import curve_fit

def func(x, a, b, c):
    return a * x * x + b * x + c

df = pd.read_csv("out.csv", header=None)

popt, pcov = curve_fit(func, df[0],df[1])

plt.scatter(df[0],df[1], marker='x')

xcurve = np.arange(min(df[0])-10, max(df[0])+10) # to make curve smooth
plt.plot(xcurve, func(xcurve, *popt), 'g--', label='fit: a=%5.3f, b=%5.3f, c=%5.3f' % tuple(popt))

plt.xlabel("Number of pixels")
plt.ylabel("Time in nanoseconds")
plt.title("Sequential Mandelbrot Set generation time")
plt.legend()
plt.grid(which='both')

#plt.show()

plt.savefig("TimeMandelbrot.pdf")