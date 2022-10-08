package com.biligame.access.match;

import com.biligame.access.constants.MatchConstants;
import com.biligame.access.log.Log;
import com.biligame.access.util.DateUtil;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 匹配运行器。 线程执行的基本单元。一个运行器内的消息是串行处理的。
 *
 * @author hejincheng
 * @version 1.0
 * @date
 **/
@EqualsAndHashCode
public class MatchRunner implements Callable<Void> {

    /**
     * 执行结果
     */
    public Future<Void> future;
    /**
     * 匹配任务
     */
    private final Set<MatchTask> matchTasks = new HashSet<>();

    public MatchRunner() {
    }

    @Override
    public Void call() {
        long start = DateUtil.getNow();

        for (MatchTask matchTask : matchTasks) {
            matchTask.run();
        }

        long end = DateUtil.getNow();
        long executeTime = end - start;

        if (Log.battleLogger.isWarnEnabled() && executeTime >= MatchConstants.MATCH_INTERVAL) {
            Log.battleLogger.warn(String.format("----------------------Match too long (%dms)-------------------------", executeTime));
            for (MatchTask matchTask : matchTasks) {
                Log.battleSlowLogger.warn(String.format("MatchTask execute time is : (%dms), the info is : {%s}", matchTask.getExecuteTime(), matchTask));
            }
            Log.battleLogger.warn(String.format("----------------------Match too long (%dms)-------------------------", executeTime));
        }
        return null;
    }
}
