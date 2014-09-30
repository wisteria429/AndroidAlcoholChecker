package com.example.alcohol;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements Runnable{
	private static final String TAG = "MainActiviy";
	private static final int REQ_ENABLE_BT = 0;
	
	private final String BT_DEVICE = "EasyBT";
	private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); 
	
	private BluetoothAdapter mAdapter;
	private BluetoothDevice mDevice;
	private BluetoothSocket mSocket;
	private boolean isRunning;
	private Handler bHandler;
	private Thread mThread;
	private TextView value;
	private TextView msg;	
	private String mValue;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        value = (TextView) findViewById(R.id.value);
        msg = (TextView) findViewById(R.id.msg);
        bHandler = new Handler();
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mAdapter.equals(null)) {
        	Toast.makeText(this, "Bluetoothがsupportされています", Toast.LENGTH_SHORT).show();        	
        } else {
        	Toast.makeText(this, "Bluetoothがsupportされていません", Toast.LENGTH_SHORT).show();
        	finish();
        }
        
        if (mAdapter.isEnabled()) {
        	Set<BluetoothDevice> devices = mAdapter.getBondedDevices();
    		System.out.println("DEBUG:devices.size:" + devices.size());
            Log.v(TAG,"devices.size:" + devices.size());
            for(BluetoothDevice device : devices){
            	Log.i(TAG,"DEVICE:"+ device.getName());
            	if(device.getName().equals(BT_DEVICE)){
            		Log.d(TAG, BT_DEVICE + " FOUND");
            		mDevice = device;
            		
                	mThread = new Thread(this);
                	isRunning = true;
                	
                	mThread.start();
            	}
            	
            
            }
        } else {
        	Intent btOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(btOn, REQ_ENABLE_BT);
        }
        
    }
    
    


    @Override
	protected void onActivityResult(int reqCode, int resCode, Intent data) {
    	if (reqCode == REQ_ENABLE_BT) {
    		if (resCode == Activity.RESULT_OK) {
    			Toast.makeText(this, "bt on", Toast.LENGTH_SHORT).show();
    		} else {
    			Toast.makeText(this, "bt off", Toast.LENGTH_SHORT).show();
    			
    		}
    	}
		
	}

    @Override
	public void run() {
		InputStream mmInStream = null;
		
		try {
			// 取得したデバイス名を使ってBluetoothでSocket接続
			
			mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
			
			mSocket.connect();
			System.out.println("bluetooth mSocket");
			Log.v(TAG,"con");
			mmInStream = mSocket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(mmInStream));
			// InputStreamのバッファを格納
			byte[] buffer = new byte[1024];
			// 取得したバッファのサイズを格納
			int bytes;
			
			while(isRunning){
				Log.v(TAG,"isRun");
				mValue = br.readLine();
				Log.v(TAG, mValue);
			
				bHandler.post(new Runnable(){

					public void run() {
						value.setText(mValue);
						int value = Integer.parseInt(mValue);
						// ------------------------　Score保存 ----------------------- //
						
						if (value > 930) {
							msg.setText("息はいてー");
						} else if (value > 900) {
							msg.setText("お酒飲んで来てね!");
						} else if (value > 800) {
							msg.setText("もっと飲んで！");
						} else if (value > 700) {
							msg.setText("楽しく飲もう！");
						} else {
							msg.setText("飲み過ぎ注意");
						}
						
					}
				});

				
				Thread.sleep(1000);

			}
		} catch (IOException e) {
			e.printStackTrace();
			
		} catch (Exception e) {
			Log.e(TAG,"error:"+e);
			e.printStackTrace();
			
			try {
				mSocket.close();
			} catch (Exception ee){
				Log.e(TAG,"error:"+ee);
				e.printStackTrace();
			}
			isRunning = false;
			Log.e(TAG,"error");
		}
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
