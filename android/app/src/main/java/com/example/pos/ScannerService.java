package com.example.pos;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.seuic.scankey.IKeyEventCallback;
import com.seuic.scankey.ScanKeyService;
import com.seuic.scanner.DecodeInfo;
import com.seuic.scanner.DecodeInfoCallBack;
import com.seuic.scanner.Scanner;
import com.seuic.scanner.ScannerFactory;
import com.seuic.scanner.ScannerKey;

@SuppressWarnings("unused")
public class ScannerService extends Service implements DecodeInfoCallBack {
    static final String TAG = "Kenz";
    Scanner scanner;
    private static  MainActivity mcontext = null;
    private boolean mScanRunning = false;

    private  void log(String  string){
        Log.i(TAG, string);
    }

    public static  void MyService(Context context){
        mcontext = (MainActivity)context;
    }

    private final ScanKeyService mScanKeyService = ScanKeyService.getInstance();
    private final IKeyEventCallback mCallback = new IKeyEventCallback.Stub() {
        @Override
        public void onKeyDown(int keyCode) throws RemoteException {
            Log.d(TAG, "onKeyDown: keyCode=" + keyCode);
            if (scanner != null) {
                scanner.startScan();
            }
        }

        @Override
        public void onKeyUp(int keyCode) throws RemoteException {
            Log.d(TAG, "onKeyUp: keyCode=" + keyCode);
            if (scanner != null) {
                scanner.stopScan();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        scanner = ScannerFactory.getScanner(this);
        boolean open = scanner.open();
        scanner.setDecodeInfoCallBack(this);
        scanner.enable();
        mScanKeyService.registerCallback(mCallback, null);
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mScanRunning = false;
        mScanKeyService.unregisterCallback(mCallback);
        ScannerKey.close();
        scanner.setDecodeInfoCallBack(null);
        scanner.setVideoCallBack(null);
        scanner.close();
        scanner = null;
        super.onDestroy();
    }

    public static final String BAR_CODE = "barcode";
    public static final String CODE_TYPE = "codetype";
    public static final String LENGTH = "length";

    // this is a custom broadcast receiver action
    public static final String ACTION = "seuic.android.scanner.scannertestreciever";

    @Override
    public void onDecodeComplete(DecodeInfo info) {
        Log.d(TAG, "onDecodeComplete: bar_code:" + info.barcode + ", code_type:" + info.codetype + ", length:" + info.length);
        Intent intent = new Intent(ACTION);
        Bundle bundle = new Bundle();
        bundle.putString(BAR_CODE, info.barcode);
        bundle.putString(CODE_TYPE, info.codetype);
        bundle.putInt(LENGTH, info.length);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }
}