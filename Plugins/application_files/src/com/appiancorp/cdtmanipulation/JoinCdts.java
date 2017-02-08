package com.appiancorp.cdtmanipulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

import com.appiancorp.services.ServiceContext;
import com.appiancorp.suiteapi.common.ServiceLocator;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;
import com.appiancorp.type.AppianTypeLong;

@AppianCdtManipulationCategory
public class JoinCdts {

	private final Logger LOG = Logger.getLogger(JoinCdts.class);
	private final List<String> joinTypes = new ArrayList<String>(Arrays.asList("LEFT","RIGHT","INNER","OUTER"));

	public void main(String[] args){
		
	}
	
	@Function
	public TypedValue joinCdts (ServiceContext sc, 
			@Parameter String joinType,
			@Parameter TypedValue leftCdt,
			@Parameter String[] selectFromLeft,
			@Parameter TypedValue rightCdt,
			@Parameter String[] selectFromRight,
			@Parameter String onLeft, 
			@Parameter String equalsRight,
			@Parameter String leftAlias,
			@Parameter String rightAlias)  throws InvalidFieldException, InvalidJoinTypeException, NullPointerException, InvalidCdtException{

		int i;
		int row;
		int col;
		int foundIndex;
		int previousIndex;
		boolean isInner;
		boolean isLeft;
		int joinTypeIndex;
		TypedValue oneLookup;
		ArrayList<Integer> foundIndices;
		ArrayList<TypedValue> lookUpVals;
		ArrayList<TypedValue> lookInVals;
		TypedValue onField = null;
		TypedValue toField = null;
    String joinOnAlias = "";
    String joinToAlias = "";
		List<HashMap<TypedValue,TypedValue>> joinOnMap = null;
		List<HashMap<TypedValue,TypedValue>> joinToMap = null;
		ArrayList<HashMap<TypedValue, TypedValue>> returnMap;
		ArrayList<TypedValue> joinOnSelectFields;
		ArrayList<TypedValue> joinToSelectFields;
		ArrayList<TypedValue> joinOnAliasFields;
		ArrayList<TypedValue> joinToAliasFields;
		TypeService ts;
		HashMap<TypedValue, TypedValue> oneRow;
		HashMap<TypedValue, TypedValue> onRow;
		HashMap<TypedValue, TypedValue> toRow;

		foundIndices = new ArrayList<Integer>();
		lookUpVals = new ArrayList<TypedValue>();
		lookInVals = new ArrayList<TypedValue>();
		returnMap = new ArrayList<HashMap<TypedValue, TypedValue>>();
		joinOnSelectFields = new ArrayList<TypedValue>();
		joinToSelectFields = new ArrayList<TypedValue>();
		joinOnAliasFields = new ArrayList<TypedValue>();
		joinToAliasFields = new ArrayList<TypedValue>();
			
		// Get type service
		ts = ServiceLocator.getTypeService(sc);
		
		// Check join type
		joinTypeIndex =  joinTypes.indexOf(joinType);
		if(joinTypeIndex == -1 || joinTypeIndex == 4)
			throw new InvalidJoinTypeException("Given join type not currently supported. Acceptable joinTypes are \"LEFT\", \"RIGHT\", or \"INNER\".");
		
		if(joinTypeIndex == 0){
			isLeft = true;
			isInner = false;
		} else if(joinTypeIndex == 1) {
			isLeft = false;
			isInner = false;
		} else {
			isLeft = true;
			isInner = true;
		}
				
		// Set "on" and "to" variables based on "joinType" or throws exception
		if(isLeft)
			setOnAndToVariables(ts, leftCdt,rightCdt,onLeft,equalsRight,selectFromLeft, selectFromRight,leftAlias,rightAlias, joinOnMap, joinToMap, onField, toField, joinOnAlias, joinToAlias, joinOnSelectFields, joinToSelectFields, joinOnAliasFields, joinToAliasFields);
		else 
			setOnAndToVariables(ts, rightCdt,leftCdt,equalsRight,onLeft,selectFromRight, selectFromLeft,rightAlias,leftAlias, joinOnMap, joinToMap, onField, toField, joinOnAlias, joinToAlias, joinOnSelectFields, joinToSelectFields, joinOnAliasFields, joinToAliasFields);
		
		// Checks to see that the CDTs are not null
		if(leftCdt.getValue().equals(null) || rightCdt.getValue().equals(null)){
			throw new NullPointerException();
		}
		
		// Checks validity of the onFields and toFields   
		onRow = joinOnMap.get(0);
		toRow = joinToMap.get(0);
		if(!onRow.keySet().contains(onField)) 
			throw new InvalidFieldException("Invalid \"onLeft\" value", "Invalid_Left_Field");
		if(!toRow.keySet().contains(toField))
			throw new InvalidFieldException("Invalid \"equalsRight\" Value", "Invalid_Right_Field");
		if(!(onRow.keySet().containsAll(joinOnSelectFields) && toRow.keySet().containsAll(joinToSelectFields)))
			throw new InvalidFieldException("Invalid Field Selected", "Invalid_Selection");
		
		// Sets ArrayLists containing values to lookup and values to be looked-up
		removeNullIds(onField, joinOnMap, lookUpVals);
		removeNullIds(toField, joinToMap, lookInVals);
			
		// Loops through each row of "on" cdt
		previousIndex = 0;
		for(i=0 ; i<joinOnMap.size() ; i++){
			
			// Gets id for row i of joinOnMap 
			oneLookup = lookUpVals.get(i);
			
			// Finds id in joinToMap
			foundIndex = lookInVals.indexOf(oneLookup);
			
			// Removes row if id isn't found and it's an inner join
			if (foundIndex == -1 && isInner) {
				joinOnMap.remove(i);
				lookUpVals.remove(i);
				i--;
			} else {
				
				// Otherwise, adds the index of the found value to foundIndices
				foundIndices.add(foundIndex);
				
				// Continues to look for matches
				while(foundIndex > -1){
					previousIndex = previousIndex + foundIndex + 1;
					
					// If there are more rows to search
					if(previousIndex < joinToMap.size()){
						foundIndex = lookInVals.subList(previousIndex, joinToMap.size()-1).indexOf(oneLookup);
						
						// If something's found, copies the row and updates arrays appropriately  
						if(foundIndex > -1){
							joinOnMap.add(i, joinOnMap.get(i));
							i++;
							foundIndices.add(foundIndex + previousIndex);
							lookUpVals.add(i, oneLookup);
						}
						
					}else{
						foundIndex = -1;
					}
				}
			}
			previousIndex = 0;
		}
		
		// Creates "List of Dictionary" (array of hashmaps) to return
		for(row=0 ; row<joinOnMap.size() ; row++){
			oneRow = new HashMap<TypedValue, TypedValue>();
			
			// Gets joinOnFields for one row
			for(col=0 ; col<joinOnSelectFields.size() ; col++){
				oneRow.put(joinOnAliasFields.get(col), joinOnMap.get(row).get(joinOnSelectFields.get(col)));
			}
			
			// Gets joinToFields for one row
			for(col=0 ; col<joinToSelectFields.size() ; col++){
				if(foundIndices.get(row) > -1)
					oneRow.put(joinToAliasFields.get(col), joinToMap.get(foundIndices.get(row)).get(joinToSelectFields.get(col)));
				else
					oneRow.put(joinToAliasFields.get(col),null);
			}
			
			returnMap.add(oneRow);
		}
		
		return new TypedValue(AppianTypeLong.LIST_OF_DICTIONARY, returnMap.toArray(new HashMap[returnMap.size()]));
		
	}
	
