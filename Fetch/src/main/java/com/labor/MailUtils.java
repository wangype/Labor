package com.labor;

import org.apache.commons.net.pop3.POP3Client;
import org.apache.commons.net.pop3.POP3MessageInfo;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by baidu on 15/8/11.
 */
public class MailUtils {

    private static Logger logger = Logger.getLogger(MailUtils.class);

    private static Pattern pattern = Pattern.compile("https://aksale.advs.jp/cp/akachan_sale_pc/reg\\?id=[^\b]+");


    public static void cleanMail(String host, String userMail, String passWord) {
        try {
            POP3Client pop3Client = new POP3Client();
            pop3Client.connect(host);
            boolean isLogin = pop3Client.login(userMail, passWord);
            if (!isLogin) {
                logger.error("邮箱登录有误：userName:" + userMail + " host: " + host + " pw: " + passWord);
                return;
            }
            POP3MessageInfo[] pop3MessageInfos = pop3Client.listMessages();
            for (POP3MessageInfo pop3MessageInfo : pop3MessageInfos) {
                int id = pop3MessageInfo.number;
                pop3Client.deleteMessage(id);
            }
            pop3Client.logout();
            pop3Client.disconnect();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }


    public static String getURLFromMail(String host, String userMail, String passWord) {
        String url = null;
        try {
            POP3Client pop3Client = new POP3Client();
            pop3Client.connect(host);
            boolean isLogin = pop3Client.login(userMail, passWord);
            if (!isLogin) {
                logger.error("邮箱登录有误：userName:" + userMail + " host: " + host + " pw: " + passWord);
                return null;
            }
            POP3MessageInfo[] pop3MessageInfos = pop3Client.listMessages();
            StringBuilder content = new StringBuilder();
            for (POP3MessageInfo pop3MessageInfo : pop3MessageInfos) {
                int id = pop3MessageInfo.number;
                Reader reader = pop3Client.retrieveMessage(id);
                BufferedReader br = new BufferedReader(reader);
                while (br.readLine() != null) {
                    content.append(br.readLine());
                }
                br.close();
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    url = matcher.group();
                    break;
                }
            }
            pop3Client.logout();
            pop3Client.disconnect();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return url;
    }


}
