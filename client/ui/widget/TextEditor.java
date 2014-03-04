package com.hexa.client.ui.widget;

import com.google.gwt.user.client.ui.TextBox;

public abstract class TextEditor extends GenericEditor<TextBox>
{
	protected abstract void onValidate( String newValue );

	public TextEditor( String text, boolean fShowCancel )
	{
		super( new TextBox(), fShowCancel );
		
		getWidget().getEditor().setFocus( true );

		getWidget().getEditor().setText( text );
	}

	@Override
	protected final void onValidate( TextBox widget )
	{
		onValidate( widget.getText() );
	}

	protected void displayMessage( String text )
	{
		getWidget().getEditor().setText( text );
		getWidget().getEditor().setEnabled( false );
	}
}
