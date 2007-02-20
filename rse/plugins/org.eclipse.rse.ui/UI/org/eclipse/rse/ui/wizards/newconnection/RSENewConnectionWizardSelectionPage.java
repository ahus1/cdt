/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation.
 *******************************************************************************/

package org.eclipse.rse.ui.wizards.newconnection;

import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.ui.RSESystemTypeAdapter;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.wizards.registries.RSEWizardSelectionTreeContentProvider;
import org.eclipse.rse.ui.wizards.registries.RSEWizardSelectionTreeLabelProvider;
import org.eclipse.rse.ui.wizards.registries.RSEWizardSelectionTreePatternFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * The New Connection Wizard main page that allows selection of system type.
 */
public class RSENewConnectionWizardSelectionPage extends WizardPage {
	private final String helpId = RSEUIPlugin.HELPPREFIX + "wncc0000"; //$NON-NLS-1$;
	
	private IRSESystemType[] restrictedSystemTypes;

	private FilteredTree filteredTree;
	private PatternFilter filteredTreeFilter;
	private ViewerFilter filteredTreeWizardStateFilter;
	private RSENewConnectionWizardSelectionTreeDataManager filteredTreeDataManager;
	
	/**
	 * Internal class. The wizard state filter is responsible to filter
	 * out any not enabled wizard from the tree.
	 */
	private class NewConnectionWizardStateFilter extends ViewerFilter {
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			Object[] children = ((ITreeContentProvider) ((AbstractTreeViewer) viewer).getContentProvider()).getChildren(element);
			if (children.length > 0) {
				return filter(viewer, element, children).length > 0;
			}
			
			if (element instanceof RSENewConnectionWizardSelectionTreeElement) {
				// the system type must be enabled, otherwise it is filtered out
				IRSESystemType systemType = ((RSENewConnectionWizardSelectionTreeElement)element).getSystemType();
				if (systemType == null) return false;
				
				// if the page is restricted to a set of system types, check on them first
				IRSESystemType[] restricted = getRestrictToSystemTypes();
				if (restricted != null && restricted.length > 0) {
					if (!Arrays.asList(restricted).contains(systemType)) return false;
				}
				
				RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(systemType.getAdapter(IRSESystemType.class));
				if (adapter != null) {
					return adapter.isEnabled(systemType);
				}
			}
			
