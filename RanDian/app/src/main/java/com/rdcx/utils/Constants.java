package com.rdcx.utils;

import com.rdcx.randian.R;

/**
 * Created by Administrator on 2015/10/29 0029.
 *
 * @author mengchuiliu
 */
public class Constants {
    // 发送验证码号码"10655020074106"
    public static final String YZM_PHONE_LT = "";// 联通
    public static final String YZM_PHONE_YD = "";// 移动
    public static final String YZM_PHONE_DX = "";// 电信

    //上传最多图片数
    public static final int MAX_IMAGE_SIZE = 9;

    // 一秒的毫秒数
    public static final long ONE_SECOND = 1000;

    // 一分钟的毫秒数
    public static final long ONE_MINUTE = ONE_SECOND * 60;

    // 一小时的毫秒数
    public static final long ONE_HOUR = ONE_MINUTE * 60;

    // 一天的毫秒数
    public static final long ONE_DAY = ONE_HOUR * 24;

    // 一周的毫秒数
    public static final long ONE_WEEK = ONE_DAY * 7;

    public static final int[] image = new int[]{R.mipmap.icon_2, R.mipmap.icon_3, R.mipmap.icon_8,
            R.mipmap.icon_1, R.mipmap.social, R.mipmap.icon_5, R.mipmap.video, R.mipmap.game, R.mipmap.read, R.mipmap.money, R.mipmap.icon_7, R.mipmap.time_axis};

    public static final int[] image1 = new int[]{R.mipmap.icon_2, R.mipmap.icon_3, R.mipmap.icon_8,
            R.mipmap.icon_1, R.mipmap.social, R.mipmap.icon_5, R.mipmap.video, R.mipmap.game, R.mipmap.read, R.mipmap.money, R.mipmap.icon_7};

    public static final String[] rankTitle = new String[]{"社交", "购物", "视频", "游戏", "理财", "阅读",
            "资讯", "生活", "办公", "出行", "健康", "拍照", "教育", "工具", "安全", "通讯", "浏览器"};

    public static final String[] rankOneself = new String[]{"勤劳度", "通话", "社交", "购物", "视频", "游戏"};

    // 正式服务器地址
    public static final String my_url = "http://service.randiancx.com/maven-web-demo";
    // 测试服务器
//    public static final String my_url = "http://120.24.253.146:8081/maven-web-demo";
    // 本地服务器地址
//    public static final String my_url = "http://192.168.0.115:8080/maven-web-demo";


    // webview服务器正式地址
//    public static final String web_url = "http://120.24.160.15:8080/";
    // 测试
//    public static final String web_url = "http://120.24.253.146:8081/";
    // webview本地地址
    public static final String web_url = "http://service.randiancx.com/";

    //头像地址
    public static final String head_url = "http://service.randiancx.com";

    // 短信验证
    public static final String YANZHENG_URL = my_url + "/user/sendSms.do";
    // 注册
    public static final String REGIST_URL = my_url + "/user/register.do";
    // 登录
//    public static final String LOGIN_URL = my_url + "/user/login.do";
    public static final String LOGIN_URL = my_url + "/user/loginSecurityLogic.do";
    // 绑定
    public static final String BINDING_USER = my_url + "/user/bindingUser.do";
    // 解绑
    public static final String UNBINDING_USER = my_url + "/user/unbundlingUser.do";
    // 修改密码
    public static final String RESET_PSW = my_url + "/user/editPassword.do";
    // 忘记密码
    public static final String FORGOT_PSW = my_url + "/user/forgotPassword.do";
    // 修改昵称
    public static final String UPDATE_NAME = my_url + "/user/editPhoneName.do";
    // 上传电话信息
    public static final String PHONE_INFO = my_url + "/upload/callRecords.do";
    // 上传照片信息
    public static final String PHOTO_INFO = my_url + "/upload/photoRecords.do";
    // 上传搜集应用信息
    public static final String APPLY_INFO = my_url + "/upload/otherRecords.do";
    // 验证手机号码是否存在
    public static final String User_Exist = my_url + "/user/existsPhoneNumber.do";
    // 意见反馈
    public static final String FEEDBACK = my_url + "/upload/feedBack.do";
    // 应用白名单
    public static final String APPLY_NAME = my_url + "/upload/findActivityList.do";
    // 上传头像到服务端
    public static final String PHOTO_UPLOAD = my_url + "/upload/uploadPhoto.do";
    // 上传头像地址信息
    public static final String ICON_INFO = my_url + "/user/updatePhotoById.do";
    // 最多联系人
    public static final String CONTACT_MORE = my_url + "/home/contactService.do";
    // 照片文案
    public static final String MYPHOTOTEXT = my_url + "/home/photoService.do";
    // GPS返回
    public static final String GETGPS = my_url + "/home/homeGPSService.do";

