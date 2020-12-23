package cz.muni.fi.thesis.dataloader;

import java.util.List;
import java.util.Map;

public class MoCapData {

    private Map<Integer, List<Integer>> HMWs;
    private Map<Integer, List<Integer>> mixedHMWs;
    private Map<Integer, List<int[]>> MOMWs;
    private Map<Integer, List<int[]>> mixedMOMWs;
    private Map<Integer, String> OFScenarios;
    private Map<Integer, String> OSScenarios;

    public Map<Integer, List<Integer>> getHMWs() {
        return HMWs;
    }

    public void setHMWs(Map<Integer, List<Integer>> HMWs) {
        this.HMWs = HMWs;
    }

    public Map<Integer, List<Integer>> getMixedHMWs() {
        return mixedHMWs;
    }

    public void setMixedHMWs(Map<Integer, List<Integer>> mixedHMWs) {
        this.mixedHMWs = mixedHMWs;
    }

    public Map<Integer, List<int[]>> getMOMWs() {
        return MOMWs;
    }

    public void setMOMWs(Map<Integer, List<int[]>> MOMWs) {
        this.MOMWs = MOMWs;
    }

    public Map<Integer, List<int[]>> getMixedMOMWs() {
        return mixedMOMWs;
    }

    public void setMixedMOMWs(Map<Integer, List<int[]>> mixedMOMWs) {
        this.mixedMOMWs = mixedMOMWs;
    }

    public Map<Integer, String> getOFScenarios() {
        return OFScenarios;
    }

    public void setOFScenarios(Map<Integer, String> OFScenarios) {
        this.OFScenarios = OFScenarios;
    }

    public Map<Integer, String> getOSScenarios() {
        return OSScenarios;
    }

    public void setOSScenarios(Map<Integer, String> OSScenarios) {
        this.OSScenarios = OSScenarios;
    }
}
