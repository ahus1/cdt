/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.sourcelookup;

import org.eclipse.cdt.debug.internal.core.sourcelookup.CDirectorySourceLocation;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CProjectSourceLocation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Enter type comment.
 * 
 * @since Jul 14, 2003
 */
public class SourceLocationFactory
{
	public static IProjectSourceLocation createProjectSourceLocation( IProject project )
	{
		return new CProjectSourceLocation( project );
	}

	public static IProjectSourceLocation createProjectSourceLocation( IProject project, boolean generated )
	{
		return new CProjectSourceLocation( project, generated );
	}

	public static IDirectorySourceLocation createDirectorySourceLocation( IPath directory )
	{
		return new CDirectorySourceLocation( directory );
	}

	public static IDirectorySourceLocation createDirectorySourceLocation( IPath directory, IPath association )
	{
		return new CDirectorySourceLocation( directory, association );
	}
}
