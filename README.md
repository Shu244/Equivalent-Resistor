# Google Play Store Link

> Download this Android app at the [Google Play Store](...). No iOS version is currently available. 

# App Overview

> This **Android** app uses a genetic algorithm to optimally build equivalent resistors given a set of resistors, a desired resistance, and priorities for the size and accuracy of the output circuit. 

> The app displays the top 10 constructed equivalent resistors with relevant information like circuit size and difference from desired resistance. The app can also download sets of resistors from online. 

# Downloading Set Format

> The resistor set being downloaded must have the following format:

```
SetName
1000 10
100 5
...
```

> The first line is the name of the set which can only contain letters and numbers. The following lines are space delimited with the first number specifying resistance and the second specifying quantity. 

> One way to upload public resistor sets is to post a file on GitHub and use the link to the raw version of the file. The default example URL for my app follows this approach, linking to the file located [here](https://github.com/Shu244/Resistor-Sets). 

# Previews

| <img src=".\images\App\1.jpg" style="zoom:30%;" /> | <img src=".\images\App\2.jpg" style="zoom:30%;" /> | <img src=".\images\App\3.jpg" style="zoom:30%;" /> |
| :------------------------------------------------: | :------------------------------------------------: | :------------------------------------------------: |
|                       **1**                        |                       **2**                        |                       **3**                        |
| <img src=".\images\App\4.jpg" style="zoom:30%;" /> | <img src=".\images\App\5.jpg" style="zoom:30%;" /> | <img src=".\images\App\6.jpg" style="zoom:30%;" /> |
|                       **4**                        |                       **5**                        |                       **6**                        |



# Algorithm Overview

> A genetic algorithm is used to evolve the set of possible resistors to the desired resistance. In order to guide the genetic algorithm so that it grows faster and with better accuracy, I populate the initial population with some equivalent resistor DNA that are generated using a greedy approach. 

# Greedy Approach

> The greedy approach first finds the resistance that minimizes the difference between the desired resistance and the current equivalent resistance. 

> If the current equivalent resistance is smaller than the desired resistance, an optimal resistor will be added in **series**. The optimal resistor is calculated as follows:

<p align="center"><img src=".\images\Equations\series_equation.PNG" /></p>

> If the current equivalent resistance is larger than the desired resistance, an optimal resistor will be added in **parallel**. The optimal resistor is calculated as follows:

<p align="center"><img src=".\images\Equations\parallel_equation.PNG" /></p>

> Since the optimal resistors may not be in the set of available resistors, the closest match will be used instead (but only if the new resistance is closer to the desired resistance).

> Once a predefined number of greedy circuits have been built, they are converted to a DNA object and used to fill some of the initial population for the genetic algorithm.

> On inspection, the greedy approach should consider both adding a resistor in parallel and in series regardless of the current resistance. This has the potential to produce a better greedy solution; however, this addition may not make a significant difference in the output since only a minority of the initial population will be seeded with the greedy approach. Further, the genetic algorithm may still arrive at the optimal solution regardless.    

# Genetic Algorithm

## DNA

> The genetic algorithm uses two chromosomes in the DNA: one to determine which resistors from the set survives and another to order and connect the selected resistors. The details of the DNA representation can be further explored in the source code.

## Crossover

> To produce the next generation, random pairs of resistors from either parents are chosen to be in the child. 

## Fitness

> Suppose *f(x)* is a polynomial function and *S* is the size of an equivalent resistor, then the fitness is the weighted sum of the accuracy of the circuit and *f(S)*.  

## Details

> To save time, the genetic algorithm uses very aggressive early stopping: the algorithm stops once fitness does not increase from the previous generation. This is used since speed is crucial for smooth operations on Android phones. 

> The other parameters of the genetic algorithm (mutation rate, population size, number of generations, etc.) can be found in the source code. 

## Exporting

> The genetic algorithm is independent and entirely contained in the *optimizer* package. The package can return a queue that holds information about the an equivalent resistor (like size and total resistance) as well as instructions on how to build the equivalent resistor. The instructions are represented in [Reverse Polish Notation](https://en.wikipedia.org/wiki/Reverse_Polish_notation ). Please refer to the source code for more information.
