package fr.lteconsulting.hexa.client.ui.search.comparators;

import java.util.ArrayList;

import fr.lteconsulting.hexa.client.common.Pair;

public class EqualOrNotComparators
{
	private static ArrayList<Pair<String, String>> __ = null;

	public static ArrayList<Pair<String, String>> get()
	{
		if( __ == null )
		{
			__ = new ArrayList<Pair<String, String>>();
			__.add( new Pair<String, String>( "=", "eq" ) );
			__.add( new Pair<String, String>( "<>", "ne" ) );
		}

		return __;
	}
}