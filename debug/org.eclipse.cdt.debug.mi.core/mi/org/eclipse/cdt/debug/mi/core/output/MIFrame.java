/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI Frame tuple parsing.
 */
public class MIFrame {

	int level;
	long addr;
	String func = ""; //$NON-NLS-1$
	String file = ""; //$NON-NLS-1$
	int line;
	MIArg[] args = new MIArg[0];

	public MIFrame(MITuple tuple) {
		parse(tuple);
	}

	public MIArg[] getArgs() {
		return args;
	}

	public String getFile() {
		return file;
	}

	public String getFunction() {
		return func;
	}

	public int getLine() {
		return line;
	}

	public long getAddress() {
		return addr;
	}

	public int getLevel() {
		return level;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("level=\"" + level + "\"");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append(",addr=\"" + Long.toHexString(addr) + "\"");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append(",func=\"" + func + "\"");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append(",file=\"" + file + "\"");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append(",line=\"").append(line).append('"'); //$NON-NLS-1$
		buffer.append(",args=["); //$NON-NLS-1$
		for (int i = 0; i < args.length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			buffer.append("{name=\"" + args[i].getName() + "\"");//$NON-NLS-1$//$NON-NLS-2$
			buffer.append(",value=\"" + args[i].getValue() + "\"}");//$NON-NLS-1$//$NON-NLS-2$
		}
		buffer.append(']');
		return buffer.toString();
	}

	void parse(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = ""; //$NON-NLS-1$
			if (value != null && value instanceof MIConst) {
				str = ((MIConst)value).getCString();
			}

			if (var.equals("level")) { //$NON-NLS-1$
				try {
					level = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("addr")) { //$NON-NLS-1$
				try {
					addr = Long.decode(str.trim()).longValue();
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("func")) { //$NON-NLS-1$
				func = null;
				if ( str != null ) {
					str = str.trim();
					if ( str.equals( "??" ) ) //$NON-NLS-1$
						func = ""; //$NON-NLS-1$
					else
					{
						// In some situations gdb returns the function names that include parameter types.
						// To make the presentation consistent truncate the parameters. PR 46592
						int end = str.indexOf( '(' );
						if ( end != -1 )
							func = str.substring( 0, end );
						else
							func = str;
					}
				}
			} else if (var.equals("file")) { //$NON-NLS-1$
				file = str;
			} else if (var.equals("line")) { //$NON-NLS-1$
				try {
					line = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("args")) { //$NON-NLS-1$
				if (value instanceof MIList) {
					args = MIArg.getMIArgs((MIList)value);
				}
			}
		}
	}
}
