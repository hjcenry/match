package com.biligame.access.match.object;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/9/28 15:17
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class MatchPlayerObject extends MatchObject {

    @JSONField(name = "uid")
    private int uid;

    public MatchPlayerObject(int matchId, long rank) {
        super(matchId, rank, MatchObjectType.PLAYER);
    }
}
