package com.ctrl.supera.locationrecorder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


public class startup extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        /* Startup logo animation operation */
        ImageView view = (ImageView) findViewById(R.id.startup_logo);
        ObjectAnimator animLogo = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        animLogo.setDuration(2000);
        animLogo.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                Intent intent = new Intent(getApplicationContext(), main.class);
                startActivity(intent);
                finish();
            }
        });
        animLogo.start();

        /* Startup text animation operation */
        TextView startupText =  (TextView) findViewById(R.id.startup_text);
        ObjectAnimator animText = ObjectAnimator.ofFloat(startupText, "alpha", 0f, 1f);
        animText.setDuration(2000);
        animText.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_startup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
