package com.labor;

import org.apache.commons.cli.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.text.SimpleDateFormat;
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
        options.addOption("p", true, "excel path");
        options.addOption("s", true, "eventFilePath,  date(MM-dd)");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            String fileName = cmd.getOptionValue("f");
            if (fileName != null) {
                fetchEventID(fileName);
                return;
            }
            if (cmd.hasOption('s')) {
                if (args.length == 3) {
                    synEventID(args[1], args[2]);
                    return;
                }
            }
            String excelFile = cmd.getOptionValue('p');
            if (excelFile != null) {
                generateCSV(excelFile);
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


    private static void synEventID(String eventDirPath, String date) {
        File dir = new File(eventDirPath);
        File[] eventFiles = dir.listFiles();
        Map<String, File> eventFilesMap = new HashMap<String, File>();
        for (File eventFile : eventFiles) {
            eventFilesMap.put(eventFile.getName(), eventFile);
        }
        try {
            SimpleDateFormat simpleDate = new SimpleDateFormat("MM-dd");
            Date userDate = simpleDate.parse(date);

            SimpleDateFormat format = new SimpleDateFormat("M/dd");

            CloseableHttpClient httpclient = HttpClients.createDefault();
            Properties prop = new Properties();
            InputStream fileInputStream = Test.class.getClassLoader().getResourceAsStream("event.properties");
            BufferedReader bf = new BufferedReader(new InputStreamReader(fileInputStream, "utf-8"));
            prop.load(bf);
            Pattern NamePattern = Pattern.compile("\\((.+)\\)");
            Set<String> record = new HashSet<String>();
            Pattern pattern = Pattern.compile("event_id=(\\d+)");
            for (Object o : prop.keySet()) {
                String shopName = o.toString();
                String url = prop.get(shopName).toString();
                Matcher match = NamePattern.matcher(shopName);
                if (match.find()) {
                    shopName = match.group(1);
                }
                CloseableHttpResponse response = Utils.getUtilOK(httpclient, url, null, 1, -1);
                String content = EntityUtils.toString(response.getEntity(), "utf-8");
                Document doc = Jsoup.parse(content);
                Elements elements = doc.getElementsByClass("evtListTable");
                Iterator<Element> iterator = elements.iterator();
                int count = 0;
                while (iterator.hasNext()) {
                    Element element = iterator.next();
                    Elements tr = element.select("tr");
                    Iterator<Element> iterator2 = tr.iterator();
                    while (iterator2.hasNext()) {
                        Element element1 = iterator2.next();
                        if (element1.getElementsMatchingText("予約受付期間").size() > 0) {
                            String time = element1.getElementsByTag("td").first().ownText();
                            String[] ss = time.split("〜|~");
                            char[] chars = ss[0].toCharArray();
                            for (int i = 0; i < chars.length; i++) {
                                char aChar = chars[i];
                                if (aChar == 160) {
                                    chars[i] = 32;
                                }
                            }
                            String bb = new String(chars);
                            String[] bbs = bb.split(" ");
                            Date webDate = format.parse(bbs[0]);
                            if (webDate.compareTo(userDate) == 0) {
                                Elements links = element.select("a[href]");
                                String s = links.first().toString();
                                Matcher matcher = pattern.matcher(s);
                                if (matcher.find()) {
                                    String evendID = matcher.group(1);
                                    File file = null;
                                    if (record.contains(shopName)) {
                                        count++;
                                        file = new File(dir.getAbsolutePath() + "/" + "events_" + shopName + count + ".csv");
                                    } else {
                                        file = eventFilesMap.get("events_" + shopName + ".csv");
                                        if (file == null) {
                                            System.out.println(shopName + "配置有误");
                                            continue;
                                        }

                                    }
                                    System.out.println("同步文件：" + file.getName());
                                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                                    Utils.writeCsv(new String[]{"event_id", "event_type"}, fileOutputStream);
                                    Utils.writeCsv(new String[]{evendID, "7"}, fileOutputStream);
                                    fileOutputStream.close();
                                    record.add(shopName);
                                }
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                        element1.getElementsByTag("td").first().ownText();
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


    public static void generateCSV(String filePath) throws IOException {
        InputStream inputStream = new FileInputStream(filePath);
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(inputStream);
        HSSFSheet sheet = hssfWorkbook.getSheetAt(0);
        String[] needs = new String[]{"sei", "mei", "sei_kana", "mei_kana", "tel1", "tel2", "tel3", "password", "card_no"};
        int[] coloums = new int[]{0, 1, 2, 3, 4, 5, 6, 11};
        int rows = sheet.getPhysicalNumberOfRows();
        File csv = new File("out.csv");
        FileOutputStream fileOutputStream = new FileOutputStream(csv);
        Utils.writeCsv(needs, fileOutputStream);
        for (int i = 0; i < rows; i++) {
            if (i == 0)
                continue;
            Row row = sheet.getRow(i);
            List<String> contents = new ArrayList<String>();
            for (int j = 0; j < coloums.length; j++) {
                Cell cell = row.getCell(coloums[j]);
                if (cell != null) {
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_FORMULA:
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            double b = cell.getNumericCellValue();
                            if (coloums[j] == 4) {
                                contents.add("0" + String.valueOf((int) b));
                            } else {
                                contents.add(String.valueOf((int) b));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            contents.add(cell.getStringCellValue().replace("\n", ""));
                            break;
                        default:
                            contents.add("error");
                            break;
                    }
                }
            }
            contents.add(7, "1234567i");
            Utils.writeCsv(contents.toArray(new String[0]), fileOutputStream);
        }
        System.out.println("生成成功");
    }

}
