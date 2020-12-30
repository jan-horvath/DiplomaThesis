package cz.muni.fi.thesis.dataloader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoCapData {

    private Map<Integer, List<Integer>> HMWs;
    private Map<Integer, List<Integer>> mixedHMWs;
    private Map<Integer, List<int[]>> MOMWs;
    private Map<Integer, List<int[]>> mixedMOMWs;
    private Map<Integer, String> OFScenarios;
    private Map<Integer, String> OSScenarios;
    private Map<Integer, Integer> OFVariableK;
    private Map<Integer, Integer> OSVariableK;

    public Map<Integer, Integer> getOSVariableKForFiltering(double filteringFactor) {
        return getVariableK(OSVariableK, filteringFactor);
    }

    public Map<Integer, Integer> getOFVariableKForFiltering(double filteringFactor) {
        return getVariableK(OFVariableK, filteringFactor);
    }

    private Map<Integer, Integer> getVariableK(Map<Integer, Integer> variableK, double filteringFactor) {
        Map<Integer, Integer> varKForFiltering = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : variableK.entrySet()) {
            varKForFiltering.put(entry.getKey(), (int) (entry.getValue() * filteringFactor));
        }
        return varKForFiltering;
    }

    // GET & SET METHODS
    public Map<Integer, List<Integer>> getHMWs() {
        return HMWs;
    }

    void setHMWs(Map<Integer, List<Integer>> HMWs) {
        this.HMWs = HMWs;
    }

    public Map<Integer, List<Integer>> getMixedHMWs() {
        return mixedHMWs;
    }

    void setMixedHMWs(Map<Integer, List<Integer>> mixedHMWs) {
        this.mixedHMWs = mixedHMWs;
    }

    public Map<Integer, List<int[]>> getMOMWs() {
        return MOMWs;
    }

    void setMOMWs(Map<Integer, List<int[]>> MOMWs) {
        this.MOMWs = MOMWs;
    }

    public Map<Integer, List<int[]>> getMixedMOMWs() {
        return mixedMOMWs;
    }

    void setMixedMOMWs(Map<Integer, List<int[]>> mixedMOMWs) {
        this.mixedMOMWs = mixedMOMWs;
    }

    public Map<Integer, String> getOFScenarios() {
        return OFScenarios;
    }

    void setOFScenarios(Map<Integer, String> OFScenarios) {
        this.OFScenarios = OFScenarios;
    }

    public Map<Integer, String> getOSScenarios() {
        return OSScenarios;
    }

    void setOSScenarios(Map<Integer, String> OSScenarios) {
        this.OSScenarios = OSScenarios;
    }

    public Map<Integer, Integer> getOFVariableK() {
        return OFVariableK;
    }

    void setOFVariableK(Map<Integer, Integer> OFVariableK) {
        this.OFVariableK = OFVariableK;
    }

    public Map<Integer, Integer> getOSVariableK() {
        return OSVariableK;
    }

    void setOSVariableK(Map<Integer, Integer> OSVariableK) {
        this.OSVariableK = OSVariableK;
    }
}
