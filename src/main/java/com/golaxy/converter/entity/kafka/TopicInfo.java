package com.golaxy.converter.entity.kafka;

/**
 * Created by yangzongze on 2017/11/29.
 */
public class TopicInfo {

    private String topicName;
    private String consumerName;
    private int consumerThreadNum;

    public TopicInfo(String topicName, String consumerName, int consumerThreadNum) {
        this.topicName = topicName;
        this.consumerName = consumerName;
        this.consumerThreadNum = consumerThreadNum;
    }
    public String getTopicName() {
        return topicName;
    }
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
    public String getConsumerName() {
        return consumerName;
    }
    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }
    public int getConsumerThreadNum() {
        return consumerThreadNum;
    }
    public void setConsumerThreadNum(int consumerThreadNum) {
        this.consumerThreadNum = consumerThreadNum;
    }
}
