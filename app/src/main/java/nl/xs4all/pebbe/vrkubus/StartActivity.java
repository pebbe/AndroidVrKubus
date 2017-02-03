package nl.xs4all.pebbe.vrkubus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    public void run(View view) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
