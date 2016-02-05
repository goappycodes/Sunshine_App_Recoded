package com.swatiag1101.sunshine_new;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;

public class MainActivity extends ActionBarActivity {

    private String mlocation;
    private static String FORECASTFRAGMENT_TAG = "FFTag";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment, new MainActivityFragment(),FORECASTFRAGMENT_TAG )
                    .commit();
        }
        openPrefferedLocationMap();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }else{
            openPrefferedLocationMap();
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPrefferedLocationMap(){
       // SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
      //  mlocation = sp.getString(getString(R.string.location), "734001");
        String mlocation = Utility.getPreferredLocation(this);
        Uri geolocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",mlocation).build();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(geolocation);
        if(i.resolveActivity(getPackageManager())!=null){
            startActivity(i);
        }else{
            Log.e("Error in map",mlocation);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation( this );
        if(location != null && !location.equals(mlocation)){
            MainActivityFragment ff = (MainActivityFragment) getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
            if ( null != ff ) {
                ff.onLocationChanged();
            }
            mlocation = location;
        }
    }
}