    // 上传联系人最大Id
    public static final String CONTACT_ID = my_url + "/upload/uploadGrabNumber.do";
    // 获取联系人最大Id
    public static final String GET_CONTACT_ID = my_url + "/upload/findGrabNumber.do";
    // 上传经纬度
    public static final String UP_LOC = my_url + "/upload/uploadGPSRecords.do";
    // 消息盒子
    public static final String MESSAGEBOX = my_url + "/messageBox/findMessageBox.do";
    // 获取推荐标签
    public static final String LABEL = my_url + "/messageBox/findLabelByType.do";
    // 退出登录
    public static final String LOGINOUT = my_url + "/user/loginOut.do";

    // 时间戳  入参：type
    public static final String TIME_STAMP = my_url + "/rule/getDateTime.do";
    // 闪屏页
    public static final String FLASH_PAGE = my_url + "/rule/getRuleMenuList.do";
    // 推送规则
    public static final String PUSH_RULE = my_url + "/rule/getPushRuleByAll.do";
    // 通过包名获取应用信息
    public static final String GET_ACTIVITY_BY_NAME = my_url + "/rule/getActivityByName.do";
    // 查询所有文案
    public static final String GET_RULE_TEXT = my_url + "/rule/getRuleText.do";

    //上传本地动态
    public static final String Loc_Dynamic = my_url + "/main/addDynamic.do";

    // 下载应用使用信息
    public static final String FIND_UPLOAD_OTHER = my_url + "/upload/findUploadOther.do";

    // 下载足迹信息
    public static final String FIND_UPLOAD_GPS = my_url + "/upload/findUploadGSP.do";

    // 下载通话信息
    public static final String FIND_UPLOAD_PHONE = my_url + "/upload/findUploadPhone.do";

    //上传推送Id
    public static final String ClientId = my_url + "/upload/addDeviceToken.do";

    //当前时间
    public static final String NowTime = my_url + "/rule/getNowDate.do";

    //个人排行榜
    public static final String RankList = my_url + "/rankList/getRankList.do";

    //应用排行榜
    public static final String AppRankList = my_url + "/rankList/getApplicationRank.do";

    //上传日记本数据
    public static final String UploadDiary = my_url + "/largeBills/addDiary.do";

    //获取日记本数据
    public static final String GetDiaryData = my_url + "/largeBills/findDiaryList.do";

    // 查询所有任务清单
    public static final String GET_TASK_BY_ALL = my_url + "/rule/getTaskByAll.do";

    // 查询用户个人的任务完成情况
    public static final String TASK_ANSWER_BY_USER_ID = my_url + "/rule/taskAnswerByUserId.do";

    // 人工验证的任务提交
    public static final String ADD_TASK_PHOTO = my_url + "/rule/addTaskPhoto.do";

    // 自动验证的任务提交
    public static final String ADD_TASK_ANSWER = my_url + "/rule/addTaskAnswer.do";

    // 通过 userId 和 taskId 去查找用户已提交的人工验证的任务
    public static final String FIND_TASK_PHOTO_BY_USER_ID = my_url + "/rule/findTaskPhotoByUserId.do";

    // 完成阶段任务
    public static final String ADD_TASK_GUIDE = my_url + "/rule/addTaskGuide.do";

    // 阶段任务完成情况查询
    public static final String FIND_TASK_GUIDE_BY_USER_ID = my_url + "/rule/findTaskGuiDeByUserId.do";

    // 查询获奖情况
    public static final String FIND_REWARD_BY_USER_ID = my_url + "/largeBills/findRewardByUserId.do";

    // 查询所有人的获取情况，根据时间后先进行排序，只查找前六位
    public static final String FIND_REWARD_BY_TOP = my_url + "/largeBills/findRewardByTop.do";

    // 查找所有奖项
    public static final String FIND_ENVELOP_BY_ALL = my_url + "/largeBills/findEnveLopByAll.do";

    // 任务清单 Banner 图接口
    public static final String FIND_GIF_BY_ALL = my_url + "/rule/findGifByAll.do";

    // 获取排行榜标签
    public static final String GetRankLable = my_url + "/rankList/geLableRankList.do";

    // 个人排行榜
    public static final String GetPersonalRank = my_url + "/rankList/getRankPersonal.do";

    public static final String GetRankIcon = my_url + "/user/getPhonePathById.do";

    // 好友任务排行
    public static final String TASK_FRIEND_RANK = my_url + "/rule/findTaskAnswerByFocus.do";

    // 全国任务排行
    public static final String TASK_NATIONAL_RANK = my_url + "/rule/findTaskAnswerByUser.do";

    //闪屏页
    public static final String SPLASHSCREEN = my_url + "/rule/findSplashScreen.do";

    //上传唯一字符串
    public static final String UpdateStr = my_url + "/main/addFirstApp.do";

    //上传渠道号
    public static final String uploadChannel = my_url + "/user/channelBinding.do";

}
