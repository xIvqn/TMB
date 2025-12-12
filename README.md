![Citation Badge](https://api.juleskreuer.eu/citation-badge.php?doi=YOURDOI)
![GitHub last commit](https://img.shields.io/github/last-commit/xIvqn/TMB) 
![GitHub Repo stars](https://img.shields.io/github/stars/xIvqn/TMB)

# A Scalable GRASP Algorithm for the Targeted Misinformation Blocking Problem

The research on Social Network Analysis has exponentially grown in the last decades due to the relevance of social networks in the society. Although most of the works have been focused on the maximization of influence, the spread of misinformation and its impact in relevant aspects of the society such as politics or economy, among others, have attracted the attention of both practitioners and the scientific community. This work is focused on the Targeted Misinformation Blocking Problem (TMB), whose aim is to minimize the number of nodes required to reduce the spread of misinformation through a social network. It is considered that if the information is spread under a certain threshold, then it would not have effect on the network. Therefore, the objective is to find a subset of blocking nodes that guarantee that the spread of information is below that threshold. To that end, a Scalable GRASP algorithm is proposed, being able to deal with medium and large scale networks in reasonable computing time. The results obtained are compared with the best method found in the literature, Scalable TMB, which generates a set of trees to simulate the influence spread and identify the most promising nodes. Experimental results show that the proposed algorithm is able to outperform the state of the art when considering two of the most extended diffusion models. Additionally, the scalability of the proposal is proven, been able to provide high-quality solutions even in those instances in which previous algorithm are not able to generate a feasible one. Those results, supported by non-parametric statistical tests, indicates that the proposed algorithm is a competitive method for solving the TMB.

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
* `_MOST`: $S$ created using the [MPIF project, by Lozano-Osorio et al.](https://doi.org/10.1111/itor.13468) to get the 100 most influential nodes in each graph. Those are the ones used in the article.

Also, to compute the solution using the pagerank greedy criteria, `.pagerank` files were created to store the nodes values.

## Repository contents

- `code`: Source code of the Java project.
- `instances`: The instances used in this project, with the MOST influential nodes and RANDOM ones as sources, and their corresponding `.pagerank` files.
- `tmb-1.0.jar`: It is the final executable of the application; further information is provided below.
- `results.xlsx`: A file containing the best and average values of the proprosal and the comparision with the DEGREE and STMB results.
- `selected.csv`: A comma-separated-value file containing the full results of the proposal, including the configuration parameters and the chosen nodes.

## Executable

You can just run the `tmb-1.0.jar` as follows.

```bash
java -jar tmb-1.0.jar -e -P -s GRASP -i "./instances" -o "./outputs/results.csv" -m LT -mc 1
java -jar tmb-1.0.jar -e -P -s GRASP -i "./instances" -o "./outputs/results.csv" -m IC -p 0.1 -mc 100
```

If you want to customize the execution, you can use the following command to know how to do it.

```bash
java -jar tmb-1.0.jar --help
```

Once you run the algorithm, the console will show you the metrics and status. You can also find the results generated in the csv when the experiment end.

## Experiment results

Apart form the `results.xlsx` file, the complete results detailing the best execution found across all experiments for each unique problem instance are available for download in `selected.csv`. This file is the CSV output that includes the following critical information for analysis:

- Name: The name of the instance.
- Model: The specific diffusion model between Iterated Cascade (IC) and Linear Threshold (LT) run in the experiment.
- Gamma: The Gamma value applied for the influence reduction.
- Objective value: The number of nodes in the dominance set.
- Execution time (s): The execution time measured in seconds consumed by the algorithm to obtain the solution.
- Execution time: The time consumed formated in "[DAYS]d [HOURS]:[MINUTES]:[SECONDS].[MILLIS]" .
- Chosen nodes: The IDs of the nodes in the graph added to the dominance set separated by white spaces.
