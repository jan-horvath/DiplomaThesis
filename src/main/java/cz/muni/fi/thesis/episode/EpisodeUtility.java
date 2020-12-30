package cz.muni.fi.thesis.episode;

import cz.muni.fi.thesis.similarity.MomwSimilarity;

import java.util.*;

public class EpisodeUtility {

    private static Double computeWeightForMotionWord(int[] motionWord, Map<Integer, List<int[]>> overlayData) {
        int count = 0;
        for (Map.Entry<Integer, List<int[]>> episodeEntry : overlayData.entrySet()) {
            for (int[] otherMW : episodeEntry.getValue()) {
                if (MomwSimilarity.overlayMotionWordsMatch(motionWord, otherMW, 1)) {
                    ++count;
                    break;
                }
            }
        }

        return Math.log(((double) overlayData.size()) / count);
    }

    /**
     * Computes IDF weights for MOMWs
     */
    private static List<Double> computeWeights(List<int[]> episode, Map<Integer, List<int[]>> overlayData) {
        List<Double> weights = new ArrayList<>();

        for (int[] motionWord : episode) {
            double weight = computeWeightForMotionWord(motionWord, overlayData);
            weights.add(weight);
        }
        return weights;
    }

    /**
     * Converts map of MOMWs into a list of MomwEpisodes with scenarios and computes the IDF weights of individual MOMWs
     */
    public static List<MomwEpisode> createMomwEpisodes(Map<Integer, List<int[]>> motionWords,
                                                       Map<Integer, String> scenarios) {
       List<MomwEpisode> episodes = new ArrayList<>();
        for (Integer epId : motionWords.keySet()) {
            List<Double> weights = computeWeights(motionWords.get(epId), motionWords);
            episodes.add(new MomwEpisode(epId, scenarios.get(epId), motionWords.get(epId), weights));
        }
        return episodes;
    }

    /**
     * Converts map of MOMWs into a map of MomwEpisodes with scenarios and computes the IDF weights of individual MOMWs
     */
    public static Map<Integer, MomwEpisode> createMomwEpisodesAsMap(Map<Integer, List<int[]>> motionWords,
                                                                    Map<Integer, String> scenarios) {
        List<MomwEpisode> momwEpisodes = createMomwEpisodes(motionWords, scenarios);
        Map<Integer, MomwEpisode> map = new HashMap<>();

        for (MomwEpisode episode : momwEpisodes) {
            map.put(episode.getId(), episode);
        }
        return map;
    }

    /**
     * Converts map of HMWs into a list of HmwEpisodes with scenarios
     * Assumes that the static setUp function on the class HmwEpisode has been properly called
     */
    public static List<HmwEpisode> createHmwEpisodes(Map<Integer, List<Integer>> motionWords,
                                                     Map<Integer, String> scenarios) {
        List<HmwEpisode> episodes = new ArrayList<>();

        for (Integer epId : motionWords.keySet()) {
            String scenario = scenarios.get(epId);
            episodes.add(new HmwEpisode(epId, scenario, motionWords.get(epId)));
        }

        return episodes;
    }
}