			return true;
		}
	}
	
	/**
	 * Internal class. The wizard viewer comparator is responsible for
	 * the sorting in the tree. Current implementation is not prioritizing
	 * categories.
	 */
	private class NewConnectionWizardViewerComparator extends ViewerComparator {
	
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerComparator#isSorterProperty(java.lang.Object, java.lang.String)
		 */
		public boolean isSorterProperty(Object element, String property) {
			// The comparator is affected if the label of the elements should change.
      return property.equals(IBasicPropertyConstants.P_TEXT);
		}
	}
	
 	/**
	 * Constructor.
	 */
	public RSENewConnectionWizardSelectionPage() {
		super("RSENewConnectionWizardSelectionPage"); //$NON-NLS-1$
		setTitle(getDefaultTitle());
		setDescription(getDefaultDescription());
	}

	/**
	 * Returns the default page title.
	 * 
	 * @return The default page title. Must be never <code>null</code>.
	 */
	protected String getDefaultTitle() {
		return SystemResources.RESID_NEWCONN_MAIN_PAGE_TITLE;
	}
	
	/**
	 * Returns the default page description.
	 * 
	 * @return The default page description. Must be never <code>null</code>.
	 */
	protected String getDefaultDescription() {
		return SystemResources.RESID_NEWCONN_MAIN_PAGE_DESCRIPTION;
	}

	/**
	 * Restrict the selectable wizards to the given set of system types.
	 * 
	 * @param systemTypes The list of the system types to restrict the page to or <code>null</code>.
	 */
	public void restrictToSystemTypes(IRSESystemType[] systemTypes) {
		this.restrictedSystemTypes = systemTypes;
	}
	
	/**
	 * Returns the list of system types the page is restricted to.
	 * 
	 * @return The list of system types the page is restricted to or <code>null</code>.
	 */
	protected IRSESystemType[] getRestrictToSystemTypes() {
		return restrictedSystemTypes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
    Label label = new Label(composite, SWT.NONE);
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    label.setText(SystemResources.RESID_CONNECTION_SYSTEMTYPE_LABEL + ":"); //$NON-NLS-1$    
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    filteredTreeFilter = new RSEWizardSelectionTreePatternFilter();
    filteredTree = new FilteredTree(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, filteredTreeFilter);
		filteredTree.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 275; layoutData.widthHint = 450;
		filteredTree.setLayoutData(layoutData);
		
    final TreeViewer treeViewer = filteredTree.getViewer();
    treeViewer.setContentProvider(new RSEWizardSelectionTreeContentProvider());
    // Explicitly allow the tree items to get decorated!!!
		treeViewer.setLabelProvider(new DecoratingLabelProvider(new RSEWizardSelectionTreeLabelProvider(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		treeViewer.setComparator(new NewConnectionWizardViewerComparator());
		treeViewer.setAutoExpandLevel(2);

		filteredTreeWizardStateFilter = new NewConnectionWizardStateFilter();
		treeViewer.addFilter(filteredTreeWizardStateFilter);

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				onSelectionChanged();
			}
		});
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (canFlipToNextPage()) getWizard().getContainer().showPage(getNextPage());
			}
		});
		
		filteredTreeDataManager = new RSENewConnectionWizardSelectionTreeDataManager();
		treeViewer.setInput(filteredTreeDataManager);
		
		// apply the standard dialog font
		Dialog.applyDialogFont(composite);
		
		setControl(composite);
		
		// Initialize the selection in the tree
		if (getWizard() instanceof ISelectionProvider) {
			ISelectionProvider selectionProvider = (ISelectionProvider)getWizard();
			if (selectionProvider.getSelection() instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection)selectionProvider.getSelection();
				if (selection.getFirstElement() instanceof IRSESystemType) {
					IRSESystemType systemType = (IRSESystemType)selection.getFirstElement();
					RSENewConnectionWizardSelectionTreeElement treeElement = filteredTreeDataManager.getTreeElementForSystemType(systemType);
					if (treeElement != null) treeViewer.setSelection(new StructuredSelection(treeElement), true);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#performHelp()
	 */
	public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem().displayHelp(helpId);
	}

	/**
	 * Called from the selection listener to propage the current
	 * system type selection to the underlaying wizard.
	 */
	protected void onSelectionChanged() {
		IWizard wizard = getWizard();
		if (wizard instanceof ISelectionProvider && filteredTree.getViewer().getSelection() instanceof IStructuredSelection) {
			ISelectionProvider selectionProvider = (ISelectionProvider)wizard;
			IStructuredSelection filteredTreeSelection = (IStructuredSelection)filteredTree.getViewer().getSelection();
			if (filteredTreeSelection.getFirstElement() instanceof RSENewConnectionWizardSelectionTreeElement) {
				RSENewConnectionWizardSelectionTreeElement element = (RSENewConnectionWizardSelectionTreeElement)filteredTreeSelection.getFirstElement();
				selectionProvider.setSelection(new StructuredSelection(element.getSystemType()));
				if (element.getDescription() != null) {
					setDescription(element.getDescription());
				} else {
					if (!getDefaultDescription().equals(getDescription())) setDescription(getDefaultDescription());
				}
			} else {
				selectionProvider.setSelection(null);
			}
		}
		
		// Update the wizard container UI elements
		IWizardContainer container = getContainer();
		if (container != null && container.getCurrentPage() != null) {
			container.updateWindowTitle();
			container.updateTitleBar();
			container.updateButtons();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getDialogSettings()
	 */
	protected IDialogSettings getDialogSettings() {
		// If the wizard is set and returns dialog settings, we re-use them here
		IDialogSettings settings = super.getDialogSettings();
		// If the dialog settings could not set from the wizard, fallback to the plugins
		// dialog settings store.
		if (settings == null) settings = RSEUIPlugin.getDefault().getDialogSettings();
		String sectionName = this.getClass().getName();
		if (settings.getSection(sectionName) == null) settings.addNewSection(sectionName);
		settings = settings.getSection(sectionName);

		return settings;
	}
}