package com.example.android.scannerapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

public class MainActivity extends AppCompatActivity implements Listener{

    public static final String TAG = MainActivity.class.getSimpleName();

    private TextView mTvMessage;
    private Button mBtPopulate;
//    private Button mBtWrite;
//    private Button mBtRead;

    private NFCWriteFragment mNfcWriteFragment;
    private NFCReadFragment mNfcReadFragment;

    private boolean isDialogDisplayed = false;
    private boolean isWrite = false;

    private NfcAdapter mNfcAdapter;

    com.getbase.floatingactionbutton.FloatingActionButton actionAdd;
    com.getbase.floatingactionbutton.FloatingActionButton actionMemo;

    String orderDetails =
            "Product\tStore\tQuantity\tUnit Weight\n" +
            "6104940\t526\t1\t1.16\n" +
            "5412340\t526\t4\t0.26\n" +
            "5911943\t526\t2\tNULL\n" +
            "5138614\t526\t1\t0.56\n" +
            "5577273\t526\t7\tNULL\n" +
            "5639423\t526\t5\t0.10\n" +
            "5102551\t526\t12\t0.30\n" +
            "5639422\t526\t4\t0.62\n" +
            "6129516\t526\t5\tNULL\n" +
            "5395096\t526\t3\t0.08\n";

    TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("NFC Tagging");

        setContentView(R.layout.activity_main);
        tableLayout = (TableLayout) findViewById(R.id.list_table);
        tableLayout.setVisibility(View.GONE);

        initViews();
        initNFC();

//        actionAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//        });
//
//        actionMemo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    private void initViews() {

        mTvMessage = (TextView) findViewById(R.id.tv_message);
        mBtPopulate = (Button) findViewById(R.id.btn_populate);
//        mBtWrite = (Button) findViewById(R.id.btn_write);
//        mBtRead = (Button) findViewById(R.id.btn_read);
        actionAdd = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_a);
        actionMemo = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_b);

//        mBtWrite.setOnClickListener(view -> showWriteFragment());
//        mBtRead.setOnClickListener(view -> showReadFragment());
//        actionAdd.setOnClickListener(view -> showWriteFragment());
//        actionMemo.setOnClickListener(view -> showReadFragment());

        actionAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showWriteFragment();
            }
        });

        actionMemo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tableLayout.setVisibility(View.GONE);
                showReadFragment();
            }
        });

        mBtPopulate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                Perform action on click
//                orderDetails = "This is a sample order";
//                mTvMessage.setText(orderDetails);
                tableLayout.setVisibility(View.VISIBLE);

            }
        });
    }


    private void initNFC(){

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    private void showWriteFragment() {

        isWrite = true;

        mNfcWriteFragment = (NFCWriteFragment) getFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);

        if (mNfcWriteFragment == null) {

            mNfcWriteFragment = NFCWriteFragment.newInstance();
        }
        mNfcWriteFragment.show(getFragmentManager(),NFCWriteFragment.TAG);

        tableLayout.setVisibility(View.GONE);
    }

    private void showReadFragment() {

        mNfcReadFragment = (NFCReadFragment) getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);

        if (mNfcReadFragment == null) {

            mNfcReadFragment = NFCReadFragment.newInstance();
        }
        mNfcReadFragment.show(getFragmentManager(),NFCReadFragment.TAG);

    }

    @Override
    public void onDialogDisplayed() {

        isDialogDisplayed = true;
    }

    @Override
    public void onDialogDismissed() {

        isDialogDisplayed = false;
        isWrite = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected,tagDetected,ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if(mNfcAdapter!= null)
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNfcAdapter!= null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.d(TAG, tag.toString());
        Log.d(TAG, "onNewIntent: "+intent.getAction());

        if(tag != null) {
            Toast.makeText(this, getString(R.string.message_tag_detected), Toast.LENGTH_SHORT).show();
            Ndef ndef = Ndef.get(tag);
            // System.out.println(ndef);

            if (isDialogDisplayed) {

                if (isWrite) {

//                    String messageToWrite = mTvMessage.getText().toString();
                    String messageToWrite = orderDetails;
                    mNfcWriteFragment = (NFCWriteFragment) getFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);
                    mNfcWriteFragment.onNfcDetected(ndef,messageToWrite);

                } else {

                    mNfcReadFragment = (NFCReadFragment)getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);
                    mNfcReadFragment.onNfcDetected(ndef);
                    tableLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
