/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;

public class OpenDeclarationsAction extends SelectionParseAction {
	public static final IASTName[] BLANK_NAME_ARRAY = new IASTName[0];
	ITextSelection selNode;

	/**
	 * Creates a new action with the given editor
	 */
	public OpenDeclarationsAction(CEditor editor) {
		super( editor );
		setText(CEditorMessages.getString("OpenDeclarations.label")); //$NON-NLS-1$
		setToolTipText(CEditorMessages.getString("OpenDeclarations.tooltip")); //$NON-NLS-1$
		setDescription(CEditorMessages.getString("OpenDeclarations.description")); //$NON-NLS-1$
	}

	private class Runner extends Job {
		Runner() {
			super(CEditorMessages.getString("OpenDeclarations.dialog.title")); //$NON-NLS-1$
		}

		protected IStatus run(IProgressMonitor monitor) {
			try {
				int selectionStart = selNode.getOffset();
				int selectionLength = selNode.getLength();
					
				IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
				if (workingCopy == null)
					return Status.CANCEL_STATUS;

				int style = 0;
//				IPDOM pdom = CCorePlugin.getPDOMManager().getPDOM(workingCopy.getCProject());
//				if (!pdom.isEmpty())
//					style |= ITranslationUnit.AST_SKIP_ALL_HEADERS;
				IASTTranslationUnit ast = workingCopy.getAST(null, style);
				IASTName[] selectedNames = workingCopy.getLanguage().getSelectedNames(ast, selectionStart, selectionLength);
					
				if (selectedNames.length > 0 && selectedNames[0] != null) { // just right, only one name selected
					IASTName searchName = selectedNames[0];
		
					IBinding binding = searchName.resolveBinding();
					if (binding != null && !(binding instanceof IProblemBinding)) {
						final IName[] declNames = ast.getDeclarations(binding);
						if (declNames.length > 0) {
							runInUIThread(new Runnable() {
								public void run() {
									try {
										open(declNames[0]);
									} catch (CoreException e) {
										CUIPlugin.getDefault().log(e);
									}
								}
							});
						} 
						// mstodo revisit
//						else if (binding instanceof IIndexBinding) {
//							IIndexBinding pdomBinding = (IIndexBinding)binding;
//							IName name = pdomBinding.getFirstDefinition();
//							if (name == null)
//								name = pdomBinding.getFirstDeclaration();
//				    		// no source location - TODO spit out an error in the status bar
//							if (name != null) {
//						    	IASTFileLocation fileloc = name.getFileLocation();
//						    	if (fileloc != null) {
//						    		final IPath path = new Path(fileloc.getFileName());
//						    		final int offset = fileloc.getNodeOffset();
//						    		final int length = fileloc.getNodeLength();
//						    		Display.getDefault().asyncExec(new Runnable() {
//						    			public void run() {
//						    				try {
//						    					open(path, offset, length);
//						    				} catch (CoreException e) {
//						    					CUIPlugin.getDefault().log(e);
//						    				}
//						    			}
//						    		});
//						    	}
//							}
//						}
					}
				}
					
				return Status.OK_STATUS;
			} catch (CoreException e) {
				return e.getStatus();
			}
		}
	}

	public void run() {
		selNode = getSelectedStringFromEditor();
		if (selNode != null) {
			new Runner().schedule();
		}
	}

	private void runInUIThread(Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		}
		else {
			Display.getDefault().asyncExec(runnable);
		}
	}

	/**
	 * For the purpose of regression testing.
	 * @since 4.0
	 */
	public void runSync() {
		selNode = getSelectedStringFromEditor();
		if (selNode != null) {
			new Runner().run(new NullProgressMonitor());
		}
	}
}

