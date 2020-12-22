package cz.muni.fi.thesis;

import java.util.List;
import java.util.Map;

public class MoCapData {

    private Map<Integer, List<Integer>> hmwDataset;
    private Map<Integer, List<Integer>> hmwMixedDataset;
    private Map<Integer, List<int[]>> momwDataset;
    private Map<Integer, List<int[]>> momwMixedDataset;
    private Map<Integer, String> orderFreeScenarios;
    private Map<Integer, String> orderSensitiveScenarios;

    public Map<Integer, List<Integer>> getHmwDataset() {
        return hmwDataset;
    }

    public void setHmwDataset(Map<Integer, List<Integer>> hmwDataset) {
        this.hmwDataset = hmwDataset;
    }

    public Map<Integer, List<Integer>> getHmwMixedDataset() {
        return hmwMixedDataset;
    }

    public void setHmwMixedDataset(Map<Integer, List<Integer>> hmwMixedDataset) {
        this.hmwMixedDataset = hmwMixedDataset;
    }

    public Map<Integer, List<int[]>> getMomwDataset() {
        return momwDataset;
    }

    public void setMomwDataset(Map<Integer, List<int[]>> momwDataset) {
        this.momwDataset = momwDataset;
    }

    public Map<Integer, List<int[]>> getMomwMixedDataset() {
        return momwMixedDataset;
    }

    public void setMomwMixedDataset(Map<Integer, List<int[]>> momwMixedDataset) {
        this.momwMixedDataset = momwMixedDataset;
    }

    public Map<Integer, String> getOrderFreeScenarios() {
        return orderFreeScenarios;
    }

    public void setOrderFreeScenarios(Map<Integer, String> orderFreeScenarios) {
        this.orderFreeScenarios = orderFreeScenarios;
    }

    public Map<Integer, String> getOrderSensitiveScenarios() {
        return orderSensitiveScenarios;
    }

    public void setOrderSensitiveScenarios(Map<Integer, String> orderSensitiveScenarios) {
        this.orderSensitiveScenarios = orderSensitiveScenarios;
    }
}
