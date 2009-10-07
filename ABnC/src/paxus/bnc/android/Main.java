package paxus.bnc.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class Main extends Activity {
    private static final int COLUMNS = 9;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        LinearLayout l = (LinearLayout) findViewById(R.id.LinearLayout01);
        for (int i = 0; i < COLUMNS; i++) {
        	getLayoutInflater().inflate(R.layout.char_view, l);
        }
        
        l = (LinearLayout) findViewById(R.id.LinearLayout02);
        for (int i = 0; i < COLUMNS; i++) {
        	getLayoutInflater().inflate(R.layout.char_view, l);
        }
        
        l = (LinearLayout) findViewById(R.id.LinearLayout03);
        for (int i = 0; i < COLUMNS; i++) {
        	getLayoutInflater().inflate(R.layout.char_view, l);
        }
        
        l = (LinearLayout) findViewById(R.id.LinearLayout04);
        for (int i = 0; i < COLUMNS; i++) {
        	getLayoutInflater().inflate(R.layout.char_view, l);
        }
        
        l = (LinearLayout) findViewById(R.id.LinearLayout05);
        for (int i = 0; i < COLUMNS; i++) {
        	getLayoutInflater().inflate(R.layout.char_view, l);
        }
        
        l = (LinearLayout) findViewById(R.id.LinearLayout06);
        for (int i = 0; i < COLUMNS; i++) {
        	getLayoutInflater().inflate(R.layout.char_view, l);
        }
        
        l = (LinearLayout) findViewById(R.id.LinearLayout07);
        for (int i = 0; i < COLUMNS; i++) {
        	getLayoutInflater().inflate(R.layout.char_view, l);
        }
        
        l = (LinearLayout) findViewById(R.id.LinearLayout08);
        for (int i = 0; i < COLUMNS; i++) {
        	getLayoutInflater().inflate(R.layout.char_view, l);
        }
        
        l = (LinearLayout) findViewById(R.id.LinearLayout09);
        for (int i = 0; i < COLUMNS; i++) {
        	getLayoutInflater().inflate(R.layout.char_view, l);
        }
    
    
    }
    
}