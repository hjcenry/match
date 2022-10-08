package com.biligame.access.match.type;

import com.biligame.access.constants.MatchConstants;
import com.biligame.access.log.Log;
import com.biligame.access.match.object.MatchObject;
import com.biligame.access.match.object.MatchObjectState;
import com.biligame.access.redis.MatchRedisTemplate;
import com.biligame.access.util.CollectionUtil;
import com.biligame.access.util.TimeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/9/20 17:36
 **/
public abstract class AbstractMatchOper implements IMatchOper {

    protected int matchTypeIndex;

    @Override
    public void setMatchTypeIndex(int index) {
        this.matchTypeIndex = index;
    }

    @Override
    public void executeMatch(String redisMatchKey) {
        // 取出匹配队列的所有玩家
        List<MatchObject> matchObjectList = MatchRedisTemplate.getAllMatchObjects(redisMatchKey);
        if (CollectionUtil.isEmpty(matchObjectList)) {
            // 没有匹配对象
            return;
        }
        // 开始匹配
        matchProcess(matchObjectList, redisMatchKey);
    }

    private static long MATCH_TIME_OUT = TimeUtils.MIN;

    private static long MATCH_EXPEND_RANK = 5;
    private static long MATCH_EXPEND_MAX_RANK = 100;

    /**
     * 把玩家从匹配池移除
     *
     * @param matchId 匹配id
     */
    protected void removeMatchObjectFromPool(int matchId) {
    }

    /**
     * 匹配排序器
     * 按开始匹配时间倒叙排序，从等待时间最长的玩家开始匹配
     */
    final Comparator<MatchObject> MATCH_SORT_COMPARATOR = (o1, o2) -> (int) (o1.getStartMatchTime() - o2.getStartMatchTime());

    /**
     * 按分数的匹配池子
     */
    TreeMap<Long, Set<MatchObject>> tmpRankMatchPoolMap = new TreeMap<>();
    /**
     * 记录需要移除的匹配对象
     */
    Set<String> tmpMarkRemoveMatchObjects = new HashSet<>();

    protected void matchProcess(Collection<MatchObject> matchPool, String redisMatchKey) {
        long now = System.currentTimeMillis();
        //====================================================================================================
        // 1.清空临时匹配记录
        //====================================================================================================
        tmpRankMatchPoolMap.clear();
        tmpMarkRemoveMatchObjects.clear();
        //====================================================================================================
        // 2.匹配预处理
        //====================================================================================================
        matchPreProcess(matchPool, now);

        for (Set<MatchObject> sameRankObjects : tmpRankMatchPoolMap.values()) {
            if (CollectionUtil.isEmpty(sameRankObjects)) {
                continue;
            }
            // 最大遍历次数
            int maxIterLen = sameRankObjects.size();
            for (int i = 0; i < maxIterLen; i++) {
                //找出同一分数段里，等待时间最长的玩家，用他来匹配，因为他的区间最大
                //如果他都不能匹配到，等待时间比他短的玩家更匹配不到
                //set已经排过序了，取第一个匹配等待时间最长的
                MatchObject matchObject = sameRankObjects.iterator().next();
                if (Log.matchLogger.isDebugEnabled()) {
                    Log.matchLogger.debug(String.format("为该分数上等待最久时间的匹配对象[id:%d|rank:%d]开始匹配,已等待[%d]", matchObject.getMatchId(), matchObject.getRank(), (now - matchObject.getStartMatchTime())));
                }

                //====================================================================================================
                // 3.按范围进行匹配，并返回匹配结果
                //====================================================================================================
                List<MatchObject> matchObjects = doMatch(matchObject, now);

                //====================================================================================================
                // 4.匹配结束处理
                //====================================================================================================
                if (matchObjects.size() < this.needMatchObjectCount(matchObject)) {
                    //没匹配够人数
                    //本分数段等待时间最长的玩家都匹配不到，其他更不用尝试了
                    if (Log.matchLogger.isDebugEnabled()) {
                        Log.matchLogger.debug(matchObject.getMatchId() + "|匹配到玩家数量不够，取消本次匹配");
                    }
                    //重置匹配到的玩家
                    for (MatchObject resetMatchObject : matchObjects) {
                        resetMatchObject.resetState();
                    }
                    break;
                }

                if (Log.matchLogger.isDebugEnabled()) {
                    Log.matchLogger.debug(matchObject.getMatchId() + "|匹配到玩家数量够了|提交匹配成功处理");
                }
                //====================================================================================================
                // 5.匹配成功处理
                //====================================================================================================
                //自己也匹配池移除
                sameRankObjects.remove(matchObject);
                //匹配成功处理
                matchObjects.add(matchObject);
                //进入待删除列表
                tmpMarkRemoveMatchObjects.add(String.valueOf(matchObject.getMatchId()));
                //把配对的人提交匹配成功处理
                matchSuccessProcess(matchObjects);
            }
        }
        //====================================================================================================
        // 6.匹配后处理
        //====================================================================================================
        matchAfterProcess(redisMatchKey);

        long endTime = System.currentTimeMillis();
        if (Log.matchLogger.isDebugEnabled()) {
            Log.matchLogger.debug("执行匹配结束|结束时间|" + endTime + "|耗时|" + (endTime - now) + "ms");
        }
    }

