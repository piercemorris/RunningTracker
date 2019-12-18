package com.example.runningtracker.Types;

import android.util.Log;

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// Definition of movement state
public class MovementState {
  public static final int STATIONARY = 0;
  public static final int WALKING = 1;
  public static final int JOGGING = 2;
  public static final int RUNNING = 3;

  public MovementState(@Movement int state) {
    System.out.println("Movement :" + state);
  }

  @IntDef({STATIONARY, WALKING, JOGGING, RUNNING})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Movement {
  }
}
