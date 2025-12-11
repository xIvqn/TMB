package io.github.xivqn.importers;

import io.github.xivqn.exceptions.ImportException;
import io.github.xivqn.entities.Graph;
import io.github.xivqn.entities.Instance;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Represents an importer for instances.
 */
public class TMBEdgesImporter {

    private static final String SUPPORTED_EXT = ".tmbedges";

    private final BufferedReader reader;
    private final String name;

    public static String getInstanceNameFromPath(Path path) {
        return path.getFileName().toString().substring(0, path.getFileName().toString().lastIndexOf('.'));
    }

    public static String getSupportedExt() {
        return SUPPORTED_EXT;
    }

    public TMBEdgesImporter(BufferedReader reader, String name) {
        this.reader = reader;
        this.name = name;
    }
    public TMBEdgesImporter(Path path) throws FileNotFoundException {
        this(
                new BufferedReader(new FileReader(path.toFile())),
                getInstanceNameFromPath(path)
        );
    }

    /**
     * Import an instance from the input file.
     *
     * @return the imported instance
     * @throws IOException if an error occurs while reading the file
     * @throws ImportException if the file format is invalid
     */
    public Instance importInstance() throws IOException, ImportException {
        GraphData graphData = initializeGraph(reader);
        Graph graph = graphData.graph;
        int numEdges = graphData.numEdges;

        for (int i = 0; i < numEdges; i++) {
            String line = reader.readLine();

            if (line == null || line.isEmpty()) throw new ImportException("Invalid instance file: not enough edges found.");

            String[] data = line.split(" ");

            try {
                int u = Integer.parseInt(data[0]);
                int v = Integer.parseInt(data[1]);

                graph.addDirectedEdge(u, v);
            } catch (NumberFormatException e) {
                throw new ImportException("Invalid instance file: edge data is not a number.");
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new ImportException("Invalid instance file: edge data is missing.");
            }
        }

        graph.sortAdjacents();
        graph.mapSources();

        return new Instance(graph, this.name);
    }

    public class GraphData {
        public final Graph graph;
        public int numNodes;
        public int numEdges;

        public GraphData(Graph graph, int numNodes, int numEdges) {
            this.graph = graph;
            this.numNodes = numNodes;
            this.numEdges = numEdges;
        }
    }

    /**
     * Get the graph data from the input file.
     *
     * @param reader the reader to read the file
     * @return the graph data
     * @throws IOException if an error occurs while reading the file
     * @throws ImportException if the file format is invalid
     */
    private GraphData initializeGraph(BufferedReader reader) throws IOException, ImportException {
        String line = reader.readLine();
        String[] data;

        if  (line == null) {
            throw new ImportException("Invalid instance file: no graph data found.");
        }

        //  For normalized instances, first line describes the graph with "N M",
        //  where N is the number of nodes and M is the number of edges.
        data = line.split(" ");
        int N = Integer.parseInt(data[0]);
        int M = Integer.parseInt(data[1]);
        int S = Integer.parseInt(data[2]);

        //  Read sources of misinformation
        line = reader.readLine();
        if (line == null) throw new ImportException("Invalid instance file: no sources found.");
        data = line.split(" ");
        if (data.length != S) throw new ImportException("Invalid instance file: invalid sources size.");
        int[] sources = Arrays.stream(data)
                .mapToInt(Integer::parseInt)
                .toArray();

        Graph graph = new Graph(N, M, sources);

        //  Read probabilities of activation
        line = reader.readLine();
        if (line == null) throw new ImportException("Invalid instance file: no probabilities found.");
        data = line.split(" ");
        double[] probabilities = Arrays.stream(data)
                .map(Float::parseFloat)
                .mapToDouble(f -> f)
                .toArray();

        graph.setProbabilities(probabilities);

        return new GraphData(graph, N, M);
    }

}
