package com.labor;

import org.apache.commons.cli.*;

import java.util.Collection;

/**总启动类
 * Created by baidu on 15/8/11.
 */
public class Launcher {


    public static void main(String[] args) {
        // 1.解析参数
        Options options = new Options();
        options.addOption("m", true, "Mails for reg");
        options.addOption("v", true, "The vip info");
        options.addOption("e", true, "events.csv");
        options.addOption("c", true, "confirm_time");
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
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 2.收集信息
        // 3.执行任务

    }



}
