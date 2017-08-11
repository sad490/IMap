package com.example.hp.imap.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hp.imap.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.UUID;

/**
 * Created by hp on 2017/8/6.
 */

public class IBluetooth extends Activity {
    private static final UUID MY_UUID =
            UUID.fromString("e3153ba5-95c4-4a38-9b4e-17749ee0e56d");
    private static final String SEARCH_NAME = "bluetooth.recipe";

    private static final int REQUEST_ENABLE = 1;
    private static final int REQUEST_DISCOVERABLE = 2;

    BluetoothAdapter mBtAdapter;
    BluetoothSocket mBtSocket;
    Button listenButton, scanButton;
    EditText Message;

    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main_bluetooth);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter == null)
        {
            Toast.makeText(this, "Bluetooth is not supported !!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if(!mBtAdapter.isEnabled())
        {
            Intent enableIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE);
        }
        Message = (EditText)findViewById(R.id.Message);
        listenButton = (Button)findViewById(R.id.listen);
        listenButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(mBtAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
                {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
                    return;
                }
                startListening();
            }
        });
        scanButton = (Button)findViewById(R.id.scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtAdapter.startDiscovery();
                setProgressBarIndeterminateVisibility(true);
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        try{
            if(mBtSocket != null)
            {
                mBtSocket.close();
            }
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case REQUEST_ENABLE:
                if(resultCode != Activity.RESULT_OK)
                {
                    Toast.makeText(this, "BlueTooth not Enable !!!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_DISCOVERABLE:
                if(resultCode == Activity.RESULT_CANCELED)
                {
                    Toast.makeText(this, "Must be discoverable", Toast.LENGTH_SHORT).show();
                } else {
                   startListening();
                }
                break;
            default:
                break;
        }
    }

    private void startListening()
    {
        AcceptTask task = new AcceptTask();
        task.execute(MY_UUID);
        setProgressBarIndeterminateVisibility(true);
    }

    private class AcceptTask extends AsyncTask<UUID, Void, BluetoothSocket>{
        @Override
        protected BluetoothSocket doInBackground(UUID... params)
        {
            String name = mBtAdapter.getName();
            try{
                mBtAdapter.setName(SEARCH_NAME);
                BluetoothServerSocket socket = mBtAdapter.listenUsingRfcommWithServiceRecord(
                        "BluetoothRecipe", params[0]);
                BluetoothSocket connected = socket.accept();
                mBtAdapter.setName(name);
                return connected;
            } catch(IOException e)
            {
                e.printStackTrace();
                mBtAdapter.setName(name);
                return null;
            }
        }
        @Override
        protected void onPostExecute(BluetoothSocket socket)
        {
            if (socket == null)
            {
                return ;
            }
            mBtSocket = socket;
            ConnectedTask task = new ConnectedTask();
            task.execute(mBtSocket);
        }
    }

    private class ConnectedTask extends
            AsyncTask<BluetoothSocket, Void, String>{
        @Override
        protected String doInBackground(BluetoothSocket...  params)
        {
            InputStream in = null;
            OutputStream out = null;
            try {
                out = params[0].getOutputStream();
                String message = Message.getText().toString();
                out.write(message.getBytes());
                in = params[0].getInputStream();
                byte[] buffer = new byte[1024];
                in.read(buffer);
                String result = new String(buffer);
                mBtSocket.close();
                return result.trim();

            }catch(Exception e)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            Toast.makeText(IBluetooth.this, result, Toast.LENGTH_SHORT).show();
            setProgressBarIndeterminateVisibility(true);
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (TextUtils.equals(device.getName(), SEARCH_NAME))
                {
                    mBtAdapter.cancelDiscovery();
                    try{
                        mBtSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                        mBtSocket.connect();
                        ConnectedTask task = new ConnectedTask();
                        task.execute(mBtSocket);
                    }catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                setProgressBarIndeterminateVisibility(false);
            }
        }
    };
}
