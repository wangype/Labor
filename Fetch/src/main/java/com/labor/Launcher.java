package com.labor;

import org.apache.commons.cli.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 总启动类
 * Created by wyp on 15/8/11.
 */
public class Launcher {


    public static void main(String[] args) {
        // 1.解析参数
        Options options = new Options();
        options.addOption("m", true, "Mails for reg");
        options.addOption("v", true, "The vip info");
        options.addOption("e", true, "events.csv");
        options.addOption("c", true, "confirm_time");
        char[] requres = new char[]{'m', 'v', 'e', 'c'};
        options.addOption("f", true, "fetch event id");
        options.addOption("g", false, "get result");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            String fileName = cmd.getOptionValue("f");
            if (fileName != null) {
                fetchEventID(fileName);
                return;
            }


            if (cmd.hasOption('g') && cmd.hasOption('m')) {
                // 2.收集信息
                String mailsFile = cmd.getOptionValue("m");
                if (mailsFile != null) {
                    getMail(Utils.readCSVFile(new File(mailsFile)));
                    return;
                }
            }

            for (int i = 0; i < requres.length; i++) {
                char c = requres[i];
                if (!cmd.hasOption(c)) {
                    System.out.println("参数错误");
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp("help", options);
                    System.exit(0);
                }
            }
            // 2.收集信息
            String mailsFile = cmd.getOptionValue("m");
            CollectorInfo.setValue(Constants.MAILS, Utils.readCSVFile(new File(mailsFile)));

            String vipFile = cmd.getOptionValue("v");
            CollectorInfo.setValue(Constants.VIPS, Utils.readCSVFile(new File(vipFile)));

            String eventFile = cmd.getOptionValue("e");
            List<Map<String, String>> eventList = Utils.readCSVFile(new File(eventFile));
            if (eventList.size() == 0) {
                System.out.println("eventID没有");
                System.exit(0);
            } else {
                Map<String, String> eventMap = eventList.get(0);
                CollectorInfo.setValue(Constants.EVENT_ID, eventMap.get("event_id"));
                CollectorInfo.setValue(Constants.EVENT_TYPE, eventMap.get("event_type"));
            }

            String confirm_time = cmd.getOptionValue("c");
            Date date = Utils.strToDate(confirm_time);
            CollectorInfo.setValue(Constants.EXCUTETIME, date);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            System.out.println("时间格式为：yyyy-MM-dd");
            System.exit(0);
        }

        // 3.执行任务
        Runner runner = new Runner();
        runner.excute();
    }


    private static void fetchEventID(String fileName) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        Properties prop = new Properties();
        InputStream fileInputStream = Test.class.getClassLoader().getResourceAsStream("event.properties");
        BufferedReader bf = new BufferedReader(new InputStreamReader(fileInputStream, "utf-8"));
        prop.load(bf);
        Pattern pattern = Pattern.compile("event_id=(\\d+)");
        Map<String, StringBuffer> allMap = new HashMap<String, StringBuffer>();
        for (Object o : prop.keySet()) {
            String shopName = o.toString();
            String url = prop.get(shopName).toString();
            CloseableHttpResponse response = Utils.getUtilOK(httpclient, url, null, 1, -1);
            String content = EntityUtils.toString(response.getEntity(), "utf-8");
            Document doc = Jsoup.parse(content);
            Elements elements = doc.getElementsByClass("evtListTable");
            Iterator<Element> iterator = elements.iterator();
            StringBuffer stringBuffer = new StringBuffer();
            while (iterator.hasNext()) {
                Element element = iterator.next();
                Elements tr = element.select("tr");
                Iterator<Element> iterator2 = tr.iterator();
                while (iterator2.hasNext()) {
                    Element element1 = iterator2.next();
                    if (element1.getElementsMatchingText("予約受付期間").size() > 0) {
                        stringBuffer.append(element1.getElementsByTag("td") + "\n");
                    }
                }
                Elements links = element.select("a[href]");
                String s = links.first().toString();
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    stringBuffer.append(matcher.group(1) + "\n");
                }
            }
            allMap.put(shopName, stringBuffer);
        }

        File file = new File(fileName);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            for (String shopName : allMap.keySet()) {
                fileOutputStream.write((shopName + "\n").getBytes());
                fileOutputStream.write((allMap.get(shopName).toString() + "\n").getBytes());
                fileOutputStream.write(("------------------------------------------" + "\n").getBytes());
            }
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
        System.out.println("生成文件:" + file.getAbsolutePath());
    }


    private static void getMail(List<Map<String, String>> mailsInfo) {
        System.out.println("开始输出结果到文件");
        for (Map<String, String> mailMap : mailsInfo) {
            String mailUser = mailMap.get(Constants.USERMAIL);
            List<String> contents = MailUtils.getComfirmInfoFromMail(mailMap.get(Constants.HOST),
                    mailUser,
                    mailMap.get(Constants.PASSWORD));
            for (String content : contents) {
                String name = mailUser + ".txt";
                Utils.outputFile(content, name);
                System.out.println("输出文件：" + name);
            }
        }
        System.out.println("运行结束");
    }


}
