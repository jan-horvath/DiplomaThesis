package cz.muni.fi.thesis.shingling.similarity;

public class CosineSimilarity {

    public static double computeSimilarity(double[] vec1, double[] vec2) {
        assert (vec1.length == vec2.length);

        double vec1Magnitude = 0.0;
        double vec2Magnitude = 0.0;
        double dotProduct = 0.0;

        for (int i = 0; i < vec1.length; ++i) {
            dotProduct += vec1[i] * vec2[i];
            vec1Magnitude += vec1[i] * vec1[i];
            vec2Magnitude += vec2[i] * vec2[i];
        }
        return dotProduct / Math.sqrt(vec1Magnitude * vec2Magnitude);
    }
}
