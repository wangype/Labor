package com.labor;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
//        options.addOption("r", false, "receive mail");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            Collection<Option> optionCollection = options.getOptions();
            for (Option option : optionCollection) {
                if (!cmd.hasOption(option.getOpt())) {
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


}
