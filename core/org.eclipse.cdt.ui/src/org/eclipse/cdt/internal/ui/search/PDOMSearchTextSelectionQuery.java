/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextSelection;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchTextSelectionQuery extends PDOMSearchQuery {

	private ITranslationUnit tu;
	private ITextSelection selection;
	
	public PDOMSearchTextSelectionQuery(ICElement[] scope, ITranslationUnit tu, ITextSelection selection, int flags) {
		super(scope, flags);
		this.tu = tu;
		this.selection = selection;
	}

	protected IStatus runWithIndex(IIndex index, IProgressMonitor monitor) {
		try {
			IASTTranslationUnit ast= tu.getAST(index, ITranslationUnit.AST_SKIP_ALL_HEADERS);
			ILanguage language = tu.getLanguage();
			IASTName[] names = language.getSelectedNames(ast, selection.getOffset(), selection.getLength());
			
			for (int i = 0; i < names.length; ++i) {
				IBinding binding = names[i].resolveBinding();
				if (binding != null)
					createMatches(index, binding);
			}
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	public String getLabel() {
		return super.getLabel() + " " + selection.getText(); //$NON-NLS-1$
	}

}
