package com.hexa.client.tools;

import com.hexa.client.tools.ColumnsSet.IColumnMng;
import com.hexa.client.tools.ColumnsSet.IEditor;
import com.hexa.client.ui.treetable.TreeTable.Row;

public class EmptyColumn<T> implements IColumnMng<T>
{
	@Override
	public void fillCell( int ordinal, Row row, T record )
	{
	}

	@Override
	public IEditor editCell( T record )
	{
		return null;
	}

	@Override
	public String getTitle()
	{
		return "";
	}
}
