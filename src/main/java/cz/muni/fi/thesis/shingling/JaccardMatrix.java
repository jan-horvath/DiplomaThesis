package cz.muni.fi.thesis.shingling;

import java.util.*;

public class JaccardMatrix {
    private Map<Integer, List<JaccardEntry>> matrix = new HashMap<>();

    public static JaccardMatrix createMatrixFromSets(Map<Integer, boolean[]> data) {
        JaccardMatrix jaccardMatrix = new JaccardMatrix();

        for (Map.Entry<Integer, boolean[]> entry1 : data.entrySet()) {
            jaccardMatrix.matrix.put(entry1.getKey(), new ArrayList<>());
            List<JaccardEntry> jaccardEntries = jaccardMatrix.matrix.get(entry1.getKey());

            for (Map.Entry<Integer, boolean[]> entry2 : data.entrySet()) {
                double jaccardValue = Jaccard.computeJaccard(entry1.getValue(), entry2.getValue());
                jaccardEntries.add(new JaccardEntry(entry2.getKey(), jaccardValue));
            }
        }
        return jaccardMatrix;
    }

    public static JaccardMatrix createMatrixFromMinhashes(Map<Integer, int[]> data) {
        JaccardMatrix jaccardMatrix = new JaccardMatrix();

        for (Map.Entry<Integer, int[]> entry1 : data.entrySet()) {
            jaccardMatrix.matrix.put(entry1.getKey(), new ArrayList<>());
            List<JaccardEntry> jaccardEntries = jaccardMatrix.matrix.get(entry1.getKey());

            for (Map.Entry<Integer, int[]> entry2 : data.entrySet()) {
                double jaccardValue = Jaccard.computeJaccardOnMinhashes(entry1.getValue(), entry2.getValue());
                jaccardEntries.add(new JaccardEntry(entry2.getKey(), jaccardValue));
            }
        }
        return jaccardMatrix;
    }

    public static JaccardMatrix createMatrixFromMultisets(Map<Integer, int[]> data) {
        JaccardMatrix jaccardMatrix = new JaccardMatrix();

        for (Map.Entry<Integer, int[]> entry1 : data.entrySet()) {
            jaccardMatrix.matrix.put(entry1.getKey(), new ArrayList<>());
            List<JaccardEntry> jaccardEntries = jaccardMatrix.matrix.get(entry1.getKey());

            for (Map.Entry<Integer, int[]> entry2 : data.entrySet()) {
                double jaccardValue = Jaccard.computeJaccardOnMultisets(entry1.getValue(), entry2.getValue());
                jaccardEntries.add(new JaccardEntry(entry2.getKey(), jaccardValue));
            }
        }
        return jaccardMatrix;
    }

    public Map<Integer, List<JaccardEntry>> getMatrix() {
        return matrix;
    }

    public static class JaccardEntry implements Comparable<JaccardEntry> {
        public final int recordID;
        public final double jaccardValue;

        public JaccardEntry(int recordID, double jaccardValue) {
            this.recordID = recordID;
            this.jaccardValue = jaccardValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JaccardEntry entry = (JaccardEntry) o;
            return Double.compare(entry.jaccardValue, jaccardValue) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(jaccardValue);
        }

        @Override
        public int compareTo(JaccardEntry o) {
            return Double.compare(jaccardValue, ((JaccardEntry) o).jaccardValue);
        }

        @Override
        public String toString() {
            return "(" + recordID + ", " + jaccardValue + ")";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, List<JaccardEntry>> entry : matrix.entrySet()) {
            sb.append(entry.getKey()).append(": ");
            for (JaccardEntry jaccardEntry : entry.getValue()) {
                sb.append(jaccardEntry.toString()).append(", ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
