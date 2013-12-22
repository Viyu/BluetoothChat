package com.viyu.lanyl;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.viyu.lanyl.view.ConversationListViewAdapter;
import com.viyu.lanyl.view.DateTimeFormater;
import com.viyu.lanyl.view.ConversationListViewAdapter.ItemRenderer;

/**
 * 
 * @author Viyu_Lu
 * 
 */
public class LanylActivity extends Activity {

	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	private ListView conversationView;
	private EditText outEditText;
	private Button sendButton;

	private String connectedDeviceName = null;
	private ConversationListViewAdapter conversationArrayAdapter;
	private StringBuffer outStringBuffer;
	private BluetoothAdapter bluetoothAdapter = null;
	private LanylService chatService = null;

	private ImageButton connectButton = null;
	
	private TextView titlebar = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_chat);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	
		//
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		titlebar = (TextView) findViewById(R.id.chatpage_titlebartextview);
		ImageButton back = (ImageButton) findViewById(R.id.chatpage_backbutton);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		connectButton = (ImageButton)findViewById(R.id.chatpage_connect);
		connectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				connectDevice(getIntent(), false);
			}
		});

		//
		conversationArrayAdapter = new ConversationListViewAdapter(this);
		//提示用户
		conversationArrayAdapter.addItemRenderer(new ItemRenderer(ItemRenderer.TYPE_HINT, getString(R.string.hint_haotoconnect)));
		conversationArrayAdapter.addItemRenderer(new ItemRenderer(ItemRenderer.TYPE_HINT, getString(R.string.hint_iffailed)));
		
		//
		conversationView = (ListView) findViewById(R.id.chatpage_talkcontentlistview);
		conversationView.setAdapter(conversationArrayAdapter);
		outEditText = (EditText) findViewById(R.id.chatpage_inputmessageedittext);
		outEditText.setOnEditorActionListener(mWriteListener);

		sendButton = (Button) findViewById(R.id.button_send);
		sendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				TextView view = (TextView) findViewById(R.id.chatpage_inputmessageedittext);
				String message = view.getText().toString();
				sendMessage(message);
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (chatService != null) {
			chatService.stop();
			chatService = null;
		}
		finish();
		super.onBackPressed();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (chatService == null)
			setupChat();
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (chatService != null) {
			if (chatService.getState() == LanylService.STATE_NONE) {
				chatService.start();
			}
		}
	}

	private void setupChat() {
		chatService = new LanylService(this, mHandler);
		outStringBuffer = new StringBuffer("");
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (chatService != null) {
			chatService.stop();
			chatService = null;
		}
	}

	private void sendMessage(String message) {
		//
		boolean fortest = false;
		if (fortest) {
			conversationArrayAdapter.addItemRenderer(new ItemRenderer(ItemRenderer.TYPE_SENDER, message));
			conversationArrayAdapter.addItemRenderer(new ItemRenderer(ItemRenderer.TYPE_RECEIVER, message));
			outStringBuffer.setLength(0);
			outEditText.setText(outStringBuffer);
		}
		//

		if (chatService.getState() != LanylService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}

		if (message.length() > 0) {
			byte[] send = message.getBytes();
			chatService.write(send);

			outStringBuffer.setLength(0);
			outEditText.setText(outStringBuffer);
		}
	}

	private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
				String message = view.getText().toString();
				sendMessage(message);
			}
			return true;
		}
	};

	private final void setStatus(int resId) {
		titlebar.setText(getString(resId));
	}

	private final void setStatus(CharSequence subTitle) {
		titlebar.setText(subTitle);
	}

	private final MyHandler mHandler = new MyHandler(this);

	private static class MyHandler extends Handler {

		private final WeakReference<LanylActivity> ref;

		public MyHandler(LanylActivity lanyl) {
			ref = new WeakReference<LanylActivity>(lanyl);
		}

		@Override
		public void handleMessage(Message msg) {
			LanylActivity lanyl = ref.get();
			if (lanyl != null) {
				switch (msg.what) {
				case MESSAGE_STATE_CHANGE:
					switch (msg.arg1) {
					case LanylService.STATE_CONNECTED:
						lanyl.setStatus(lanyl.getString(R.string.title_connected_to, lanyl.connectedDeviceName));
						//
						lanyl.conversationArrayAdapter.clear();
						//再加个当前时间
						lanyl.conversationArrayAdapter.addItemRenderer(new ItemRenderer(ItemRenderer.TYPE_HINT, DateTimeFormater.getInstance().getFormatedNowTime()));
						//先提示一下用户信息不保存
						lanyl.conversationArrayAdapter.addItemRenderer(new ItemRenderer(ItemRenderer.TYPE_HINT, lanyl.getString(R.string.hint_notsaverecord)));
						lanyl.conversationArrayAdapter.addItemRenderer(new ItemRenderer(ItemRenderer.TYPE_HINT, lanyl.getString(R.string.hint_leavemeiyoule)));
						lanyl.conversationArrayAdapter.addItemRenderer(new ItemRenderer(ItemRenderer.TYPE_HINT, lanyl.getString(R.string.hint_copytoclip)));
						//
						break;
					case LanylService.STATE_CONNECTING:
						lanyl.setStatus(R.string.title_connecting);
						break;
					case LanylService.STATE_LISTEN:
					case LanylService.STATE_NONE:
						lanyl.setStatus(R.string.status_ready_toconnect);
						break;
					}
					break;
				case MESSAGE_WRITE:
					byte[] writeBuf = (byte[]) msg.obj;
					String writeMessage = new String(writeBuf);
					lanyl.conversationArrayAdapter.addItemRenderer(new ItemRenderer(ItemRenderer.TYPE_SENDER, writeMessage));
					break;
				case MESSAGE_READ:
					byte[] readBuf = (byte[]) msg.obj;
					String readMessage = new String(readBuf, 0, msg.arg1);
					lanyl.conversationArrayAdapter.addItemRenderer(new ItemRenderer(ItemRenderer.TYPE_RECEIVER, readMessage));
					break;
				case MESSAGE_DEVICE_NAME:
					lanyl.connectedDeviceName = msg.getData().getString(DEVICE_NAME);
					Toast.makeText(lanyl.getApplicationContext(), lanyl.getString(R.string.title_connected) + lanyl.connectedDeviceName, Toast.LENGTH_SHORT).show();
					break;
				case MESSAGE_TOAST:
					Toast.makeText(lanyl.getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}
	};

	private void connectDevice(Intent data, boolean secure) {
		String address = data.getExtras().getString(LanylDeviceListActivity.EXTRA_DEVICE_ADDRESS);
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
		chatService.connect(device, secure);
	}
}
