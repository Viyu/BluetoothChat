package com.viyu.lanyl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * 
 * @author Viyu_Lu
 * 
 */
public class LanylDeviceListActivity extends Activity implements OnItemClickListener/*, com.viyu.lanyl.view.TitlePopup.OnItemOnClickListener*/ {
	public static String EXTRA_DEVICE_ADDRESS = "DeviceAddress";

	private ArrayAdapter<NameAndAddress> pairedDevicesArrayAdapter;
	private ArrayAdapter<NameAndAddress> newDevicesArrayAdapter;

	private BluetoothAdapter bluetoothAdapter = null;

	private ProgressBar searchNewProgress = null;
	private ImageButton searchNewButton = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_devicelist);

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Toast.makeText(this, R.string.lanyanotvalid, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		pairedDevicesArrayAdapter = new ArrayAdapter<NameAndAddress>(this, R.layout.listitem_deviceinfo);
		newDevicesArrayAdapter = new ArrayAdapter<NameAndAddress>(this, R.layout.listitem_deviceinfo);

		ListView pairedListView = (ListView) findViewById(R.id.page_devicelist_paireddevices);
		pairedListView.setAdapter(pairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(this);

		ListView newDevicesListView = (ListView) findViewById(R.id.page_devicelist_newdevices);
		newDevicesListView.setAdapter(newDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(this);

		searchNewProgress = (ProgressBar)findViewById(R.id.devicelist_progressbar);
		searchNewButton = (ImageButton)findViewById(R.id.page_devicelist_search);
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);
		//
		if (!bluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else {
			updateDeviceList();
		}
	}

	private static final int REQUEST_ENABLE_BT = 3;

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				updateDeviceList();
			} else {
				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void updateDeviceList() {
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

		if (pairedDevices.size() > 0) {
			findViewById(R.id.page_devicelist_title_yipeiduitextview).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				pairedDevicesArrayAdapter.add(new NameAndAddress(device.getName(), device.getAddress()));
			}
		} else {
			String noDevices = getResources().getText(R.string.none_paired).toString();
			pairedDevicesArrayAdapter.add(new NameAndAddress(noDevices, null));
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (bluetoothAdapter != null) {
			bluetoothAdapter.cancelDiscovery();
		}

		this.unregisterReceiver(mReceiver);
	}

	private void ensureDiscoverable() {
		if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	private void doDiscovery() {
		searchNewProgress.setVisibility(View.VISIBLE);
		searchNewButton.setEnabled(false);
		newDevicesArrayAdapter.clear();
		foundDevice.clear();
		/*//
		if (bluetoothAdapter.isDiscovering()) {
			bluetoothAdapter.cancelDiscovery();
		}*/
		//
		bluetoothAdapter.startDiscovery();
	}

	/**
	 * list的点击事件
	 */
	public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
		bluetoothAdapter.cancelDiscovery();

		NameAndAddress naa = (NameAndAddress) av.getItemAtPosition(arg2);
		String address = naa.address;

		if (address != null) {
			Intent intent = new Intent(this, LanylActivity.class);
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			startActivity(intent);
		}
	}

	public void makeVisible(View view) {
		ensureDiscoverable();
	}
	
	public void searchDevice(View view) {
		doDiscovery();
	}
	
	List<NameAndAddress> foundDevice = new ArrayList<NameAndAddress>();
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					NameAndAddress naa = new NameAndAddress(device.getName(), device.getAddress());
					if(!foundDevice.contains(naa)) {
						newDevicesArrayAdapter.add(naa);
						foundDevice.add(naa);
					}
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				searchNewProgress.setVisibility(View.INVISIBLE);
				searchNewButton.setEnabled(true);
				
				if (newDevicesArrayAdapter.getCount() == 0) {
					String noDevices = getResources().getText(R.string.none_found).toString();
					newDevicesArrayAdapter.add(new NameAndAddress(noDevices, null));
				}
			}
		}
	};
	
	protected class NameAndAddress {
		String name = null;
		String address = null;

		public NameAndAddress(String name, String address) {
			if (name == null || name.equals("")) {
				getString(R.string.nonamedevice);
			}
			this.name = name;
			this.address = address;
		}

		@Override
		public boolean equals(Object o) {
			if(o == null)
				return false;
			if(!(o instanceof NameAndAddress)) {
				return false;
			}
			NameAndAddress naa = (NameAndAddress)o;
			return naa.name.equals(this.name) && naa.address.equals(this.address);
		}
		
		@Override
		public String toString() {
			if(name == null || name.equals("")) {
				return getString(R.string.nonamedevice);
			}
			return name;
		}
	}

}
