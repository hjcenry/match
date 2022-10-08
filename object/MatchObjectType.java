package com.biligame.access.match.object;

import com.biligame.access.util.enumutil.IndexedEnum;
import com.biligame.util.utils.EnumUtil;

import java.util.List;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/9/28 15:15
 **/
public enum MatchObjectType implements IndexedEnum {
    /**
     * 玩家
     */
    PLAYER(0),
    /**
     * 队伍
     */
    TEAM(1),
    ;

    private int index;

    MatchObjectType(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    private static final List<MatchObjectType> values = IndexedEnumUtil.toIndexes(MatchObjectType.values());

    public static List<MatchObjectType> getValues() {
        return values;
    }

    public static MatchObjectType valueOf(int value) {
        return EnumUtil.valueOf(values, value);
    }
}