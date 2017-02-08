package com.appiancorp.cdtmanipulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.appiancorp.suiteapi.type.Datatype;
import com.appiancorp.suiteapi.type.DatatypeProperties;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;
import com.appiancorp.suiteapi.type.exceptions.InvalidTypeException;
import com.appiancorp.type.AppianTypeLong;
import com.appiancorp.type.DataTypeProperties;

public class HelperFunctions {
	
	public static ArrayList<HashMap<TypedValue,TypedValue>> toMapList(TypeService ts, TypedValue typedValue) throws InvalidCdtException {

		HashMap<TypedValue, TypedValue> returnMap;
		ArrayList<HashMap<TypedValue,TypedValue>> returnList;
		try {
			return new ArrayList<HashMap<TypedValue,TypedValue>>(Arrays.asList((HashMap<TypedValue,TypedValue>[]) ts.cast(AppianTypeLong.LIST_OF_DICTIONARY, typedValue).getValue()));
		} catch(Exception e1) {
			try {
				returnMap = (HashMap<TypedValue,TypedValue>) ts.cast(AppianTypeLong.DICTIONARY, typedValue).getValue();
				returnList = new ArrayList<HashMap<TypedValue,TypedValue>>();
				returnList.add(returnMap);
				return (ArrayList<HashMap<TypedValue,TypedValue>>) returnList.clone();
			} catch(Exception e2){
				throw new InvalidCdtException("Invalid CDT");
			}
		}
		
	}
	
	public static TypedValue toDictionaryList(TypeService ts, ArrayList<HashMap<TypedValue,TypedValue>> toCast) throws InvalidTypeException {
		try {
			return new TypedValue(AppianTypeLong.LIST_OF_DICTIONARY, toCast.toArray(new HashMap[toCast.size()]));
		} catch (Exception e){
			throw new InvalidTypeException("Could not cast to list of dictionary");
		}
	}

	public static HashMap<TypedValue, TypedValue> toHashMap(TypedValue fieldsAndValues) throws InvalidDictionaryException {

		HashMap<TypedValue, TypedValue> returnMap = new HashMap<TypedValue, TypedValue>();
		try {
			returnMap.putAll((HashMap<TypedValue, TypedValue>)fieldsAndValues.getValue());
			return (HashMap<TypedValue, TypedValue>) returnMap.clone();
		} catch(Exception e) {
			throw new InvalidDictionaryException("Invalid dictionary");
		}
	}

	public static ArrayList<TypedValue> setToArrayList(Set<TypedValue> set) {
		return new ArrayList<TypedValue>(Arrays.asList(set.toArray(new TypedValue[set.size()])));
	}

	public static TypedValue getScalarType(TypeService ts, TypedValue typedValue) throws InvalidTypeException, InvalidDictionaryException {
		//
		// Attempts to get the scalar type of the given type. Will throw an error if passed a CDT or list of variant (2d array)
		//


		Long type = null;
		DatatypeProperties typeProperties;
		List<Datatype> referencedTypes;
		int i;
		Long typeLong;
		Long listType;

		typeLong = typedValue.getInstanceType();
		typeProperties = ts.getDatatypeProperties(typeLong);
		referencedTypes = ts.getReferencedTypes(typeLong);
		
		if (!typeProperties.hasFlag(DataTypeProperties.FLAG_SYSTEM) || typeLong == AppianTypeLong.LIST_OF_VARIANT) 
			throw new InvalidDictionaryException("Nested cdts and lists currently not supported.");
		
		if(!typeProperties.isListType())
			type = typeLong;
		else {
			// Loops through all the referenced types  and checks for the one that's list type equals the given type
			for(i=0 ; i<referencedTypes.size() ; i++) {
				listType = referencedTypes.get(i).getList();
				if(listType != null) {
					if(listType.equals(typeLong))
						type = referencedTypes.get(i).getTypeof();
				}
			}
		}
		
		return new TypedValue(type);
	}

	public static Object[] toObjectArr(Object obj) {

		Object[] objArr;
		try {
			objArr = (Object[]) obj;
		} catch(Exception e) {
			objArr = new Object[1];
			objArr[0] = obj;
		}
		return objArr;
	}
}
