package com.biligame.access.match.object;

import com.biligame.access.util.enumutil.IndexedEnum;
import com.biligame.util.utils.EnumUtil;
import lombok.Getter;

import java.util.List;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/9/27 17:15
 **/
public enum MatchObjectState implements IndexedEnum {
    /**
     * 等待匹配
     */
    MATCH_WAIT(0),
    /**
     * 已匹配成功
     */
    MATCH_FINISH(1, true),
    /**
     * 已取消匹配
     */
    MATCH_CANCEL(2, true),
    ;

    private int index;
    @Getter
    private boolean needRemove;

    MatchObjectState(int index) {
        this(index, false);
    }

    MatchObjectState(int index, boolean needRemove) {
        this.index = index;
        this.needRemove = needRemove;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    private static final List<MatchObjectState> values = IndexedEnumUtil.toIndexes(MatchObjectState.values());

    public static List<MatchObjectState> getValues() {
        return values;
    }

    public static MatchObjectState valueOf(int value) {
        return EnumUtil.valueOf(values, value);
    }

    private MatchObjectState[] preStates;

    static {
        // 设置每个状态合法的前置状态，如果不设置，则无法进入此状态
        MATCH_WAIT.setPreState();
        MATCH_FINISH.setPreState(MATCH_WAIT);
        MATCH_CANCEL.setPreState(MATCH_WAIT);
    }

    public void setPreState(MatchObjectState... preStates) {
        this.preStates = preStates;
    }

    public boolean canEnter(MatchObjectState targetState) {
        if (targetState.preStates == null) {
            return false;
        }
        for (int i = 0; i < targetState.preStates.length; i++) {
            if (targetState.preStates[i] == this) {
                return true;
            }
        }
        return false;
    }

}