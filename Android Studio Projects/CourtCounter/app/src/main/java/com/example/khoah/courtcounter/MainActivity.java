package com.example.khoah.courtcounter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    int scoreA = 0;
    int scoreB = 0;

    public void displayForTeamA(int score) {
        TextView scoreView = (TextView) findViewById(R.id.team_a_score);
        scoreView.setText(String.valueOf(score));
    }

    public void addThreeForTeamA(View v) {
        scoreA += 3;
        displayForTeamA(scoreA);
    }

    public void addTwoForTeamA(View v) {
        scoreA += 2;
        displayForTeamA(scoreA);
    }

    public void addOneForTeamA(View v) {
        scoreA += 1;
        displayForTeamA(scoreA);
    }

    public void displayForTeamB(int score) {
        TextView scoreView = (TextView) findViewById(R.id.team_b_score);
        scoreView.setText(String.valueOf(score));
    }

    public void addThreeForTeamB(View v) {
        scoreB += 3;
        displayForTeamB(scoreB);
    }

    public void addTwoForTeamB(View v) {
        scoreB += 2;
        displayForTeamB(scoreB);
    }

    public void addOneForTeamB(View v) {
        scoreB += 1;
        displayForTeamB(scoreB);
    }

    public void reset(View v) {
        scoreA = 0;
        scoreB = 0;
        displayForTeamA(scoreA);
        displayForTeamB(scoreB);
    }
}
