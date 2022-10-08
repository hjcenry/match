package com.biligame.access.match.type;

import com.biligame.access.util.enumutil.IndexedEnum;
import com.biligame.util.utils.EnumUtil;
import lombok.Getter;

import java.util.List;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/9/20 17:02
 **/
public enum MatchType implements IndexedEnum {
    // NONE
    NONE(0, null),
    // 资源关卡
    RESOURCE_LEVEL(1, new ResourceLevelMatchOper()),
    ;

    private final int index;
    @Getter
    private final IMatchOper oper;

    MatchType(int index, IMatchOper oper) {
        this.index = index;
        this.oper = oper;
        if (this.oper != null) {
            this.oper.setMatchTypeIndex(this.index);
        }
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    private static final List<MatchType> values = IndexedEnumUtil.toIndexes(MatchType.values());

    public static List<MatchType> getValues() {
        return values;
    }

    public static MatchType valueOf(int value) {
        return EnumUtil.valueOf(values, value);
    }
}