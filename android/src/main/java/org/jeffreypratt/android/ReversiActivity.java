package org.jeffreypratt.android;

import playn.android.GameActivity;

import org.jeffreypratt.core.Reversi;

public class ReversiActivity extends GameActivity {

  @Override public void main () {
    new Reversi(platform());
  }
}
