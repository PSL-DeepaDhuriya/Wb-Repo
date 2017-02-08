package com.appiancorp.cdtmanipulation;

import static org.exolab.castor.jdo.engine.JDBCSyntax.Parameter;

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

@AppianCdtManipulationCategory
public class UpdateCdt {

	private static final Logger LOG = Logger.getLogger(UpdateCdt.class);

	@Function
	public TypedValue updateCdt(ServiceContext sc, 
			@Parameter TypedValue cdt,
			@Parameter TypedValue fieldsAndValues) throws InvalidCdtException, InvalidDictionaryException, InvalidTypeException {
		//
		// Updates fields in a given CDT or Dictionary
		//
    TypeService ts;
    ArrayList<HashMap<TypedValue, TypedValue>> mapList;
    HashMap<TypedValue, TypedValue> dictionary;
    ArrayList<TypedValue> fieldsList;
    TypedValue oneKey;
    TypedValue oneValue;
    int i;
    int j;
    int nCol;
    int nRow;
    Object[] oneCol;

    ts = ServiceLocator.getTypeService(sc);
    mapList = HelperFunctions.toMapList(ts, cdt);
    dictionary = HelperFunctions.toHashMap(fieldsAndValues);
    fieldsList = HelperFunctions.setToArrayList(dictionary.keySet());

    nRow = mapList.size();
    nCol = fieldsList.size();

    // Loops throw every column, setting the new values. Throws an error if the given replacement list length doesn't
    // equal original list length
    for(i=0 ; i<nCol ; i++) {
      oneKey = fieldsList.get(i);
      oneCol = HelperFunctions.toObjectArr(dictionary.get(oneKey).getValue());

      if(oneCol.length != nRow)
        throw new InvalidDictionaryException("Length of input arrays must equal length of CDT/Dictionary array");

      oneValue = HelperFunctions.getScalarType(ts, dictionary.get(oneKey));

      for(j=0 ; j<nRow ; j++) {
        oneValue.setValue(oneCol[j]);
        mapList.get(j).put(oneKey, (TypedValue)oneValue.clone());
      }
    }

    TypedValue returnVal = HelperFunctions.toDictionaryList(ts, mapList);
    return returnVal;

	}


}
