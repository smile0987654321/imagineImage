package com.sam.imagineimage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Pat on 18-Sep-17.
 */

public class ChainCodeFactory {
    private static Set<ChainCode> theSet;
    private static Set<String> theTempSet;
    private static Map<String, String> theMap; //TODO: Temporary solution before feature selection

    public ChainCodeFactory() {
        theSet = new HashSet<>();
        theTempSet = new HashSet<>();
        theMap = new HashMap<>();
        startFactory();
        theTempSet.add("abc");
    }

//    public static ChainCodeFactory createInstance() {
//        ChainCodeFactory instance = new ChainCodeFactory();
//        instance.startFactory();
//        return instance;
//    }

    public void startFactory() {
        //theSet.add
        theMap.put("4+6+8+2+",
                "0");
        theMap.put("4+6+8+2+",
                "1");
        theMap.put("4+6+8+\\d6+\\d4+6+8+2+4+\\d2+\\d8+2{1,3}",
                "2");
        theMap.put("4+6+8+2+4+\\d2+\\d8+2+4+\\d2+\\d8+2+",
                "3");
        theMap.put("4+6+\\d4+\\d2+4+6+8+2+\\d8+2+",
                "4");
        theMap.put("4+6+8+\\d6+\\d4+6+8+2+4+\\d2+\\d8+2+",
                "5");
        theMap.put("4+6+8+\\d6+\\d4+6+8+2+",
                "6");
        theMap.put("4+6+8+2+\\d8+2+",
                "7");
        theMap.put("4+6+8+2+",
                "8");
        theMap.put("4+6+8+2+4+\\d2+\\d8+2+",
                "9");
    }

    public static Map<String, String> getTheSet() { //TODO: Temporary set to string
        return theMap;
    }

//    public ChainCode getChainCode(String objectName) {
//        if (objectName == null) {
//            return null;
//        }
//        if (objectName.equalsIgnoreCase("zero")) {
//            return new ChainCode(8, 2, 3, 2, 8, 2, 3, 2);
//        }
//
//        return null;
//    }


}
