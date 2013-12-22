package com.viyu.lanyl.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ConversationListViewAdapter extends BaseAdapter {

	public final static String KEY_CHECKACTION = "CheckAction";

	private List<ItemRenderer> mData;

	private LayoutInflater mInflater;

	public ConversationListViewAdapter(Context context) {
		mData = new ArrayList<ItemRenderer>();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void addItemRenderer(ItemRenderer item) {
		mData.add(item);
		notifyDataSetChanged();
    }
	
	public void clear() {
		mData.clear();
	}
	
	public int getCount() {
		return mData.size();
	}

	public Object getItem(int position) {
		return mData.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public int getViewTypeCount() {
		return 4;
	}
	
	@Override
	public int getItemViewType(int position) {
		return mData.get(position).getRendererType();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent);
	}

	private View createViewFromResource(int position, View convertView, ViewGroup parent) {
		ItemRenderer renderer = mData.get(position);

		if (renderer == null || renderer.getLayoutId() == -1)
			return null;

		View layoutView;
		boolean reused = false;
		if (convertView == null) {
			layoutView = mInflater.inflate(renderer.getLayoutId(), parent, false);
		} else {
			reused = true;
			layoutView = convertView;
		}
		decorateView(position, layoutView, renderer, reused);
		return layoutView;
	}

	private void decorateView(int position, View layoutView, ItemRenderer renderer, boolean reused) {
		if (renderer.getMessageViewId() == -1)
			return;

		View view = layoutView.findViewById(renderer.getMessageViewId());
		if (view instanceof TextView) {
			((TextView) view).setText(renderer.getMessage() /*+ (reused ? "~" : "")*/);
			
		} else if (view instanceof RelativeLayout && renderer.getAdsView() != null) {// for ads,把ads放到listview中行不通
			/*((RelativeLayout)view).removeAllViews();
			((RelativeLayout)view).addView(renderer.getAdsView());*/
		}
	}

	public static class ItemRenderer {
		public static final int TYPE_SENDER = 0;
		public static final int TYPE_RECEIVER = 1;
		public static final int TYPE_HINT = 2;
		public static final int TYPE_ADS = 3;

		private int renderType = TYPE_HINT;

		private String message = "";
		private View adsView = null;

		public ItemRenderer(int type, String message) {
			this.renderType = type;
			this.message = message;
		}
		
		public ItemRenderer(int type, String message, View adsView) {
			this(type, message);
			this.adsView = adsView;
		}

		public int getRendererType() {
			return renderType;
		}
		
		public int getLayoutId() {
			switch (renderType) {
			case TYPE_SENDER:
				return com.viyu.lanyl.R.layout.listitem_message_right;
			case TYPE_RECEIVER:
				return com.viyu.lanyl.R.layout.listitem_message_left;
			case TYPE_HINT:
				return com.viyu.lanyl.R.layout.listitem_message_hint;
			case TYPE_ADS:
			}
			return -1;
		}

		public int getMessageViewId() {
			switch (renderType) {
			case TYPE_SENDER:
				return com.viyu.lanyl.R.id.listitem_message_right_text;
			case TYPE_RECEIVER:
				return com.viyu.lanyl.R.id.listitem_message_left_text;
			case TYPE_HINT:
				return com.viyu.lanyl.R.id.listitem_message_hint_text;
			case TYPE_ADS:
			}
			return -1;
		}

		public String getMessage() {
			return this.message;
		}
		
		public View getAdsView() {
			return this.adsView;
		}

		@Override
		public String toString() {
			return message;
		}
	}
}
