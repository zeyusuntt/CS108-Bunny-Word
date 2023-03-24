package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_you_lose);
        Bundle bundle = getIntent().getExtras();
        String text = bundle.getString("text");
        TextView pointsNum = findViewById(R.id.points_num);
        pointsNum.setText(String.format("%s", text));
    }


    public void OnBackGame(View view) {
        Intent intent = new Intent(this, GameMode.class);
        startActivity(intent);
    }


    public void OnMainPage(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
