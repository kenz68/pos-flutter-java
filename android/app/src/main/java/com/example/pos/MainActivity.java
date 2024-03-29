package com.example.pos;

import io.flutter.embedding.android.FlutterActivity;
import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.seuic.scankey.IKeyEventCallback;
import com.seuic.scankey.ScanKeyService;
import com.seuic.scanner.DecodeInfo;
import com.seuic.scanner.DecodeInfoCallBack;
import com.seuic.scanner.Scanner;
import com.seuic.scanner.ScannerFactory;
import com.seuic.scanner.ScannerKey;

public class MainActivity extends FlutterActivity implements DecodeInfoCallBack {
    private static final String CHANNEL = "samples.flutter.dev/battery";
    private static final String TAG = "Kenz";
    MethodChannel.Result mResult;
    public static final String STREAM = "samples.flutter.dev/stream";

    EventChannel.EventSink mEvents;
    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            // This method is invoked on the main thread.
                            if (call.method.equals("getBatteryLevel")) {
                                mResult = result;
                                initScanner();
                            } else {
                                result.notImplemented();
                            }
                        });
        new EventChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), STREAM).setStreamHandler(
                new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object args, EventChannel.EventSink events) {
                        Log.w(TAG, "adding listener");
                        mEvents = events;
                    }

                    @Override
                    public void onCancel(Object args) {
                        Log.w(TAG, "cancelling listener");
                    }
                }
        );
    }

    Scanner scanner;
    private void initScanner() {
        scanner = ScannerFactory.getScanner(this);
        boolean open = scanner.open();
        scanner.setDecodeInfoCallBack(this);
        scanner.enable();
        mScanKeyService.registerCallback(mCallback, null);
    }

    private void destroyScanner() {
        mScanKeyService.unregisterCallback(mCallback);
        ScannerKey.close();
        scanner.setDecodeInfoCallBack(null);
        scanner.setVideoCallBack(null);
        scanner.close();
        scanner = null;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Call the service when the program is opened, you can call only once, do not need to call in each activity

        //Each activitys to register the receiver to receive scan service passed over the bar code
        // use custom broadcast receiver (you can define action yourself)
        // Registering and unloading sinks is recommended on onResume and onPause
    }

    @Override
    protected void onResume() {
        super.onResume();



    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDecodeComplete(DecodeInfo info) {
        Log.d(TAG, "onDecodeComplete: bar_code:" + info.barcode + ", code_type:" + info.codetype + ", length:" + info.length);
        //mResult.success(info.barcode);
        mEvents.success(info.barcode);
    }
}