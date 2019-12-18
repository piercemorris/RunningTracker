package com.example.runningtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.runningtracker.Service.ICallback;
import com.example.runningtracker.Service.TrackerService;
import com.example.runningtracker.Types.MovementState;

public class MainActivity extends AppCompatActivity {

  private TrackerService.MyBinder runningService = null;
  private TextView length;
  private TextView timer;
  private TextView state;
  private Button trackButton;

  private boolean isTracking = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    length = findViewById(R.id.textView2);
    timer = findViewById(R.id.textView3);
    state = findViewById(R.id.textView4);
    state.setText("Stationary");
    trackButton = findViewById(R.id.button);

    this.startService(new Intent(this, TrackerService.class));
    this.bindService(new Intent(this, TrackerService.class), serviceConnection, Context.BIND_AUTO_CREATE);
  }

  private ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      Log.d("runningTracker", "onServiceConnected: " + componentName.toShortString());
      runningService = (TrackerService.MyBinder) iBinder;
      runningService.registerCallback(callback);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
      Log.d("runningTracker", "onServiceDisconnected: " + componentName.toShortString());
      runningService.unregisterCallback(callback);
      runningService = null;
    }
  };

  ICallback callback = new ICallback() {
    @Override
    public void runningEvent(final Double locationSum, final int Minutes, final int Seconds, final int movementState) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          length.setText("" + String.format("%4.2f", locationSum) +  "m");
          timer.setText("" + Minutes + ":"
                + String.format("%02d", Seconds));

          switch(movementState) {
            case 1:
              state.setText("Walking");
              break;
            case 2:
              state.setText("Jogging");
              break;
            case 3:
              state.setText("Running");
              break;
            default:
              state.setText("Stationary");
              break;
          }
        }
      });
    }
  };

  public void onClickTrack(View view) {
    Log.d("runningTracker", "onClickTrack: ");
    if (!isTracking) {
      runningService.startTracker();
      trackButton.setText("Finish");
      isTracking = true;
    } else {
      runningService.stopTracker();
      trackButton.setText("Track");
      isTracking = false;
    }
  }
}
