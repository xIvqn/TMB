![Citation Badge](https://api.juleskreuer.eu/citation-badge.php?doi=YOURDOI)
![GitHub last commit](https://img.shields.io/github/last-commit/xIvqn/TMB) 
![GitHub Repo stars](https://img.shields.io/github/stars/xIvqn/TMB)

# A Scalable GRASP Algorithm for the Targeted Misinformation Blocking Problem

...

- Journal: XXX
- Impact Factor: XXX
- Paper link: XXX
- Area: XXX
- Quartil: XXX

## Datasets

The datasets for this projects were obtained from the [SNAP](https://snap.stanford.edu/index.html) repository, including:

* [Brightkite_edges](https://snap.stanford.edu/data/loc-Brightkite.html)
* [CA-HepTh](https://snap.stanford.edu/data/ca-HepTh.html)
* [as20000102](https://snap.stanford.edu/data/as-733.html)
* [p2p-Gnutella04](https://snap.stanford.edu/data/p2p-Gnutella04.html)
* [soc-sign-bitcoin-otc](https://snap.stanford.edu/data/soc-sign-bitcoin-otc.html)
* [web-Stanford](https://snap.stanford.edu/data/web-Stanford.html)

Those datasets were modified to add the information regarding the source $S$ node (Line 2) and node weight for the Linear Threshold model (Line 3).

For each instance, 2 sub-instances were created depending on how the $S$ set was created. This could be:

* `_RANDOM`: $S$ created randomly using a prython script to fetch random nodes from the graph.
* `_MOST`: $S$ created using the [MPIF project, by Lozano-Osorio et al.](https://doi.org/10.1111/itor.13468) to get the 100 most influential nodes in each graph.

Also, to compute the solution using the pagerank greedy criteria, `.pagerank` files were created to store the nodes values.

## Repository contents

- `code`: Source code of the Java project.
- `instances`: The instances used in this project, with the MOST influential nodes and RANDOM ones as sources, and their corresponding `.pagerank` files.
- `tmb-1.0.jar`: It is the final executable of the application; further information is provided below.
- `results.xlsx`: A file containing the best and average values of the proprosal and the comparision with the FastPIDS results by Sun et al.
- ...

## Executable

You can just run the `tmb-1.0.jar` as follows.

```bash
java -XmsXXX -XmxXXX -jar tmb-1.0.jar -i "./instances" -o "./outputs/results.csv" -n ...
```

If you want to customize the execution, you can use the following command to know how to do it.

```bash
java -jar tmb-1.0.jar --help
```

Once you run the algorithm, the console will show you the metrics and status. You can also find the results generated in the csv when the experiment end.

## Experiment results

Apart form the `results.xlsx` file, the complete results detailing the best execution found across all experiments for each unique problem instance are available for download in [the following link](https://example.org/TODO). This file is the CSV output that includes the following critical information for analysis:

- Name: The name of the instance.
- Objective value: The number of nodes in the dominance set.
- Execution time (s): The execution time measured in seconds consumed by the algorithm to obtain the solution.
- Execution time: The time consumed formated in "[DAYS]d [HOURS]:[MINUTES]:[SECONDS].[MILLIS]" .
- Chosen nodes: The IDs of the nodes in the graph added to the dominance set separated by white spaces.
