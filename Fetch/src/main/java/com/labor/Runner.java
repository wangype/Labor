package com.labor;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 执行器
 * Created by wyp on 15/8/11.
 */
public class Runner {


    private static Logger logger = Logger.getLogger(Runner.class);
    private Object lock = new Object();

    public void excute() {
        logger.info("开始清理邮箱");
        // 1.清理邮箱
        List<Map<String, String>> mailsInfo = (List<Map<String, String>>) CollectorInfo.getValue(Constants.MAILS);
        for (Map<String, String> mailMap : mailsInfo) {
            MailUtils.cleanMail(mailMap.get(Constants.HOST),
                    mailMap.get(Constants.USERMAIL),
                    mailMap.get(Constants.PASSWORD));
            Utils.threadSleep(500);
        }
        // 2. 等待启动时间
        Date excuteTime = (Date) CollectorInfo.getValue(Constants.EXCUTETIME);
        long startTime = excuteTime.getTime();
        logger.info(String.format("等待执行时间[%s]", Utils.dateToStr(excuteTime)));
        while (true) {
            long current = System.currentTimeMillis();
            if (current >= startTime) {
                break;
            }
            Utils.threadSleep(300);
        }
        logger.info("执行时间到，开始执行");

        ExecutorService executorService = Executors.newCachedThreadPool();

        List<Map<String, String>> vipInfo = (List<Map<String, String>>) CollectorInfo.getValue(Constants.VIPS);
        int size = mailsInfo.size();
        CountDownLatch countDownLatch = new CountDownLatch(size);
        int index = 0;
        for (Map<String, String> mailInfo : mailsInfo) {
            // 3. 开始进行邮件注册
            executorService.execute(new RegForOrder(mailInfo.get(Constants.USERMAIL), countDownLatch));
            // 4. 填写信息
            executorService.execute(new ComfirmOrder(mailInfo.get(Constants.USERMAIL), mailInfo.get(Constants.HOST),
                    mailInfo.get(Constants.PASSWORD), vipInfo.get(index), countDownLatch));
            index++;
        }

        // 主线程等子线程完成
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 5.从邮箱获取结果，输出到文件
        logger.info("开始输出结果到文件");
        for (Map<String, String> mailMap : mailsInfo) {
            String mailUser = mailMap.get(Constants.USERMAIL);
            List<String> contents = MailUtils.getComfirmInfoFromMail(mailMap.get(Constants.HOST),
                    mailUser,
                    mailMap.get(Constants.PASSWORD));
            for (String content : contents) {
                String name = mailUser + ".txt";
                Utils.outputFile(content, name);
                logger.info("输出文件：" + name);
            }
        }
        executorService.shutdown();
        logger.info("运行结束");
    }


    // 注册邮件线程
    private class RegForOrder extends Thread {

        private String mailUser;
        private CountDownLatch countDownLatch;

        public RegForOrder(String mailUser, CountDownLatch countDownLatch) {
            this.mailUser = mailUser;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            // 注册邮箱
            String eventID = (String) CollectorInfo.getValue(Constants.EVENT_ID);
            String eventType = (String) CollectorInfo.getValue(Constants.EVENT_TYPE);
            String requestURL = "https://aksale.advs.jp/cp/akachan_sale_pc/mail_confirm.cgi";
            Map<String, String> params = new HashMap<String, String>();
            params.put("sbmt", Constants.SBMT);
            params.put("event_id", eventID);
            params.put("event_type", eventType);
            params.put("mail1", mailUser);
            params.put("mail2", mailUser);
            CloseableHttpClient httpclient = HttpClients.createDefault();
            // 发送注册请求
            logger.info(String.format("Email [%s] 开始进行注册", mailUser));
            CloseableHttpResponse reponse = Utils.postUtilNoDbFailure(httpclient, requestURL, params, 5, mailUser, 6000);
            if (reponse != null) {
                logger.info(String.format("Email [%s] 注册成功", mailUser));
            } else {
                logger.info(String.format("Email [%s] 注册失败", mailUser));
                synchronized (lock) {
                    failSet.add(mailUser);
                    countDownLatch.countDown();
                }
            }
        }
    }


    private Set<String> failSet = new HashSet<String>();

    // 提取URL并填写信息线程
    private class ComfirmOrder extends Thread {

        private String mailUser;
        private String host;
        private String passWord;
        private Map<String, String> vipMap;
        private CountDownLatch countDownLatch;

