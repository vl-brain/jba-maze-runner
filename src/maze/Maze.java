package maze;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class Maze {
    private static final Maze DEMO_INSTANCE = new Maze(new int[][]{
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 1, 0, 0, 0, 0, 1},
            {1, 1, 1, 0, 1, 1, 0, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 1},
            {1, 0, 0, 0, 1, 1, 0, 1, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 0, 1, 0, 1, 1, 1},
            {1, 0, 0, 1, 0, 1, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    });
    private final int[][] matrix;

    public Maze(int[][] matrix) {
        if (matrix.length < 3 || matrix[0].length < 3) {
            throw new IllegalArgumentException("Too small matrix!");
        }
        this.matrix = matrix;
    }

    private static Maze onlyWalls(int height, int width) {
        final int[][] matrix = new int[height][width];
        for (int[] line : matrix) {
            Arrays.fill(line, 1);
        }
        return new Maze(matrix);
    }

    private void addPassage(int fromRow, int fromColumn, int toRow, int toColumn) {
        int minRow = Math.min(fromRow, toRow);
        int maxRow = Math.max(fromRow, toRow);
        int minCol = Math.min(fromColumn, toColumn);
        int maxCol = Math.max(fromColumn, toColumn);
        if (minRow == maxRow || minCol == maxCol) {
            setPassage(minRow, maxRow, minCol, maxCol);
        } else {
            setPassage(minRow, minCol, maxRow, minCol);
            setPassage(maxRow, minCol, maxRow, maxCol);
        }
    }

    private void setPassage(int minRow, int maxRow, int minCol, int maxCol) {
        for (int i = minRow; i <= maxRow; i++) {
            for (int j = minCol; j <= maxCol; j++) {
                matrix[i][j] = 0;
            }
        }
    }

    public static Maze demo() {
        return DEMO_INSTANCE;
    }

    public static Maze random(int height, int width) {
        class Vertex {
            private final int row;
            private final int column;

            Vertex(int row, int column) {
                this.row = row;
                this.column = column;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                Vertex vertex = (Vertex) o;
                return row == vertex.row &&
                        column == vertex.column;
            }

            @Override
            public int hashCode() {
                return Objects.hash(row, column);
            }

            public int getRow() {
                return row;
            }

            public int getColumn() {
                return column;
            }

        }

        class WeightTree {
            private final Vertex root;
            private final Map<Set<Vertex>, Integer> weightEdges;

            WeightTree(Vertex root, Map<Set<Vertex>, Integer> weightEdges) {
                this.root = requireNonNull(root, "Root required!");
                this.weightEdges = weightEdges;
            }

            WeightTree getMst() {
                Set<Vertex> vertexes = new HashSet<>(Set.of(root));
                Map<Set<Vertex>, Integer> edges = new HashMap<>();
                Map<Set<Vertex>, Integer> remainEdges = new HashMap<>(this.weightEdges);
                final WeightTree mst = new WeightTree(root, edges);
                while (remainEdges.entrySet().stream()
                        .filter(entry -> !vertexes.containsAll(entry.getKey()) &&
                                !Collections.disjoint(entry.getKey(), vertexes))
                        .min(Map.Entry.comparingByValue())
                        .map(entry -> {
                            edges.put(entry.getKey(), entry.getValue());
                            vertexes.addAll(entry.getKey());
                            remainEdges.remove(entry.getKey());
                            return true;
                        }).orElse(false)) {
                }
                return mst;
            }

            Set<Set<Vertex>> getEdges() {
                return Set.copyOf(weightEdges.keySet());
            }
        }

        final Random random = new Random();
        Vertex[][] vertexes = new Vertex[(height - 1) / 2][(width - 1) / 2];
        Map<Set<Vertex>, Integer> edges = new HashMap<>();
        Vertex entry = null;
        for (int i = 0, entryIndex = random.nextInt(vertexes.length), exitIndex = random.nextInt(vertexes.length);
             i < vertexes.length; i++) {
            for (int j = 0; j < vertexes[i].length; j++) {
                final int row = 1 + i * 2;
                final int col = 1 + j * 2;
                final Vertex vertex = new Vertex(row, col);
                vertexes[i][j] = vertex;
                if (j == 0 && i == entryIndex) {
                    entry = new Vertex(row, 0);
                    edges.put(Set.of(entry, vertex), random.nextInt(width));
                }
                if (j == vertexes[i].length - 1 && i == exitIndex) {
                    edges.put(Set.of(vertex, new Vertex(row, width - 1)), random.nextInt(width));
                }
                if (j > 0) {
                    edges.put(Set.of(vertexes[i][j - 1], vertex), random.nextInt(width));
                }
                if (i > 0) {
                    edges.put(Set.of(vertexes[i - 1][j], vertex), random.nextInt(width));
                }
            }
        }
        final WeightTree mst = new WeightTree(entry, edges).getMst();
        final Maze maze = Maze.onlyWalls(height, width);
        for (Set<Vertex> edge : mst.getEdges()) {
            final Iterator<Vertex> vertexIt = edge.iterator();
            final Vertex first = vertexIt.next();
            final Vertex second = vertexIt.next();
            maze.addPassage(first.getRow(), first.getColumn(), second.getRow(), second.getColumn());
        }
        return maze;
    }

    @Override
    public String toString() {
        return Arrays.stream(matrix)
                .map(line -> Arrays.stream(line)
                        .mapToObj(i -> i == 0 ? "  " : "\u2588\u2588")
                        .collect(Collectors.joining()))
                .collect(Collectors.joining("\n"));
    }
}
