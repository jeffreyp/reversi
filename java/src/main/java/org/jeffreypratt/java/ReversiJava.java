package org.jeffreypratt.java;

import playn.java.LWJGLPlatform;

import org.jeffreypratt.core.Reversi;

public class ReversiJava {

  public static void main (String[] args) {
    LWJGLPlatform.Config config = new LWJGLPlatform.Config();
    // use config to customize the Java platform, if needed
    LWJGLPlatform plat = new LWJGLPlatform(config);
    new Reversi(plat);
    plat.start();
  }
}
