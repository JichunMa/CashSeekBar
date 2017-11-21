package com.example.majichun.cashseekbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

  CashSeekBar mineSeekBar;

  static final String TAG = "MainActivity";

  float[] displayValues = {200.00f, 300.00f, 400.00f, 500.00f, 600.00f, 700.00f, 800.00f, 900.00f, 1000.00f,
      1100.00f, 1200.00f, 1300.00f, 1400.00f, 1500.00f, 1600.00f, 1700.00f, 1800.00f, 1900.00f, 2000.00f,
      2100.00f, 2200.00f, 2300.00f, 2400.00f, 2500.00f, 2600.00f, 2700.00f, 2800.00f, 2900.00f, 3000.00f};


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mineSeekBar = findViewById(R.id.mineSeekBar);
    mineSeekBar.setOnProgressChangeListener(new CashSeekBar.OnProgressChangeListener() {
      @Override
      public String onProgressChanged(int progress) {
        //Log.d(TAG, "progress: " + progress);
        float fProgress = progress / 100.0f;
        int index = (int) (fProgress * (displayValues.length-1));
        return String.format("%.2f", displayValues[index]);
      }
    });

    //mineSeekBar.setProgress(100);

  }
}
