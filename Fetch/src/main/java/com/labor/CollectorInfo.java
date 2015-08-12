package com.labor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**装在容器，存储用户传入参数
 * Created by baidu on 15/8/11.
 */
public class CollectorInfo {



    private static Map<String, Object> collectInfo = new HashMap<String, Object>();



    public static Object getValue(String key) {
        return collectInfo.get(key);
    }


    public static void setValue(String key, Object value) {
        collectInfo.put(key, value);
    }


}
