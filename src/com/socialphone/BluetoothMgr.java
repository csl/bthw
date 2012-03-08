package com.socialphone;

import java.util.ArrayList;
import java.util.StringTokenizer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothMgr extends Activity {
    // Debugging
    private static final String TAG = "BluetoothMgr";
    private static final boolean D = true;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    int prestatus = 0;
    static int reconnect = 0;
    int autoconnect = 0;
    int count_t=0;
    
    static String address;
    
    BluetoothMgr bm = this;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mChatService = null;

    private medplayer mp;
    
    static char auchCRCHi[ ]={
    	0x00,0xc1,0x81,0x40,0x01,0xc0,0x80,0x41,0x01,0xc0,
    	0x80,0x41,0x00,0xc1,0x81,0x40,0x01,0xc0,0x80,0x41,
    	0x00,0xc1,0x81,0x40,0x00,0xc1,0x81,0x40,0x01,0xc0,
    	0x80,0x41,0x01,0xc0,0x80,0x41,0x00,0xc1,0x81,0x40,
    	0x00,0xc1,0x81,0x40,0x01,0xc0,0x80,0x41,0x00,0xc1,
    	0x81,0x40,0x01,0xc0,0x80,0x41,0x01,0xc0,0x80,0x41,
    	0x00,0xc1,0x81,0x40,0x01,0xc0,0x80,0x41,0x00,0xc1,
    	0x81,0x40,0x00,0xc1,0x81,0x40,0x01,0xc0,0x80,0x41,
    	0x00,0xc1,0x81,0x40,0x01,0xc0,0x80,0x41,0x01,0xc0,
    	0x80,0x41,0x00,0xc1,0x81,0x40,0x00,0xc1,0x81,0x40,
    	0x01,0xc0,0x80,0x41,0x01,0xc0,0x80,0x41,0x00,0xc1,
    	0x81,0x40,0x01,0xc0,0x80,0x41,0x00,0xc1,0x81,0x40,
    	0x00,0xc1,0x81,0x40,0x01,0xc0,0x80,0x41,0x01,0xc0,
    	0x80,0x41,0x00,0xc1,0x81,0x40,0x00,0xc1,0x81,0x40,
    	0x01,0xc0,0x80,0x41,0x00,0xc1,0x81,0x40,0x01,0xc0,
    	0x80,0x41,0x01,0xc0,0x80,0x41,0x00,0xc1,0x81,0x40,
    	0x00,0xc1,0x81,0x40,0x01,0xc0,0x80,0x41,0x01,0xc0,
    	0x80,0x41,0x00,0xc1,0x81,0x40,0x01,0xc0,0x80,0x41,
    	0x00,0xc1,0x81,0x40,0x00,0xc1,0x81,0x40,0x01,0xc0,
    	0x80,0x41,0x00,0xc1,0x81,0x40,0x01,0xc0,0x80,0x41,
    	0x01,0xc0,0x80,0x41,0x00,0xc1,0x81,0x40,0x01,0xc0,
    	0x80,0x41,0x00,0xc1,0x81,0x40,0x00,0xc1,0x81,0x40,
    	0x01,0xc0,0x80,0x41,0x01,0xc0,0x80,0x41,0x00,0xc1,
    	0x81,0x40,0x00,0xc1,0x81,0x40,0x01,0xc0,0x80,0x41,
    	0x00,0xc1,0x81,0x40,0x01,0xc0,0x80,0x41,0x01,0xc0,
    	0x80,0x41,0x00,0xc1,0x81,0x40};
    
    	static char auchCRCLo[]={
    	0x00,0xc0,0xc1,0x01,0xc3,0x03,0x02,0xc2,0xc6,0x06,
    	0x07,0xc7,0x05,0xc5,0xc4,0x04,0xcc,0x0c,0x0d,0xcd,
    	0x0f,0xcf,0xce,0x0e,0x0a,0xca,0xcb,0x0b,0xc9,0x09,
    	0x08,0xc8,0xd8,0x18,0x19,0xd9,0x1b,0xdb,0xda,0x1a,
    	0x1e,0xde,0xdf,0x1f,0xdd,0x1d,0x1c,0xdc,0x14,0xd4,
    	0xd5,0x15,0xd7,0x17,0x16,0xd6,0xd2,0x12,0x13,0xd3,
    	0x11,0xd1,0xd0,0x10,0xf0,0x30,0x31,0xf1,0x33,0xf3,
    	0xf2,0x32,0x36,0xf6,0xf7,0x37,0xf5,0x35,0x34,0xf4,
    	0x3c,0xfc,0xfd,0x3d,0xff,0x3f,0x3e,0xfe,0xfa,0x3a,
    	0x3b,0xfb,0x39,0xf9,0xf8,0x38,0x28,0xe8,0xe9,0x29,
    	0xeb,0x2b,0x2a,0xea,0xee,0x2e,0x2f,0xef,0x2d,0xed,0xec,
    	0x2c,0xe4,0x24,0x25,0xe5,0x27,0xe7,0xe6,0x26,
    	0x22,0xe2,0xe3,0x23,0xe1,0x21,0x20,0xe0,0xa0,0x60,
    	0x61,0xa1,0x63,0xa3,0xa2,0x62,0x66,0xa6,0xa7,0x67,
    	0xa5,0x65,0x64,0xa4,0x6c,0xac,0xad,0x6d,0xaf,0x6f,
    	0x6e,0xae,0xaa,0x6a,0x6b,0xab,0x69,0xa9,0xa8,0x68,
    	0x78,0xb8,0xb9,0x79,0xbb,0x7b,0x7a,0xba,0xbe,0x7e,
    	0x7f,0xbf,0x7d,0xbd,0xbc,0x7c,0xb4,0x74,0x75,0xb5,
    	0x77,0xb7,0xb6,0x76,0x72,0xb2,0xb3,0x73,0xb1,0x71,
    	0x70,0xb0,0x50,0x90,0x91,0x51,0x93,0x53,0x52,0x92,
    	0x96,0x56,0x57,0x97,0x55,0x95,0x94,0x54,0x9c,0x5c,
    	0x5d,0x9d,0x5f,0x9f,0x9e,0x5e,0x5a,0x9a,0x9b,0x5b,
    	0x99,0x59,0x58,0x98,0x88,0x48,0x49,0x89,0x4b,0x8b,
    	0x8a,0x4a,0x4e,0x8e,0x8f,0x4f,0x8d,0x4d,0x4c,0x8c,
    	0x44,0x84,0x85,0x45,0x87,0x47,0x46,0x86,0x82,0x42,
    	0x43,0x83,0x41,0x81,0x80,0x40};    

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        mp = null;

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize the BluetoothService to perform bluetooth connections
        mChatService = new BluetoothService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    
    public static byte[] int2byte(ArrayList<Integer> src) {
        int srcLength = src.size();
        byte[]dst = new byte[srcLength];
        
        for (int i=0; i<srcLength; i++) {
            int x = src.get(i);
            dst[i] = (byte) ((x >>> 0) & 0xff);           
        }
        return dst;
    }    

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
        	
        	StringTokenizer Tok = new StringTokenizer(message, ",");
            ArrayList<Integer> array = new ArrayList<Integer>();

            while (Tok.hasMoreElements())
            {
            	array.add(Integer.valueOf((String) Tok.nextElement(), 16));
            }      	

        	byte[] send = int2byte(array);
        	
        	for (int j=0; j<send.length; j++)
        	{
        		Log.i(TAG, "send:" + String.format("0x%02x", send[j]));
        	}
        	
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            //mOutStringBuffer.setLength(0);
           // mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };
    
     int CRC16(byte Msg[])
     {
    	 int uchCRCHi=0xFF ; /*CRC high byte*/
    	 int uchCRCLo=0xFF ; /*CRC low byte*/
    	 int uIndex;
    	 int count=0;
    	 
    	 int usDatalen = Msg.length;
    	 
    	 while(usDatalen != 0) /*pass through message buffer*/
    	 {
    		 uIndex=uchCRCHi^Msg[count]; /*calculate the CRC*/
    		 count++;
    		 uchCRCHi=(uchCRCLo) ^ (auchCRCHi[uIndex]);
    		 uchCRCLo=auchCRCLo[uIndex] ;
    		 usDatalen--;
    	 }
    	 
    	 return (uchCRCHi<<8 | uchCRCLo) ;
    }
     
    // The Handler that gets information back from the BluetoothService
    private final Handler mHandler = new Handler() 
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) 
                {
                case BluetoothService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    mConversationArrayAdapter.clear();
                    reconnect = 0;
                    
                    break;
                case BluetoothService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                	
                	//disconnect && message
                	if (prestatus == BluetoothService.STATE_CONNECTED)
                	{
                         //voice
                         mp = new medplayer();
                         mp.play_voice("warn.mp3");

                         mTitle.setText("disconnect...");
                        
                        //reconnect
                        reconnect = 1;
                        Intent serverIntent = new Intent(bm, DeviceListActivity.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                        
                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                        mChatService.connect(device);
                        
                	}
                	else
                	{
                		mTitle.setText(R.string.title_not_connected);
                	}
                    break;
                }
                prestatus = msg.arg1;
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                //String writeMessage = new String(writeBuf);
                //mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                //String readMessage = new String(readBuf, 0, msg.arg1);
                
                int start=0;
                for (int j=0; j<readBuf.length; j++)
            	{
                	if (readBuf[j] == 0x00) continue;
                   		Log.i(TAG, count_t + "  " + String.format("0x%02X", readBuf[j]));
               	}
                count_t++;
/*                int start=0, bytecount=0;
                byte crchi = 0;                
                byte crclo = 0;                
                byte[] Buf = new byte[readBuf.length];
                byte[] crcBuf = new byte[readBuf.length-4];
                for (int j=0; j<readBuf.length; j++)
            	{
                	if (readBuf[j] == 0x0A)
                	{
                		start = 1;
                	}
                	else if (readBuf[j] == 0x0D) 
                	{
                		start = 0;
                		//clone
                        for (int r=0; j<readBuf.length-2; r++)
                    	{
                        	crcBuf[r] = readBuf[r];
                    		Log.i(TAG, String.format("0x%02X", crcBuf[r]));
                    	}
                        
                        crchi = readBuf[readBuf.length-2];
                        crclo = readBuf[readBuf.length-1];
                        
                		break;
                	}
                	else if (start == 1)
                	{
                		Log.i(TAG, Integer.toString(bytecount));
                		Buf[bytecount] = readBuf[j];
                		bytecount++;
                	}
            	}
                
                //cal crc
                
                Log.i(TAG, "crc: " + String.format("0x%X", CRC16(crcBuf)));
                Log.i(TAG, "crchi: " + String.format("0x%02X", crchi));
                Log.i(TAG, "crclo: " + String.format("0x%02X", crclo));
  */              
                
                
                //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            if (resultCode == Activity.RESULT_OK) {
            	//now address
                address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                mChatService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            if (resultCode == Activity.RESULT_OK) {
                setupChat();
            } else {
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
        	reconnect=0;
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            ensureDiscoverable();
            return true;
        }
        return false;
    }
    

}