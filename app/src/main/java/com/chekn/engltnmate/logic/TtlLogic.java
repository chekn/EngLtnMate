package com.chekn.engltnmate.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by CHEKN on 2019-2-23.
 */

public class TtlLogic {


     private static Map<String,String> dic = new HashMap<String,String>(){
        {
            this.put("人教","rj");
            this.put("仁爱","ra");

            this.put("小学","ltl");
            this.put("初中","mid");
            this.put("高中","hig");

            this.put("一年级","1");
            this.put("二年级","2");
            this.put("三年级","3");
            this.put("四年级","4");
            this.put("五年级","5");
            this.put("六年级","6");
            this.put("七年级","7");
            this.put("八年级","8");
            this.put("九年级","9");

            this.put("高一","1");
            this.put("高二","2");
            this.put("高三","3");

            this.put("上","up");
            this.put("下","down");

        }
    };

    public static String parse(String ttl){
        String[] level = ttl.split("/");

        StringBuilder strBu = new StringBuilder();
        java.util.Iterator levelite = Arrays.asList(level).iterator();
        while(true){
            String lv = (String) levelite.next();
            strBu.append(dic.get(lv.trim()));

            if(levelite.hasNext())
                strBu.append("_");
            else
                break;
        }
        return strBu.toString();
    }

}
