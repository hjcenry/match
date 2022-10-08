package com.biligame.access.match.object;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 匹配对象
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/9/23 18:08
 **/
@Data
public class MatchObject {
    /**
     * 匹配ID
     */
    @JSONField(name = "mid")
    private int matchId;
    /**
     * 匹配分数
     */
    @JSONField(name = "rk")
    private long rank;
    /**
     * 开始匹配时间
     */
    @JSONField(name = "smt")
    private long startMatchTime;
    /**
     * 匹配状态(int类型用于json序列化，枚举类型用于正常使用)
     */
    @JSONField(name = "st")
    private int matchStateIndex;
    @JSONField(serialize = false, deserialize = false)
    private MatchObjectState matchObjectState;

    /**
     * 匹配对象类型(int类型用于json序列化，枚举类型用于正常使用)
     */
    @JSONField(name = "tp")
    private int matchObjectTypeIndex;
    @JSONField(serialize = false, deserialize = false)
    private MatchObjectType matchObjectType;

    public MatchObject() {
    }

    public MatchObject(int matchId, long rank, MatchObjectType matchObjectType) {
        this.matchId = matchId;
        this.rank = rank;
        this.startMatchTime = System.currentTimeMillis();
        this.setMatchObjectState(MatchObjectState.MATCH_WAIT);
    }

    /**
     * JSON序列化调用
     *
     * @param matchStateIndex
     */
    public void setMatchStateIndex(int matchStateIndex) {
        this.matchStateIndex = matchStateIndex;
        this.matchObjectState = MatchObjectState.valueOf(matchStateIndex);
    }

    /**
     * JSON序列化调用
     *
     * @param matchObjectTypeIndex
     */
    public void setMatchObjectTypeIndex(int matchObjectTypeIndex) {
        this.matchObjectTypeIndex = matchObjectTypeIndex;
        this.matchObjectType = MatchObjectType.valueOf(matchObjectTypeIndex);
    }

    public void setMatchObjectState(MatchObjectState matchObjectState) {
        this.matchObjectState = matchObjectState;
        this.matchStateIndex = matchObjectState.getIndex();
    }

    public void setMatchObjectType(MatchObjectType matchObjectType) {
        this.matchObjectType = matchObjectType;
        this.matchObjectTypeIndex = matchObjectType.getIndex();
    }

    /**
     * 重置匹配状态
     *
     * @return 是否成功
     */
    public boolean resetState() {
        return this.enterState(MatchObjectState.MATCH_WAIT);
    }

    /**
     * 进入匹配某个状态
     *
     * @param state 状态
     * @return 是否成功
     */
    public boolean enterState(MatchObjectState state) {
        if (this.matchObjectState.canEnter(state)) {
            this.setMatchObjectState(state);
            return true;
        }
        return false;
    }

    public void update(MatchObject matchObject) {
        this.setMatchObjectState(matchObject.getMatchObjectState());
    }
}
