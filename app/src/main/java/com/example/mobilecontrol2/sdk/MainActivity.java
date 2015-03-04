/*
 * Copyright (c) 2015 ESU electronic solutions ulm GmbH & Co KG
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE.txt file for details.
 */

package com.example.mobilecontrol2.sdk;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import eu.esu.mobilecontrol2.sdk.MobileControl2;
import eu.esu.mobilecontrol2.sdk.OnThrottleListener;
import eu.esu.mobilecontrol2.sdk.ThrottleFragment;

/**
 * This class demonstrates how to use the Mobile Control II hardware features.
 */
public class MainActivity extends FragmentActivity {

    private SeekBar mSeekBar;
    private TextView mThrottleButtonState;

    /**
     * The fragment that interacts with the Mobile Control II Throttle service.
     */
    private ThrottleFragment mThrottleFragment;

    /**
     * Callback interface for throttle events.
     */
    private OnThrottleListener mOnThrottleListener = new OnThrottleListener() {
        @Override
        public void onButtonDown() {
            mThrottleButtonState.setText("DOWN");
        }

        @Override
        public void onButtonUp() {
            mThrottleButtonState.setText("UP");
        }

        @Override
        public void onPositionChanged(int position) {
            mSeekBar.setProgress(position);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // Don't move throttle when changed from the motor event.
            if (fromUser) {
                mThrottleFragment.moveThrottle(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    /**
     * Listener for the LED Buttons.
     */
    private View.OnClickListener mOnLedButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.red_on:
                    MobileControl2.setLedState(MobileControl2.LED_RED, true);
                    break;
                case R.id.red_off:
                    MobileControl2.setLedState(MobileControl2.LED_RED, false);
                    break;
                case R.id.red_flash:
                    MobileControl2.setLedState(MobileControl2.LED_RED, 250, 250);
                    break;
                case R.id.green_on:
                    MobileControl2.setLedState(MobileControl2.LED_GREEN, true);
                    break;
                case R.id.green_off:
                    MobileControl2.setLedState(MobileControl2.LED_GREEN, false);
                    break;
                case R.id.green_flash:
                    MobileControl2.setLedState(MobileControl2.LED_GREEN, 250, 250);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // To use the throttle just add the fragment to the activity.
        mThrottleFragment = ThrottleFragment.newInstance();
        mThrottleFragment.setOnThrottleListener(mOnThrottleListener);
        getSupportFragmentManager().beginTransaction()
                .add(mThrottleFragment, "mc2:throttle")
                .commit();

        // Set up views
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setMax(126); // Set the throttle range.
        mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        mThrottleButtonState = (TextView) findViewById(R.id.textView);
        // Set text to default value. We will receive MSG_BUTTON_DOWN if the value changed.
        mThrottleButtonState.setText("UP");

        findViewById(R.id.red_on).setOnClickListener(mOnLedButtonClickListener);
        findViewById(R.id.red_off).setOnClickListener(mOnLedButtonClickListener);
        findViewById(R.id.red_flash).setOnClickListener(mOnLedButtonClickListener);
        findViewById(R.id.green_on).setOnClickListener(mOnLedButtonClickListener);
        findViewById(R.id.green_off).setOnClickListener(mOnLedButtonClickListener);
        findViewById(R.id.green_flash).setOnClickListener(mOnLedButtonClickListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case ThrottleFragment.KEYCODE_THROTTLE_WAKEUP:
                // Always ignore the wake up key. You must return true here to avoid further input key handling.
                return true;
            case MobileControl2.KEY_CODE_LEFT_TOP:
                showMessage("Top left");
                return true;
            case MobileControl2.KEY_CODE_LEFT_BOTTOM:
                showMessage("Bottom left");
                return true;
            case MobileControl2.KEY_CODE_RIGHT_TOP:
                showMessage("Top right");
                return true;
            case MobileControl2.KEY_CODE_RIGHT_BOTTOM:
                showMessage("Bottom right");
                return true;
            case MobileControl2.KEYCODE_STOP:
                showMessage("STOP");
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private void showMessage(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}