	// Sets "On" and "To" variables based on the users choice of joinType, leftCdt, and rightCdt
	public void setOnAndToVariables(
	    TypeService ts,
      TypedValue onCdt,
      TypedValue toCdt,
      String joinOn,
      String equals,
      String[] onSelectFields,
      String[] toSelectFields,
      String onAlias,
      String toAlias,
      List<HashMap<TypedValue,TypedValue>> joinOnMap,
      List<HashMap<TypedValue,TypedValue>> joinToMap,
      TypedValue onField,
      TypedValue toField,
      String joinOnAlias,
      String joinToAlias,
      ArrayList<TypedValue> joinOnSelectFields,
      ArrayList<TypedValue> joinToSelectFields,
      ArrayList<TypedValue> joinOnAliasFields,
      ArrayList<TypedValue> joinToAliasFields) throws InvalidCdtException{

		try {
			joinOnMap = new ArrayList<HashMap<TypedValue,TypedValue>>(Arrays.asList((HashMap<TypedValue,TypedValue>[]) ts.cast(AppianTypeLong.LIST_OF_DICTIONARY, onCdt).getValue()));
		} catch(Exception e) {
			throw new InvalidCdtException("Join On Cdt Invalid.");
		}
		try {
			joinToMap = new ArrayList<HashMap<TypedValue,TypedValue>>(Arrays.asList((HashMap<TypedValue,TypedValue>[]) ts.cast(AppianTypeLong.LIST_OF_DICTIONARY, toCdt).getValue()));
		} catch(Exception e) {
			throw new InvalidCdtException("Join To Cdt Invalid.");
		}
		
		onField = new TypedValue(AppianTypeLong.STRING, joinOn);
		toField = new TypedValue(AppianTypeLong.STRING, equals);
		joinOnAlias = onAlias;
		joinToAlias = toAlias;
		int i;
		for(i=0 ; i<onSelectFields.length ; i++){
			joinOnSelectFields.add(new TypedValue(AppianTypeLong.STRING, onSelectFields[i]));
			joinOnAliasFields.add(new TypedValue(AppianTypeLong.STRING,joinOnAlias + "." + onSelectFields[i]));
		}
		
		for(i=0 ; i<toSelectFields.length ; i++){
			joinToSelectFields.add(new TypedValue(AppianTypeLong.STRING, toSelectFields[i]));
			joinToAliasFields.add(new TypedValue(AppianTypeLong.STRING,joinToAlias + "." + toSelectFields[i]));
		}
		
	}	
	
	// Removes any rows with null ids
	public void removeNullIds(
			TypedValue field, List<HashMap<TypedValue,TypedValue>> map,
			ArrayList<TypedValue> vals) {

	  TypedValue oneLookup;
    int i;
		for(i=0 ; i<map.size() ; i++){
			oneLookup = map.get(i).get(field);
			if (oneLookup.getValue() == null || oneLookup.getValue().toString().length() == 0){
				map.remove(i);
				i--;
			} else {
				vals.add(oneLookup);
			}
		}
		
	}
}
