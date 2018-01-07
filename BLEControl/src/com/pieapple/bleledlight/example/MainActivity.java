package com.pieapple.bleledlight.example;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.elecfreaks.bleexample.R;
import com.pieapple.bleledlight.BLEDeviceListAdapter;
import com.pieapple.bleledlight.BluetoothHandler;
import com.pieapple.bleledlight.BluetoothHandler.OnConnectedListener;
import com.pieapple.bleledlight.BluetoothHandler.OnScanListener;
import com.pieapple.bleledlight.example.Protocol.OnReceivedRightDataListener;

public class MainActivity extends Activity implements View.OnClickListener {

	private Button scanButton;
	private ListView bleDeviceListView;
	private BLEDeviceListAdapter listViewAdapter;

	private BluetoothHandler bluetoothHandler;
	private boolean isConnected;
	private Protocol protocol;

	private static final boolean INPUT = false;
	private static final boolean OUTPUT = true;
	private static final boolean LOW = false;
	private static final boolean HIGH = true;

	private boolean digitalVal[];
	private int analogVal[];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		findViewById(R.id.gui_mode_layout).setVisibility(View.INVISIBLE);

		scanButton = (Button) findViewById(R.id.scanButton);
		bleDeviceListView = (ListView) findViewById(R.id.bleDeviceListView);

		findViewById(R.id.switch_mode_btn).setOnClickListener(this);

		findViewById(R.id.requestTurnLedOnBtn).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						bluetoothHandler.sendData("P");
						Log.i("DEBUG_TAG", "requestTurnLedOnBtn click");
					}
				});

		findViewById(R.id.requestTurnLedOffBtn).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						bluetoothHandler.sendData("p");
						Log.i("DEBUG_TAG", "requestTurnLedOffBtn click");
					}
				});

		findViewById(R.id.requestToggleLedBtn).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						bluetoothHandler.sendData("T");
						Log.i("DEBUG_TAG", "requestToggleLedBtn click");
					}
				});

		listViewAdapter = new BLEDeviceListAdapter(this);
		digitalVal = new boolean[14];
		analogVal = new int[14];

		bluetoothHandler = new BluetoothHandler(this);
		bluetoothHandler.setOnConnectedListener(new OnConnectedListener() {

			@Override
			public void onConnected(boolean isConnected) {
				// TODO Auto-generated method stub
				setConnectStatus(isConnected);
				// bluetoothHandler.sendData("L");
			}
		});
		protocol = new Protocol(this, new Transmitter(this, bluetoothHandler));
		protocol.setOnReceivedDataListener(recListener);
	}

	private OnReceivedRightDataListener recListener = new OnReceivedRightDataListener() {

		@Override
		public int onReceivedData(String str) {
			// TODO Auto-generated method stub
			try {
				JSONObject readJSONObject = new JSONObject(str);
				int type = readJSONObject.getInt("T");
				int value = readJSONObject.getInt("V");

				switch (type) {
				case Protocol.ANALOG: {
					int pin = readJSONObject.getInt("P");
					analogVal[pin] = value;
				}
					break;
				case Protocol.DIGITAL: {
					int pin = readJSONObject.getInt("P");
					digitalVal[pin] = (value > 0) ? HIGH : LOW;
				}
					break;
				case Protocol.TEMPERATURE: {
					float temperature = ((float) value) / 100;
				}
					break;
				case Protocol.HUMIDITY: {
					float humidity = ((float) value) / 100;
				}
					break;
				default:
					break;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return 0;
		}
	};

	public void scanOnClick(final View v) {
		if (!isConnected) {
			bleDeviceListView.setAdapter(bluetoothHandler
					.getDeviceListAdapter());
			bleDeviceListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					String buttonText = (String) ((Button) v).getText();
					if (buttonText.equals("scanning")) {
						showMessage("scanning...");
						return;
					}
					BluetoothDevice device = bluetoothHandler
							.getDeviceListAdapter().getItem(position).device;
					// connect
					bluetoothHandler.connect(device.getAddress());
				}
			});
			bluetoothHandler.setOnScanListener(new OnScanListener() {
				@Override
				public void onScanFinished() {
					// TODO Auto-generated method stub
					((Button) v).setText("scan");
					((Button) v).setEnabled(true);
				}

				@Override
				public void onScan(BluetoothDevice device, int rssi,
						byte[] scanRecord) {
				}
			});
			((Button) v).setText("scanning");
			((Button) v).setEnabled(false);
			bluetoothHandler.scanLeDevice(true);
		} else {
			setConnectStatus(false);
		}
	}

	public void setConnectStatus(boolean isConnected) {
		this.isConnected = isConnected;
		if (isConnected) {
			showMessage("Connection successful");
			scanButton.setText("break");
		} else {
			bluetoothHandler.onPause();
			bluetoothHandler.onDestroy();
			scanButton.setText("scan");
		}
	}

	private void showMessage(String str) {
		Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
	}

	public boolean mIsLedLightOn = false;

	@Override
	public void onClick(View v) {
		int vid = v.getId();
		switch (vid) {
		case R.id.switch_mode_btn:
			this.switchModeLayout();
			break;
		case R.id.JTImgBtn:
			Log.i("DEBUG_TAG", "JTImgBtn is clicked");
			
			/*
			if (mIsLedLightOn) {
				bluetoothHandler.sendData("p");
				mIsLedLightOn = false;
			} else {
				bluetoothHandler.sendData("P");
				mIsLedLightOn = true;
			}
			*/
			
			bluetoothHandler.sendData("t");
			
			
			Log.i("DEBUG_TAG", "JTImgBtn is click");
			break;
		}
	}

	public boolean mIsToolModeLayout = true;

	protected void switchModeLayout() {
		View GuiModeView = this.findViewById(R.id.gui_mode_layout);
		View ToolModeView = this.findViewById(R.id.tool_mode_layout);

		if (mIsToolModeLayout == true) {
			mIsToolModeLayout = false;
			ToolModeView.setVisibility(View.INVISIBLE);
			GuiModeView.setVisibility(View.VISIBLE);

		} else // for GuiModeLayout
		{
			mIsToolModeLayout = true;
			ToolModeView.setVisibility(View.VISIBLE);
			GuiModeView.setVisibility(View.INVISIBLE);
			View v = findViewById(R.id.JTImgBtn);
			int len = v.getWidth();
			// v.setLayoutParams(new FrameLayout.LayoutParams(len, len));
			v.setLayoutParams(new FrameLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, len));
			v.setOnClickListener(this);

		}
	}
}
