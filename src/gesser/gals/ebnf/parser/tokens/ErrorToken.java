/*
 * Copyright 2004 Carlos Eduardo Gesser
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, you can get it at http://www.gnu.org/licenses/gpl.txt
 */

package gesser.gals.ebnf.parser.tokens;

import javax.swing.text.SimpleAttributeSet;

import gesser.gals.analyser.AnalysisError;
import gesser.gals.editor.Style;

/**
 * @author Carlos Gesser
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ErrorToken extends Token
{
	private AnalysisError error;
	private String lexeme;
	
	public ErrorToken(int position, String lexeme, AnalysisError error)
	{
		super(null, position);
		this.lexeme = lexeme;
		this.error = error;
	}

	public String getLexeme()
	{
		return lexeme;
	}
	
	public AnalysisError getError()
	{
		return error;
	}
	
	public String toString()
	{
		return super.toString() + " >>> " + error.getMessage();
	}
	
	public SimpleAttributeSet getStyle() 
	{
		return Style.ERROR.getStyle();
	}
}
