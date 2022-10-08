package com.biligame.access.match;

import com.biligame.access.base.constants.RedisKeyEnum;
import com.biligame.access.log.Log;
import com.biligame.access.match.type.MatchType;
import com.biligame.access.redis.VersionRedisTemplate;
import com.biligame.access.util.DateUtil;
import lombok.EqualsAndHashCode;

/**
 * 匹配逻辑基本单元
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/9/28 16:55
 **/
@EqualsAndHashCode
public class MatchTask implements Runnable {

    /**
     * Runner的匹配玩法
     */
    private final MatchType matchType;
    /**
     * 关卡id
     */
    private final int levelId;
    /**
     * redis锁
     */
    private final String redisLockKey;
    /**
     * redis匹配key
     */
    private final String redisMatchKey;
    /**
     * 执行时间
     */
    private long executeTime;

    public MatchTask(MatchType matchType, int levelId) {
        this.matchType = matchType;
        this.levelId = levelId;
        this.redisLockKey = RedisKeyEnum.MATCH_LOCK + String.valueOf(matchType.getIndex()) + ":" + levelId;
        this.redisMatchKey = String.valueOf(matchType.getIndex()) + ":" + levelId;
    }

    @Override
    public void run() {
        long start = DateUtil.getNow();

        if (!VersionRedisTemplate.tryLock(RedisKeyEnum.MATCH_LOCK, this.redisLockKey)) {
            // 没抢到匹配锁，本地调度不执行
            return;
        }
        try {
            // 执行匹配
            matchType.getOper().executeMatch(this.redisMatchKey);
        } catch (Exception e) {
            Log.matchLogger.error(String.format("match[%s].runner.err", this.matchType.name()), e);
        } finally {
            // 释放匹配锁
            VersionRedisTemplate.releaseLock(RedisKeyEnum.MATCH_LOCK, this.redisLockKey);
        }

        this.executeTime = DateUtil.getNow() - start;
    }

    public long getExecuteTime() {
        return executeTime;
    }

    @Override
    public String toString() {
        return "MatchTask{" +
                "matchType=" + matchType +
                ", levelId=" + levelId +
                ", redisLockKey='" + redisLockKey + '\'' +
                '}';
    }
}
