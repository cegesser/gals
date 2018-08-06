package gesser.gals;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FileFilters extends FileFilter
{
	private String description;
	private String extension;
	
	
	private FileFilters(String extension, String description)
	{
		this.extension = "."+extension;
		this.description = description;
	}
	
	public boolean accept(File f)
	{
		if (f.isDirectory())
				return true;
				
		String name = f.getName();
		int length = name.length();
		return (length > extension.length()) && name.substring(length-extension.length()).equals(extension);
	}

	public String getDescription()
	{
		return description;
	}
	
	public static final FileFilters GALS_FILTER = new FileFilters("gals", "Especificação Sintática (*.gals)");
	public static final FileFilters BNF_FILTER = new FileFilters("bnf", "Arquivo GAS (*.bnf)");
	public static final FileFilters DIRECTORY_FILTER = new FileFilters("", "Pastas")
	{
		public boolean accept(File f)
		{
			return f.isDirectory();
		}
	};
	
	public static final FileFilters HTML_FILTER = new FileFilters("", "Arquivos html(*.html, *.htm)")
	{
		public boolean accept(File f)
		{
			if (f.isDirectory())
				return true;
				
			String name = f.getName();
			int length = name.length();
			return 
				(length > 5 && name.substring(length-5).equals(".html") ) |
				(length > 4 && name.substring(length-4).equals(".htm") );
		}
	};
}