        public ComfirmOrder(String mailUser, String host, String passWord, Map<String, String> vipMap,
                            CountDownLatch countDownLatch) {
            this.mailUser = mailUser;
            this.host = host;
            this.passWord = passWord;
            this.vipMap = vipMap;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            List<String> urlList = null;
            // 从邮箱获取url
            boolean regStatus = true;
            while (true) {
                urlList = MailUtils.getURLFromMail(host, mailUser, passWord);
                if (urlList != null && urlList.size() > 0) {
                    logger.info(String.format("[%s]检查到注册邮件", mailUser));
                    break;
                }
                logger.info(String.format("[%s] 检查邮箱中注册邮件", mailUser));
                Utils.threadSleep(1000);
                synchronized (lock) {
                    // 注册邮件失败，这里就不再进行检查
                    if (failSet.remove(mailUser)) {
                        regStatus = false;
                        break;
                    }
                }
            }
            //注册失败就不进行之后步骤了
            if (!regStatus) {
                return;
            }
            // 根据获取url开始填写信息
            final CloseableHttpClient httpclient = HttpClients.createDefault();
            logger.info("[" + mailUser + "]获取到url数量:" + urlList.size());

            final CountDownLatch latch = new CountDownLatch(urlList.size());
            for (final String url : urlList) {
                new Thread(new Runnable() {
                    public void run() {
                        logger.info("get url from mail :" + url);
                        // 发送get请求查看url是否可用
                        logger.info(String.format("[%s]验证邮箱中url是否正确", mailUser));
                        String ret = checkUrlValid(url, httpclient);
                        logger.info(String.format("[%s]" + ret, mailUser));
                        logger.info(String.format("[%s]开始提交卡号", mailUser));
                        // 提交卡号
                        ret = submitCardNO(vipMap.get(Constants.CARD_NO), httpclient);
                        logger.info(String.format("[%s]" + ret, mailUser));
                        // 填写会员信息
                        ret = regFormEvent(vipMap, httpclient);
                        logger.info(String.format("[%s]" + ret, mailUser));
                        // 确认信息
                        logger.info(String.format("[%s]开始确认信息", mailUser));
                        ret = makeSure(httpclient);
                        logger.info(String.format("[%s]" + ret, mailUser));
                        latch.countDown();
                    }
                }).start();
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            countDownLatch.countDown();
        }


        private String makeSure(CloseableHttpClient httpclient) {
            String request_url = "https://aksale.advs.jp/cp/akachan_sale_pc/reg_confirm_event.cgi";
            Map<String, String> params = new HashMap<String, String>();
            params.put("sbmt", Constants.SBMT);
            CloseableHttpResponse response = Utils.postUtilNoDbFailure(httpclient, request_url, params, 5, mailUser, -1);
            return "已确认";
        }


        private String regFormEvent(Map<String, String> vipMap, CloseableHttpClient httpclient) {
            String request_url = "https://aksale.advs.jp/cp/akachan_sale_pc/reg_form_event_1.cgi";
            Map<String, String> params = new HashMap<String, String>();
            params.put(Constants.VIPPASSWORD, vipMap.get(Constants.VIPPASSWORD));
            params.put(Constants.SEI, Utils.encodeJP(vipMap.get(Constants.SEI)));
            params.put(Constants.MEI, Utils.encodeJP(vipMap.get(Constants.MEI)));
            params.put(Constants.MEI_KANA, Utils.encodeJP(vipMap.get(Constants.MEI_KANA)));
            params.put(Constants.SEI_KANA, Utils.encodeJP(vipMap.get(Constants.SEI_KANA)));
            params.put(Constants.TEL1, vipMap.get(Constants.TEL1));
            params.put(Constants.TEL2, vipMap.get(Constants.TEL2));
            params.put(Constants.TEL3, vipMap.get(Constants.TEL3));
            params.put(Constants.SBMT, Constants.SBMT);
            CloseableHttpResponse response = Utils.postUtilNoDbFailure(httpclient, request_url, params, 5, mailUser, -1);
            return "已经填写";
        }

        private String submitCardNO(String cardNO, CloseableHttpClient httpclient) {
            String request_url = "https://aksale.advs.jp/cp/akachan_sale_pc/form_card_no.cgi";
            Map<String, String> params = new HashMap<String, String>();
            params.put("sbmt", Constants.SBMT);
            params.put("card_no", cardNO);
            CloseableHttpResponse response = Utils.postUtilNoDbFailure(httpclient, request_url, params, 5, mailUser, -1);
            return "已提交卡号";
        }


        private String checkUrlValid(String url, CloseableHttpClient httpclient) {
            CloseableHttpResponse response = Utils.getUtilNoErr(httpclient, url, null, 5, mailUser, 10000);
            if (response == null) {
                logger.error("提取的url有误，无法打开 " + url);
                return String.format("url [%s] error", url);
            }
            return String.format("提取url[%s]正确", url);
        }
    }

}
