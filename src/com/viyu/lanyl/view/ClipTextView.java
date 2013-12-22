package com.viyu.lanyl.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * 
 * @author Viyu_Lu
 * 
 */
public class ClipTextView extends TextView implements View.OnClickListener {
	//private Context context = null;

	public ClipTextView(Context context) {
		super(context);
		//this.context = context;
		//setOnClickListener(this);
	}

	public ClipTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//this.context = context;
		//setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		//ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
	//	cm.setPrimaryClip(ClipData.newPlainText(null, getText()));
		//Toast.makeText(context, context.getString(R.string.toast_messagecopytoclip), Toast.LENGTH_SHORT).show();
	}
}
