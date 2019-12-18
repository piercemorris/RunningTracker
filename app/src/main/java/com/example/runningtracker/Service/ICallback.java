package com.example.runningtracker.Service;

public interface ICallback {
  public void runningEvent(Double locationSum, int Minutes, int Seconds, int MovementState);
}
