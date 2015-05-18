/*
 * Copyright (c) 2015 ESU electronic solutions ulm GmbH & Co KG
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.example.mobilecontrol2.sdk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import eu.esu.mobilecontrol2.sdk.MobileControl2;
import eu.esu.mobilecontrol2.sdk.StopButtonFragment;
import eu.esu.mobilecontrol2.sdk.ThrottleFragment;
import eu.esu.mobilecontrol2.sdk.ThrottleScale;

/**
 * This class demonstrates how to use the Mobile Control II hardware.
 */
public class MainActivity extends AppCompatActivity {

    private SeekBar mSeekBar;
    private TextView mThrottlePosition;
    private TextView mThrottleStep;
    private TextView mThrottleButtonState;
    private TextView mStopButtonState;

    private ThrottleScale mThrottleScale = new ThrottleScale(10, 15);

    /**
     * The fragment that interacts with the Mobile Control II Throttle service.
     */
    private ThrottleFragment mThrottleFragment;

    /**
     * Callback interface for throttle events.
     */
    private ThrottleFragment.OnThrottleListener mOnThrottleListener = new ThrottleFragment.OnThrottleListener() {
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
            mSeekBar.setProgress(mThrottleScale.positionToStep(position));
            mThrottlePosition.setText(Integer.toString(position));
        }
    };

    /**
     * Callback interface for the stop button.
     */
    private StopButtonFragment.OnStopButtonListener mOnStopButtonListener = new StopButtonFragment.OnStopButtonListener() {
        @Override
        public void onStopButtonDown() {
            mStopButtonState.setText("DOWN");
        }

        @Override
        public void onStopButtonUp() {
            mStopButtonState.setText("UP");
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // Don't move throttle when changed from motor event.
            int position = mThrottleScale.stepToPosition(progress);

            if (fromUser) {
                mThrottleFragment.moveThrottle(position);
                mThrottlePosition.setText(Integer.toString(position));
            }

            mThrottleStep.setText(Integer.toString(progress));
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

    private AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    mThrottleScale = new ThrottleScale(10, 15);
                    mSeekBar.setMax(14);
                    break;
                case 1:
                    mThrottleScale = new ThrottleScale(10, 29);
                    mSeekBar.setMax(28);
                    break;
                case 2:
                    mThrottleScale = new ThrottleScale(10, 127);
                    mSeekBar.setMax(126);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case ThrottleFragment.KEYCODE_THROTTLE_WAKEUP:
                // Ignore the wake up key. You must return true here to avoid further input key handling.
                return true;
            case MobileControl2.KEYCODE_TOP_LEFT:
                showMessage("Top left");
                return true;
            case MobileControl2.KEYCODE_BOTTOM_LEFT:
                showMessage("Bottom left");
                return true;
            case MobileControl2.KEYCODE_TOP_RIGHT:
                showMessage("Top right");
                return true;
            case MobileControl2.KEYCODE_BOTTOM_RIGHT:
                showMessage("Bottom right");
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // To use the throttle just add the fragment to the activity.
        mThrottleFragment = ThrottleFragment.newInstance(1);
        mThrottleFragment.setOnThrottleListener(mOnThrottleListener);

        StopButtonFragment stopButtonFragment = StopButtonFragment.newInstance();
        stopButtonFragment.setOnStopButtonListener(mOnStopButtonListener);

        getSupportFragmentManager().beginTransaction()
                .add(mThrottleFragment, "mc2:throttle")
                .add(stopButtonFragment, "mc2:stopKey")
                .commit();

        // Set up views
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setMax(14); // Maximum of mThrottleScale
        mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        mThrottlePosition = (TextView) findViewById(R.id.text_position);
        mThrottlePosition.setText("0");

        mThrottleStep = (TextView) findViewById(R.id.text_step);
        mThrottlePosition.setText("0");

        mThrottleButtonState = (TextView) findViewById(R.id.text_button);
        // Set text to default value. We will receive MSG_BUTTON_DOWN if the value changed.
        mThrottleButtonState.setText("UP");

        mStopButtonState = (TextView) findViewById(R.id.text_stop);
        mStopButtonState.setText("UP");

        Spinner spinnerSteps = (Spinner) findViewById(R.id.spinner_steps);
        spinnerSteps.setOnItemSelectedListener(mOnItemSelectedListener);

        findViewById(R.id.red_on).setOnClickListener(mOnLedButtonClickListener);
        findViewById(R.id.red_off).setOnClickListener(mOnLedButtonClickListener);
        findViewById(R.id.red_flash).setOnClickListener(mOnLedButtonClickListener);
        findViewById(R.id.green_on).setOnClickListener(mOnLedButtonClickListener);
        findViewById(R.id.green_off).setOnClickListener(mOnLedButtonClickListener);
        findViewById(R.id.green_flash).setOnClickListener(mOnLedButtonClickListener);
    }

    private void showMessage(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
