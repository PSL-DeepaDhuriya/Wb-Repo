package com.appiancorp.cdtmanipulation;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.appiancorp.services.ServiceContext;
import com.appiancorp.suiteapi.common.ServiceLocator;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;
import com.appiancorp.suiteapi.type.exceptions.InvalidTypeException;
import com.appiancorp.type.AppianTypeLong;

@AppianCdtManipulationCategory
public class ExpandCdt {

	private final Logger LOG = Logger.getLogger(ExpandCdt.class);

	@Function
	public TypedValue expandCdt(ServiceContext sc, @Parameter TypedValue cdt,
			@Parameter String expandField) 
					throws InvalidFieldException, InvalidCdtException, InvalidNestedFieldException, InvalidTypeException, InvalidDictionaryException {
		TypeService ts;
		ArrayList<HashMap<TypedValue, TypedValue>> mapList;
		ArrayList<HashMap<TypedValue, TypedValue>> returnList;
		HashMap<TypedValue, TypedValue> oneRow;
		HashMap<TypedValue, TypedValue> oneNestedCdt;
		Object[] oneNestedList;
		TypedValue fieldKey;
		TypedValue oneKey;
		TypedValue oneValue;
		String typename;
		int numFields;
		int i;
		int j;
		// Get type service
		ts = ServiceLocator.getTypeService(sc);
		
		// Cast cdt as Array of Maps
		mapList = HelperFunctions.toMapList(ts, cdt);
		
		// Check to see expansion type
		oneRow = mapList.get(0);
		typename = ts.getType(oneRow.get(new TypedValue(AppianTypeLong.STRING, expandField)).getInstanceType()).getName();
		fieldKey = new TypedValue(AppianTypeLong.STRING, expandField);
		returnList = new ArrayList<HashMap<TypedValue, TypedValue>>();
		
		// Expands CDT
		oneRow = mapList.get(0);
		if(typename.toLowerCase().contains("list"))
			expandRows(mapList, ts, oneRow, fieldKey, returnList);
		else
			expandColumns(mapList, fieldKey, oneRow, returnList);
		
		return new TypedValue(AppianTypeLong.LIST_OF_DICTIONARY, returnList.toArray(new HashMap[returnList.size()]));
	}

	private void expandColumns(
      ArrayList<HashMap<TypedValue,TypedValue>> cdt,
      TypedValue fieldKey,
      HashMap<TypedValue,TypedValue> oneRow,
      ArrayList<HashMap<TypedValue,TypedValue>> returnList) throws InvalidNestedFieldException {
		//
		// Creates a new column for every column in the nested CDT
		//
    HashMap<TypedValue,TypedValue> oneNestedCdt = null;
		try {
			oneNestedCdt = (HashMap<TypedValue, TypedValue>) oneRow.get(fieldKey).getValue();
		} catch (Exception e) {
			throw new InvalidNestedFieldException("Could not cast nested field");
		}

		ArrayList<TypedValue> nestedFieldsList = HelperFunctions.setToArrayList(oneNestedCdt.keySet());
		int numFields = nestedFieldsList.size();
		int i;
		for(i=0 ; i<cdt.size() ; i++) {
			oneRow = cdt.get(i);
			oneNestedCdt = (HashMap<TypedValue, TypedValue>) oneRow.get(fieldKey).getValue();

			int j;
			TypedValue oneKey;
			for(j=0 ; j<numFields ; j++) {
				oneKey = nestedFieldsList.get(j);
				oneRow.put(new TypedValue(oneKey.getInstanceType(),"nest." + oneKey.getValue()),oneNestedCdt.get(oneKey));
			}
			
			oneRow.remove(fieldKey);
			returnList.add(oneRow);
		}
	}

	private void expandRows(
      ArrayList<HashMap<TypedValue,TypedValue>> cdt,
      TypeService ts,
      HashMap<TypedValue,TypedValue> oneRow, TypedValue fieldKey, ArrayList<HashMap<TypedValue, TypedValue>> returnList) throws InvalidTypeException, InvalidDictionaryException {
		//
		// Creates a new row for every value in a nested list
		//
		TypedValue oneValue = HelperFunctions.getScalarType(ts, oneRow.get(fieldKey));
    int i;
		for(i=0 ; i<cdt.size() ; i++) {
			oneRow = cdt.get(i);
			Object[] oneNestedList = HelperFunctions.toObjectArr(oneRow.get(fieldKey).getValue());

      int j;
			for(j=0 ; j<oneNestedList.length ; j++) {
				oneRow = (HashMap<TypedValue,TypedValue>) oneRow.clone();
				oneValue = new TypedValue(oneValue.getInstanceType(),oneNestedList[j]);
				oneRow.put(fieldKey,oneValue);
				returnList.add(oneRow);
			}
		}			
	}

}
