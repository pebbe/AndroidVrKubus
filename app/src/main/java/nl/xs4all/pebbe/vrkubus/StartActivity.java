package nl.xs4all.pebbe.vrkubus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private int delay = 2;
    private int enhance = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        int i = getIntValue("mode");
        if (i < 0) {
            i = 0;
        }
        showHideDelayed(i == 0);
        showHideServer(i == 1);

        Spinner optMode = (Spinner) findViewById(R.id.opt_mode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.mode_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        optMode.setAdapter(adapter);
        optMode.setSelection(i);
        optMode.setOnItemSelectedListener(this);

        delay = getIntValue("delay");
        if (delay < 0) {
            delay = 2;
        }
        setDelay(delay);

        SeekBar optDelay = (SeekBar) findViewById(R.id.opt_delay);
        optDelay.setProgress(delay);
        optDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setDelay(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveValue("delay", ""+delay);
            }
        });

        enhance = getIntValue("enhance");
        if (enhance < 0) {
            enhance = 6;
        }
        setEnhance(enhance);

        SeekBar optEnhance = (SeekBar) findViewById(R.id.opt_enhance);
        optEnhance.setProgress(enhance);
        optEnhance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setEnhance(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveValue("enhance", ""+enhance);
            }
        });


        String s = getValue("address");
        TextView tv = (TextView) findViewById(R.id.opt_server_address);
        tv.setText(s);

        i = getIntValue("port");
        if (i > 0) {
            tv = (TextView) findViewById(R.id.opt_server_port);
            s = "" + i;
            tv.setText(s);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        saveServerValues();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        saveValue("mode", ""+position);
        showHideDelayed(position == 0);
        showHideServer(position == 1);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void setDelay(int i) {
        delay = i;
        TextView tv = (TextView) findViewById(R.id.val_delay);
        String s = String.format("%.1f", 0.5 * (float)i);
        tv.setText(s);
    }

    private void setEnhance(int i) {
        enhance = i;
        TextView tv = (TextView) findViewById(R.id.val_enhance);
        String s = String.format("%.1f", 0.5 * (float)(i-4));
        tv.setText(s);
    }

    private void saveServerValues() {
        TextView tv = (TextView) findViewById(R.id.opt_server_address);
        String s = tv.getText().toString().trim();
        saveValue("address", s);
        tv = (TextView) findViewById(R.id.opt_server_port);
        s = tv.getText().toString().trim();
        saveValue("port", s);
    }

    private void saveValue(String key, String value) {
        MyDBHandler handler = new MyDBHandler(this, null, null, 1);
        handler.addSetting(key, value);
    }

    private String getValue(String key) {
        MyDBHandler handler = new MyDBHandler(this, null, null, 1);
        return handler.findSetting(key);
    }

    private int getIntValue(String key) {
        String s = getValue(key);
        int i = -1;
        if (! s.equals("")) {
            i = Integer.parseInt(s, 10);
        }
        return i;
    }

    private void showHideDelayed(boolean show) {
        int v = show ? View.VISIBLE : View.INVISIBLE;
        TextView tv = (TextView) findViewById(R.id.lbl_delay);
        tv.setVisibility(v);
        tv = (TextView) findViewById(R.id.val_delay);
        tv.setVisibility(v);
        SeekBar sb = (SeekBar) findViewById(R.id.opt_delay);
        sb.setVisibility(v);
        tv = (TextView) findViewById(R.id.lbl_enhance);
        tv.setVisibility(v);
        tv = (TextView) findViewById(R.id.val_enhance);
        tv.setVisibility(v);
        sb = (SeekBar) findViewById(R.id.opt_enhance);
        sb.setVisibility(v);
   }

    private void showHideServer(boolean show) {
        int v = show ? View.VISIBLE : View.INVISIBLE;
        TextView tv = (TextView) findViewById(R.id.lbl_server_address);
        tv.setVisibility(v);
        tv = (TextView) findViewById(R.id.lbl_server_port);
        tv.setVisibility(v);
        EditText et = (EditText) findViewById(R.id.opt_server_address);
        et.setVisibility(v);
        et = (EditText) findViewById(R.id.opt_server_port);
        et.setVisibility(v);
    }

    public void run(View view) {
        saveServerValues();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
