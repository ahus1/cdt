/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;



/**
 * GDB/MI register response parsing.
 */
public class MIRegisterValue {
	int number;
	String value;

	public MIRegisterValue(int n, String v) {
		number = n;
		value = v;
	}

	public int getNumber() {
		return number;
	}

	public String getValue() {
		return value;
	} 

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("number=\"").append(number).append('"');
		buffer.append(',').append("value=\"" + value + "\"");
		return buffer.toString();
	}

	/**
	 * Parsing a MIList of the form:
	 * [{number="1",value="0xffff"},{number="xxx",value="yyy"},..]
	 */
	public static MIRegisterValue[] getMIRegisterValues(MIList miList) {
		List aList = new ArrayList();
		MIValue[] values = miList.getMIValues();
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof MITuple) {
				MIRegisterValue reg = getMIRegisterValue((MITuple)values[i]);
				if (reg != null) {
					aList.add(reg);
				}
			}
		}
		return ((MIRegisterValue[])aList.toArray(new MIRegisterValue[aList.size()]));
	}

	/**
	 * Parsing a MITuple of the form:
	 * {number="xxx",value="yyy"}
	 */
	public static MIRegisterValue getMIRegisterValue(MITuple tuple) {
		MIResult[] args = tuple.getMIResults();
		MIRegisterValue arg = null;
		if (args.length == 2) {
			// Name
			String aName = ""; //$NON-NLS-1$
			MIValue value = args[0].getMIValue();
			if (value != null && value instanceof MIConst) {
				aName = ((MIConst)value).getCString();
			} else {
				aName = ""; //$NON-NLS-1$
			}

			// Value
			String aValue = ""; //$NON-NLS-1$
			value = args[1].getMIValue();
			if (value != null && value instanceof MIConst) {
				aValue = ((MIConst)value).getCString();
			} else {
				aValue = ""; //$NON-NLS-1$
			}

			try {
				int reg = Integer.parseInt(aName.trim());
				arg = new MIRegisterValue(reg, aValue.trim());
			} catch (NumberFormatException e) {
			}
		}
		return arg;
	}
}
