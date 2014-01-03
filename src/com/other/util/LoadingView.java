package com.other.util;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadingView extends LinearLayout {

	public LoadingView(Context context) {
		super(context);
	}

	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private String TEXTCONTENT = "加载中...";
	public void setWaitingText(String text){
	    TEXTCONTENT = text;
	}

	public void loadView() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT);
        //lp.gravity = Gravity.CENTER;
        this.setLayoutParams(lp);
        this.setGravity(Gravity.CENTER);
        this.setVisibility(View.GONE);
        this.setBackgroundColor(Color.parseColor("#EEEEEE"));
	    this.setOrientation(LinearLayout.HORIZONTAL);

		ProgressBar progressBar = new ProgressBar(getContext());
		LinearLayout.LayoutParams animParams = new LinearLayout.LayoutParams(
		        LinearLayout.LayoutParams.WRAP_CONTENT,
		        LinearLayout.LayoutParams.WRAP_CONTENT);
		
		animParams.gravity = Gravity.CENTER;
		this.addView(progressBar, animParams);

		TextView textView = new TextView(getContext());
		textView.setText(TEXTCONTENT);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		textView.setTextColor(Color.parseColor("#cc000000"));
		LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
		        LinearLayout.LayoutParams.WRAP_CONTENT,
		        LinearLayout.LayoutParams.WRAP_CONTENT);
		textParams.gravity = Gravity.CENTER;
		this.addView(textView, textParams);
	}

	public void startLoadingView() {
		this.setVisibility(View.VISIBLE);
	}

	public void stopLoadingView() {
		this.setVisibility(View.GONE);
	}
}
