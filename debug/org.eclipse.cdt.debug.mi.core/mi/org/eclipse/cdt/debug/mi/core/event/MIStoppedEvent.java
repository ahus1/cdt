/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.output.MIConst;
import org.eclipse.cdt.debug.mi.core.output.MIExecAsyncOutput;
import org.eclipse.cdt.debug.mi.core.output.MIFrame;
import org.eclipse.cdt.debug.mi.core.output.MIResult;
import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;
import org.eclipse.cdt.debug.mi.core.output.MITuple;
import org.eclipse.cdt.debug.mi.core.output.MIValue;



/**
 *  *stopped
 *
 */
public class MIStoppedEvent extends MIEvent {

	private int threadId;
	private MIFrame frame;
	private MIExecAsyncOutput exec;
	private MIResultRecord rr;

	public MIStoppedEvent(MIExecAsyncOutput record) {
		super(record.getToken());
		exec = record;
		parse();
	}

	public MIStoppedEvent(MIResultRecord record) {
		super(record.getToken());
		rr = record;
		parse();
	}
	
	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int id) {
		threadId = id;
	}

	public MIFrame getFrame() {
		return frame;
	}

	public void setFrame(MIFrame f) {
		frame = f;
	}

	public MIExecAsyncOutput getMIExecAsyncOutput() {
		return exec;
	}

	public MIResultRecord getMIResultRecord() {
		return rr;
	}
	
	void parse () {
		MIResult[] results = null;
		if (exec != null) {
			results = exec.getMIResults();
		} else if (rr != null) {
			results = rr.getMIResults();
		}
		if (results != null) {
			for (int i = 0; i < results.length; i++) {
				String var = results[i].getVariable();
				MIValue value = results[i].getMIValue();

				if (var.equals("thread-id")) { //$NON-NLS-1$
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getString();
						try {
							threadId = Integer.parseInt(str.trim());
						} catch (NumberFormatException e) {
						}
					}
				} else if (var.equals("frame")) { //$NON-NLS-1$
					if (value instanceof MITuple) {
						frame = new MIFrame((MITuple)value);
					}
				}
			}
		}
	}
}
