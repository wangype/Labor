package com.labor;

import org.apache.commons.net.pop3.POP3Client;
import org.apache.commons.net.pop3.POP3MessageInfo;
import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wyp on 15/8/11.
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
            logger.info("Email [" + userMail + "] " + pop3MessageInfos.length + " mails in inbox: ");
            for (POP3MessageInfo pop3MessageInfo : pop3MessageInfos) {
                int id = pop3MessageInfo.number;
                pop3Client.deleteMessage(id);
                logger.info(String.format("[%s] clean 1 mail", userMail));
            }
            pop3Client.logout();
            pop3Client.disconnect();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

//
//    public static List<String> getURLFromMail(String host, String userMail, String passWord) {
//        List<String> urlList = new ArrayList<String>();
//        try {
//            POP3Client pop3Client = new POP3Client();
//            pop3Client.connect(host);
//            boolean isLogin = pop3Client.login(userMail, passWord);
//            if (!isLogin) {
//                logger.error("邮箱登录有误：userName:" + userMail + " host: " + host + " pw: " + passWord);
//                return null;
//            }
//            POP3MessageInfo[] pop3MessageInfos = pop3Client.listMessages();
//            for (POP3MessageInfo pop3MessageInfo : pop3MessageInfos) {
//                int id = pop3MessageInfo.number;
//                Reader reader = pop3Client.retrieveMessage(id);
//                BufferedReader br = new BufferedReader(reader);
//                String line;
//                StringBuffer stringBuffer = new StringBuffer();
//                while ((line = br.readLine()) != null) {
//                    stringBuffer.append(line);
//                    Matcher matcher = pattern.matcher(line);
//                    if (matcher.find()) {
//                        String url = matcher.group(0);
//                        urlList.add(url);
//                        break;
//                    }
//                }
//                stringBuffer.toString();
//            }
//            pop3Client.logout();
//            pop3Client.disconnect();
//        } catch (IOException e) {
//            logger.error(e.getMessage());
//        }
//        return urlList;
//    }


//    public static List<String> getComfirmInfo(String host, String userMail, String passWord) {
//        List<String> contentList = new ArrayList<String>();
//        try {
//            POP3Client pop3Client = new POP3Client();
//            pop3Client.connect(host);
//            boolean isLogin = pop3Client.login(userMail, passWord);
//            if (!isLogin) {
//                logger.error("邮箱登录有误：userName:" + userMail + " host: " + host + " pw: " + passWord);
//                return null;
//            }
//            POP3MessageInfo[] pop3MessageInfos = pop3Client.listMessages();
//            StringBuilder content = new StringBuilder();
//            for (POP3MessageInfo pop3MessageInfo : pop3MessageInfos) {
//                int id = pop3MessageInfo.number;
//                Reader reader = pop3Client.retrieveMessage(id);
//                BufferedReader br = new BufferedReader(reader);
//                while (br.readLine() != null) {
//                    content.append(br.readLine());
//                }
//                br.close();
//                if (Constants.PARRTERN_SUCCESS.matcher(content).find()) {
//                    contentList.add(content.toString());
//                }
//            }
//            pop3Client.logout();
//            pop3Client.disconnect();
//        } catch (IOException e) {
//            logger.error(e.getMessage());
//        }
//        return contentList;
//    }



    public static List<String> getComfirmInfoFromMail(String host, String userMail, String passWord) {
        List<String> contentList = new ArrayList<String>();
        try {
            Session session = createMailSession(host);
            Store store = session.getStore("pop3");
            store.connect(userMail, passWord);

            // 获得收件箱
            Folder folder = store.getFolder("INBOX");

            folder.open(Folder.READ_WRITE);    //打开收件箱
            // 得到收件箱中的所有邮件,并解析
            Message[] messages = folder.getMessages();

            for (int i = 0, count = messages.length; i < count; i++) {
                MimeMessage msg = (MimeMessage) messages[i];
                StringBuffer buffer = new StringBuffer();
                getMailTextContent(msg, buffer);
                if (Constants.PARRTERN_SUCCESS.matcher(buffer).find()) {
                    contentList.add(buffer.toString());
                }
            }
            //释放资源
            folder.close(true);
            store.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return contentList;
    }


    private static Session createMailSession(String host) {
        // 准备连接服务器的会话信息
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "pop3");        // 协议
        props.setProperty("mail.pop3.port", "110");                // 端口
        props.setProperty("mail.pop3.host", host);    // pop3服务器
        // 创建Session实例对象
        Session session = Session.getInstance(props);
        return session;
    }



    /**
     * 从邮件中获取url
     */
    public static List<String> getURLFromMail(String host, String userMail, String passWord) {
        List<String> urlList = new ArrayList<String>();
        try {
            Session session = createMailSession(host);
            Store store = session.getStore("pop3");
            store.connect(userMail, passWord);

            // 获得收件箱
            Folder folder = store.getFolder("INBOX");

            folder.open(Folder.READ_WRITE);    //打开收件箱
            // 得到收件箱中的所有邮件,并解析
            Message[] messages = folder.getMessages();

            for (int i = 0, count = messages.length; i < count; i++) {
                MimeMessage msg = (MimeMessage) messages[i];
                StringBuffer buffer = new StringBuffer();
                getMailTextContent(msg, buffer);
                String[] lines = buffer.toString().split("\n");
                for (String line : lines) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String url = matcher.group(0);
                        url = url.replaceAll("\n|\r", "");
                        urlList.add(url);
                    }
                }
            }
            //释放资源
            folder.close(true);
            store.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return urlList;
    }


    private static void getMailTextContent(Part part, StringBuffer content) throws MessagingException, IOException {
        //如果是文本类型的附件，通过getContent方法可以取到文本内容，但这不是我们需要的结果，所以在这里要做判断
        boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;
        if (part.isMimeType("text/*") && !isContainTextAttach) {
            content.append(part.getContent().toString());
        } else if (part.isMimeType("message/rfc822")) {
            getMailTextContent((Part) part.getContent(), content);
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                getMailTextContent(bodyPart, content);
            }
        }
    }

}
