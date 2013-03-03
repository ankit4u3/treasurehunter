
package org.treasurehunter;

import org.treasurehunter.activity.main.MainActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class Splash extends Activity {
    private ImageButton btnstartnow;

    /**
     * @see android.app.Activity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO Put your code here

        setContentView(R.layout.activity_home);
        this.btnstartnow = (ImageButton)findViewById(R.id.btnstartnow);
        this.btnstartnow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View target) {

                Toast.makeText(getApplicationContext(), "Click Pressed", Toast.LENGTH_SHORT).show();
                // Calls another activity, by name, without passing data
                //
                // Intent hackbookIntent = new Intent().setClass(Splash.this,
                // GameTeamActivity.class);
                // startActivity(hackbookIntent);

            }
        });
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                finish();
                // Starts a service (task to be accomplished in the background,
                // without UI)
                // The class employing the snippet code must implement
                // ServiceConnection
                Intent iServ = new Intent();
                iServ.setClass(getBaseContext(), AppService.class); // TODO
                                                                    // Replace
                                                                    // 'ServiceName'
                                                                    // with the
                                                                    // class
                                                                    // name for
                                                                    // your
                                                                    // Service
                startService(iServ);
                Intent hackbookIntent = new Intent().setClass(Splash.this, MainActivity.class);
                startActivity(hackbookIntent);
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 1000);
    }

}
