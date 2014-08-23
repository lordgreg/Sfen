package gpapez.sfen;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class AboutActivity extends Activity {

    private int mClickedSfen = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_cancel) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void onClickAboutDonate(View v) {

        Intent browserIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=gregorp%40gmail%2ecom&lc=SI&item_name=Sfen&item_number=sfen&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted"));
        startActivity(browserIntent);

    }

    public void onClickAboutFacebook(View v) {

        Intent browserIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.facebook.com/sfenapp"));
        startActivity(browserIntent);

    }

    public void onClickAboutGooglePlus(View v) {

        Intent browserIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://plus.google.com/u/0/b/105320324349424689518/105320324349424689518/posts"));
        startActivity(browserIntent);

    }

    public void onClickAboutTwitter(View v) {

        Intent browserIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://twitter.com/gpapez"));
        startActivity(browserIntent);

    }

    public void onClickAboutSfen(View v) {

        mClickedSfen++;


        if (mClickedSfen > 5) {

            MediaPlayer mp = MediaPlayer.create(this, R.raw.sfen_sound);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }

            });
            mp.start();


        }



    }




}
