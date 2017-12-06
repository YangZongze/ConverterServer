package com.golaxy.converter.entity.frontend;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 状态吗
 */
public class StatusCode {

    /** controller接口返回code码 **/
    /** 接口返回数据成功 **/
    public static final int SUCESS_CODE = 200;
    /** 缺少md5 **/
    public static final int UPLOAD_MD5_REQUIRED = 101;
    /** 错误md5 **/
    public static final int UPLOAD_MD5_INVALID = 102;
    /** 原始文件已存在 **/
    public static final int RAW_FILE_EXSIT = 103;
    /** 原始文件不存在 **/
    public static final int RAW_FILE_NONEXSIT = 104;
    /** 用户文件已存在 **/
    public static final int USER_FILE_EXSIT = 105;
    /** 缺少username/uid/md5 **/
    public static final int UPLOAD_PARAMS_LACK = 106;
    /** 上传成功 **/
    public static final int UPLOAD_SUCCESS = 200;
    /** 上传失败 **/
    public static final int UPLOAD_FAILURE = 107;


    /** websocket通信code码对应的类型 **/
    /** websocket连接成功 **/
    public static final int WEBSOCKET_CONNECTED = 0;
    /** 正在进度 **/
    public static final int WEBSOCKET_UPLOAD_PROGRESS = 1;
    /** 上传速度 **/
    public static final int WEBSOCKET_UPLOAD_SPEED = 2;
    /** 转换排队中 **/
    public static final int WEBSOCKET_CONVERT_QUEUE = 3;
    /** 正在转换 **/
    public static final int WEBSOCKET_CONVERT_RUNNING = 4;
    /** 转换成功 **/
    public static final int WEBSOCKET_CONVERT_SUCCESS = 5;
    /** 转换失败 **/
    public static final int WEBSOCKET_CONVERT_FAILURE = 6;
    /** gitlab保存成功 **/
    public static final int WEBSOCKET_GITLAB_SAVED = 7;


    /** 数据库转换结果码 **/
    /** 转换成功 **/
    public static final boolean MYSQL_CONVERT_SUCCESS = true;
    /** 转换失败 **/
    public static final boolean MYSQL_CONVERT_FAILURE = false;


    /** 数据库转换状态码 **/
    /** 未开始转换 **/
    public static final int MYSQL_CONVERT_NOT_STARTED = 0;
    /** 排队成功 **/
    public static final int MYSQL_CONVERT_INQUEUE_SUCCESS = 1;
    /** 排队失败 **/
    public static final int MYSQL_CONVERT_INQUEUE_FAILURE = 2;
    /** 正在转换 **/
    public static final int MYSQL_CONVERT_RUNNING = 3;
    /** 转换成功 **/
    public static final int MYSQL_CONVERT_FINISHED_SUCCESS = 4;
    /** 转换失败 **/
    public static final int MYSQL_CONVERT_FINISHED_FAILURE = 5;

}
