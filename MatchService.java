package com.biligame.access.match;

import com.biligame.access.core.base.impl.IService;
import com.biligame.access.network.core.bean.Packet;
import com.biligame.access.thread.MatchThreadProcessGroup;
import com.biligame.access.thread.constant.ThreadConstants;
import com.biligame.access.thread.message.IMessageProcessor;
import lombok.Getter;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/9/20 11:48
 **/
public class MatchService implements IService {

    private static final MatchService INSTANCE = new MatchService();

    private MatchService() {
    }

    public static MatchService getInstance() {
        return INSTANCE;
    }

    @Getter
    private MatchRunnerManager matchRunnerManager;

    private IMessageProcessor<Packet> matchMsgProcessor;

    @Override
    public void onReady() throws Exception {
        matchRunnerManager = new MatchRunnerManager();
        if (ThreadConstants.MATCH_THREAD_NUM <= 0) {
            throw new Exception(String.format("match.thread.num[%d].err", ThreadConstants.MATCH_THREAD_NUM));
        }
        // 创建场景线程组
        matchMsgProcessor = new MatchThreadProcessGroup(ThreadConstants.MATCH_THREAD_NUM);
    }

    @Override
    public void onStart() throws Exception {
        matchMsgProcessor.start();
    }

    @Override
    public void onStop() throws Exception {
        matchMsgProcessor.stop();
    }
}
