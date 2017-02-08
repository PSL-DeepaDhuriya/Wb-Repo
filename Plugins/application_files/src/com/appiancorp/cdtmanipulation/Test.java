package com.appiancorp.cdtmanipulation;

import java.util.HashMap;

import com.appiancorp.suiteapi.type.TypedValue;
import com.appiancorp.type.AppianTypeLong;

public class Test {
	private static ExpandCdt expandCdt;
	private static TypedValue cdt;
	private static HashMap<TypedValue, TypedValue> map = new HashMap<TypedValue,TypedValue>();
	private static String expandField;
	private static TypedValue oneKey;
	private static TypedValue oneValue;
	private static double[] oneDouble;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		expandCdt = new ExpandCdt();
		cdt = new TypedValue();
		map = new HashMap<TypedValue,TypedValue>();
		expandField = "a";
		oneKey = new TypedValue(AppianTypeLong.STRING);
		oneValue = new TypedValue(AppianTypeLong.LIST_OF_DOUBLE);
		oneDouble = new double[3];
		oneDouble[1] = 1.1; oneDouble[2] = 2.2; oneDouble[3] = 3.3;
		oneKey.setValue("a");
		oneValue.setValue(oneDouble);
		map.put(oneKey, oneValue);
		
	}

}
