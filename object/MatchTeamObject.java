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
public class MatchTeamObject extends MatchObject {

    @JSONField(name = "tid")
    private int teamId;

    public MatchTeamObject(int matchId, long rank) {
        super(matchId, rank, MatchObjectType.TEAM);
    }
}