    /**
     * 匹配预处理
     *
     * @param matchPool 匹配池
     * @param now       当前时间
     */
    protected void matchPreProcess(Collection<MatchObject> matchPool, long now) {
        for (MatchObject matchObject : matchPool) {
            // 1.在匹配池中时间超时移除
            if ((now - matchObject.getStartMatchTime()) > MATCH_TIME_OUT) {
                if (Log.matchLogger.isWarnEnabled()) {
                    Log.matchLogger.warn(String.format("uid[%d].match.timeout[%d]", matchObject.getMatchId(), MATCH_TIME_OUT));
                }
                removeMatchObjectFromPool(matchObject.getMatchId());
                continue;
            }
            MatchObjectState matchObjectState = matchObject.getMatchObjectState();
            // 2.玩家状态需要移除
            if (matchObjectState.isNeedRemove()) {
                removeMatchObjectFromPool(matchObject.getMatchId());
                continue;
            }
            // 3.玩家状态不能匹配
            if (!matchObjectState.canEnter(MatchObjectState.MATCH_FINISH)) {
                continue;
            }
            // 4.把匹配池中的对象按分数分布,正式进入匹配池
            Set<MatchObject> set = tmpRankMatchPoolMap.computeIfAbsent(matchObject.getRank(), k -> new TreeSet<>(MATCH_SORT_COMPARATOR));
            set.add(matchObject);
        }
    }

    /**
     * 按范围匹配处理
     * 真正的匹配逻辑
     *
     * @param matchObject 待匹配对象
     * @param now         当前时间
     * @return 匹配结果
     */
    protected List<MatchObject> doMatch(MatchObject matchObject, long now) {
        List<MatchObject> matchObjects = new ArrayList<>();

        long waitTime = now - matchObject.getStartMatchTime();
        //按等待时间扩大匹配范围
        long expendRange = waitTime / MatchConstants.MATCH_INTERVAL * MATCH_EXPEND_RANK;
        long min = Math.max((matchObject.getRank() - expendRange), 0);
        long max = matchObject.getRank() + expendRange;

        if (Log.matchLogger.isDebugEnabled()) {
            Log.matchLogger.debug(String.format("匹配对象[%d]本次搜索范围[%d/%d]", matchObject.getMatchId(), min, max));
        }

        Set<MatchObject> tmpRankMatchObjects = new HashSet<>();
        //从中位数向两边扩大范围搜索
        for (long searchRankUp = matchObject.getRank(), searchRankDown = matchObject.getRank(); searchRankUp <= max || searchRankDown >= min; searchRankUp++, searchRankDown--) {
            tmpRankMatchObjects.addAll(tmpRankMatchPoolMap.getOrDefault(searchRankUp, Collections.emptySet()));
            if (searchRankDown != searchRankUp && searchRankDown > 0) {
                tmpRankMatchObjects.addAll(tmpRankMatchPoolMap.getOrDefault(searchRankDown, Collections.emptySet()));
            }
            if (tmpRankMatchObjects.isEmpty()) {
                // 当前分数匹配队列没人
                continue;
            }
            if (matchObjects.size() >= this.needMatchObjectCount(matchObject)) {
                // 匹配人数够了
                break;
            }
            for (MatchObject rankMatchObject : tmpRankMatchObjects) {
                if (rankMatchObject.getMatchId() == matchObject.getMatchId()) {
                    //排除玩家本身
                    continue;
                }
                MatchObjectState matchObjectState = rankMatchObject.getMatchObjectState();
                if (!matchObjectState.canEnter(MatchObjectState.MATCH_FINISH)) {
                    continue;
                }
                if (matchObjects.size() >= this.needMatchObjectCount(matchObject)) {
                    // 匹配人数满
                    break;
                }
                // 最后一步检测，检测成功标记状态
                if (!this.checkAndUpdateMatchSuccessState(rankMatchObject)) {
                    continue;
                }
                // 匹配成功!!!
                matchObjects.add(rankMatchObject);
                if (Log.matchLogger.isDebugEnabled()) {
                    Log.matchLogger.debug(String.format("玩家[%d][%d]匹配到玩家[%d][%d]", matchObject.getMatchId(), matchObject.getRank(), rankMatchObject.getMatchId(), rankMatchObject.getRank()));
                }
            }
            tmpRankMatchObjects.clear();
        }
        return matchObjects;
    }

    /**
     * 检测并更新匹配对象
     *
     * @param matchObject 匹配对象
     * @return 是否可成功匹配
     */
    protected boolean checkAndUpdateMatchSuccessState(MatchObject matchObject) {
        // 更新匹配对象状态
        return MatchRedisTemplate.tryEnterMatchObjectState(matchObject, MatchObjectState.MATCH_FINISH);
    }

    /**
     * 匹配成功
     *
     * @param matchObjects 匹配成功对象list
     */
    protected abstract void matchSuccessProcess(List<MatchObject> matchObjects);

    /**
     * 匹配后处理
     */
    protected void matchAfterProcess(String redisMatchKey) {
        if (!CollectionUtil.isEmpty(tmpMarkRemoveMatchObjects)) {
            MatchRedisTemplate.removeMatchObjects(redisMatchKey, tmpMarkRemoveMatchObjects);
        }
    }

    /**
     * 需要匹配的对象数量
     *
     * @param matchObject 匹配对象
     * @return 需要匹配的对象数量
     */
    protected int needMatchObjectCount(MatchObject matchObject) {
        return 1;
    }

}
