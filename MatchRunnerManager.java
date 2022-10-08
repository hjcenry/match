package com.biligame.access.match;

import java.util.HashSet;
import java.util.Set;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/9/20 17:02
 **/
public class MatchRunnerManager {

    /**
     * 匹配runner
     */
    private Set<MatchRunner> matchRunners = new HashSet<>();

    public MatchRunnerManager() {
    }

    public Set<MatchRunner> getMatchRunners() {
        return matchRunners;
    }
}
