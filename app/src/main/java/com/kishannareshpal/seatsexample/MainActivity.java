package com.kishannareshpal.seatsexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.kishannareshpal.seats.SeatsView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SeatsView seatsView = findViewById(R.id.seatsview);

        String patt = "0002,11,11011,11011,11011,11011,11011,11011,11011,11011,11011,11011";
        List<String> resevations = Arrays.asList("5");
        seatsView.show(patt, resevations, new SeatsView.OnSeatClickListener() {
            @Override
            public void OnSeatClick(int seatId, boolean isSelected, List<String> selectedSeatsList) {

            }
        });
        Log.d("oisjdf", String.valueOf(seatsView.getSelectedSeats()));

    }

}
