package com.rdcx.tools;

/**
 * Created by Administrator on 2015/12/15 0015.
 * <p/>
 * 一条文案
 */
public class RuleText {

    // ID 号
    public int _id;

    // 类型 1：日维度 2：周维度 3：月维度 4：年维度，5：推荐日维度，6：推荐周维度
    public int type;

    // 细分类型 1：QQ使用时长、2：微信使用时长、3：总通话时长、4：购物时长、5：电话进入时间、6：总通话次数、7：和XX通话次数、8：和XX通话时长、9：最长未通话时长、10：照片、11：拒接+未接电话次数
    // 推荐日维度 1-11
    // 推荐周维度 1-11 没有 9
    // 日周月维度 1、2、3、4、8、10
    public int ruleType;

    // 优先级
    public int priority;

    //起始日期
    public long startDate;

    // 结束日期
    public long endDate;

    // 最小匹配值
    public long ruleStartValue;

    // 最大匹配值
    public long ruleEndValue;

    /**
     * 图片 url
     */
    public String rulePhotoUrl;

    // 关键词
    public String keywords;

    // 文案名
    public String ruleName;

    // 文案内容
    public String ruleText;

    @Override
    public String toString() {
        return "RuleText{" +
                "_id=" + _id +
                ", type=" + type +
                ", ruleType=" + ruleType +
                ", priority=" + priority +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", ruleStartValue=" + ruleStartValue +
                ", ruleEndValue=" + ruleEndValue +
                ", rulePhotoUrl='" + rulePhotoUrl + '\'' +
                ", keywords='" + keywords + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", ruleText='" + ruleText + '\'' +
                '}';
    }
}
