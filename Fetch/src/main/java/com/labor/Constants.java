package com.labor;

import java.util.regex.Pattern;

/**
 * Created by wyp on 15/8/12.
 */
public class Constants {


    /**
     * 传入参数
     */
    public static String MAILS = "MAILS";

    public static String EVENTS = "EVENTS";

    public static String VIPS  = "VIPS";

    public static String EXCUTETIME = "EXCUTETIME";


    /**
     * 邮箱参数
     */
    public static String HOST = "mail_host";

    public static String USERMAIL = "mail_user";

    public static String PASSWORD = "mail_passwd";


    /**
     * event参数
     */
    public static String EVENT_ID = "event_id";

    public static String EVENT_TYPE = "event_type";


    /**
     * 会员参数
     */
    public static String SEI = "sei";

    public static String MEI = "mei";

    public static String SEI_KANA = "sei_kana";

    public static String MEI_KANA = "mei_kana";

    public static String TEL1 = "tel1";

    public static String TEL2 = "tel2";

    public static String TEL3 = "tel3";

    public static String VIPPASSWORD = "password";

    public static String CARD_NO = "card_no";


    public static String SBMT = "%97%5C%96%F1";

    /**
     * 网站返回字符
     */
    public static Pattern PARRTERN_DB_FAIL = Pattern.compile("DB接続に失敗しました");
    public static Pattern PARRTERN_SESSION_ERR = Pattern.compile("セッションエラーが発生しました。恐れ入りますが、もう一度最初から操作してください");
    public static Pattern PARRTERN_UNKNOWN_ERR = Pattern.compile("エラーが発生しました。恐れ入りますが、もう一度最初から操作してくださ");
    public static Pattern PARRTERN_ORDER_SUCCESS = Pattern.compile("予約完了");
    public static Pattern PARRTERN_ORDER_FULL = Pattern.compile("ただいま満席のため");
    public static Pattern PARRTERN_ORDERED = Pattern.compile("既にご予約済み");
    public static Pattern PARRTERN_BEFORE_START = Pattern.compile("本イベントは受付期間外のため");
    public static Pattern PARRTERN_TOO_BUSY = Pattern.compile("Too Busy");
    public static Pattern PARRTERN_ZSCALER = Pattern.compile("Zscaler Directory Authentication");
    public static Pattern PARRTERN_VIP_HAS_RESERVED = Pattern.compile("既にご予約済みですので、新規予約ができません");
    public static Pattern PARRTERN_REG_ERROR = Pattern.compile("不正なアクセスです。恐れ入りますが、もう一度最初から操作してください");
    public static Pattern PARRTERN_OUT_ERROR = Pattern.compile("予約数に達したため、受付は終了いたしました");


    public static Pattern PARRTERN_SUCCESS = Pattern.compile("予約受付番号");





}
