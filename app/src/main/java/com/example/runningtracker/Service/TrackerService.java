package com.example.runningtracker.Service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.runningtracker.Types.MovementState;

public class TrackerService extends Service implements LocationListener {

  // Constants
  private final int MIN_TIME = 1000;
  private final int MIN_DISTANCE = 5;
  private final double WALKING = 1.4;
  private final double JOGGING = 2.7;

  // Variable declarations & initialisations
  RemoteCallbackList<MyBinder> remoteCallbackList = new RemoteCallbackList<MyBinder>();
  private final IBinder binder = new MyBinder();
  private LocationManager locationManager = null;
  protected RunningDuration runningDuration;
  public int movementState = 0;
  public long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
  public int Seconds, Minutes = 0;
  public boolean running = true;
  public Location previousLocation = null;
  public double locationSum = 0.00;
  public long timeStamp = 0L;

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d("runningTracker", "onCreate: service");
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  @Override
  public void onLocationChanged(Location location) {
    Log.d("runningTracker", "onLocationChanged: tracking location");
    if (previousLocation == null) {
      previousLocation = location;
      timeStamp = SystemClock.elapsedRealtimeNanos();
    } else {

      // Calculate speed
      double distanceTravelled = previousLocation.distanceTo(location);
      long currentTime = location.getElapsedRealtimeNanos();

      Log.d("runningTracker", "onLocationChanged: location speed = " + location.getSpeed());
      Log.d("runningTracker", "onLocationChanged: location time = " + ((currentTime - timeStamp)/1000000000));

      float speed = location.getSpeed();
      Log.d("runningTracker", "onLocationChanged: speed = " + speed);
      // Identify movement state
      if (speed <= WALKING) {
        movementState = MovementState.WALKING;
      } else if (speed > WALKING && speed <= JOGGING) {
        movementState = MovementState.JOGGING;
      } else {
        movementState = MovementState.RUNNING;
      }

      // Update data
      locationSum += distanceTravelled;
      previousLocation = location;
    }
  }

  @Override
  public void onStatusChanged(String s, int i, Bundle bundle) {

  }

  @Override
  public void onProviderEnabled(String s) {

  }

  @Override
  public void onProviderDisabled(String s) {

  }

  public void subscribeToLocationChanges() {
    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    running = true;

    if (ContextCompat.checkSelfPermission(TrackerService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      running = false;
    } else {

      try {
        Log.d("runningTracker", "subscribeToLocationChanges: tracking");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        runningDuration = new RunningDuration();
      } catch(SecurityException e) {
        Log.d("runningTracker", "run: " + e.toString());
      }
    }
  }

  public void unsubscribeToLocationChanges() {
    locationManager.removeUpdates(this);
    locationSum = 0.00;
    MillisecondTime = 0L;
    StartTime = 0L;
    UpdateTime = 0L;
    TimeBuff = 0L;
    Seconds = 0;
    Minutes = 0;
    previousLocation = null;
    running = false;
    movementState = 0;
    doCallbacks(locationSum);
  }

  protected class RunningDuration extends Thread implements Runnable {

    public RunningDuration() {
      Log.d("runningTracker", "RunningDuration: started");
      this.start();
    }

    public void run() {
      Log.d("runningTracker", "run: thread");
      StartTime = SystemClock.uptimeMillis();

      while(running) {
        MillisecondTime = SystemClock.uptimeMillis() - StartTime;
        UpdateTime = TimeBuff + MillisecondTime;
        Seconds = (int) (UpdateTime / 1000);
        Minutes = Seconds / 60;
        Seconds = Seconds % 60;
        //doCallbacks(locationSum);

        /*
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          Log.d("runningTracker", "run: " + e.toString());
        }

         */
      }
    }
  }

  public void doCallbacks(Double totalTrackedLength) {
    final int n = remoteCallbackList.beginBroadcast();
    for (int i=0; i<n; i++) {
      remoteCallbackList.getBroadcastItem(i).callback.runningEvent(totalTrackedLength, Minutes, Seconds, movementState);
    }
    remoteCallbackList.finishBroadcast();
  }


  public class MyBinder extends Binder implements IInterface {

    @Override
    public IBinder asBinder() {
      return this;
    }

    public void startTracker() {
      subscribeToLocationChanges();
    }

    public void stopTracker() {
      unsubscribeToLocationChanges();
    }

    public void registerCallback(ICallback callback) {
      this.callback = callback;
      remoteCallbackList.register(MyBinder.this);
    }

    public void unregisterCallback(ICallback callback) {
      remoteCallbackList.unregister(MyBinder.this);
    }

    ICallback callback;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // TODO Auto-generated method stub
    Log.d("runningTracker", "onStartCommand: service");
    return TrackerService.START_STICKY;
  }
}
