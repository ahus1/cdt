/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIVarListChildrenInfo;

/**
 * 
 *     -var-list-children NAME
 *
 *  Returns a list of the children of the specified variable object:
 *
 *     numchild=N,children={{name=NAME,
 *     numchild=N,type=TYPE},(repeats N times)}
 * 
 */
public class MIVarListChildren extends MICommand 
{
	public MIVarListChildren(String name) {
		super("-var-list-children", new String[]{name}); //$NON-NLS-1$
	}

	public MIVarListChildrenInfo getMIVarListChildrenInfo() throws MIException {
		return (MIVarListChildrenInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIVarListChildrenInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
