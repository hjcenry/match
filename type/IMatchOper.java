package com.biligame.access.match.type;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/9/20 17:36
 **/
public interface IMatchOper {

    /**
     * 执行匹配
     * @param redisMatchKey
     */
    void executeMatch(String redisMatchKey);

    void setMatchTypeIndex(int index);
}
