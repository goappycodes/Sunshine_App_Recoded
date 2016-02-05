package com.swatiag1101.sunshine_new;

/**
 * Created by Swati Agarwal on 04-02-2016.
 */

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.swatiag1101.sunshine_new.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int my_loader_id = 0;
    TextView tv;
    private static String hashTag = "#SunshineApp";
    private String forecast;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    ShareActionProvider sap;
    public PlaceholderFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        sap = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if(sap!=null){
            sap.setShareIntent( createShareForecastIntent());
        }else {
            Log.v("Share Error", "ShareActionProvide is null");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        Intent intent = getActivity().getIntent();
          /*  if (intent != null) {
                forecast = intent.getDataString();
              }

            if (null != forecast) {
                ((TextView) rootView.findViewById(R.id.textView2))
                        .setText(forecast);
            }
*/
        //CursorLoader cur= (CursorLoader) onCreateLoader(my_loader_id,null);

        return rootView;
    }

    private Intent createShareForecastIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,forecast+hashTag);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v("onCreateLoader", "In onCreateLoader");
        Intent i =getActivity().getIntent();
        if(i==null){
            Log.v("test123","no error");
            return null;
        }
        return new CursorLoader(getActivity(),i.getData(),FORECAST_COLUMNS,null,null,null);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(my_loader_id, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v("onLoadFinished", "In onLoadFinished");
        if(!data.moveToFirst()){
            return;
        }

        String date = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
        String desc = data.getString(COL_WEATHER_DESC);
        boolean isMetric = Utility.isMetric(getActivity());
        String high = Utility.formatTemperature(data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(data.getDouble(COL_WEATHER_MIN_TEMP),isMetric);
        forecast = String.format("%s - %s - %s/%s",date,desc,high,low);
        Log.v("Test",forecast);
        TextView tv = (TextView) getView().findViewById(R.id.textView2);

        tv.setText(forecast);
        if(sap !=null){
            sap.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}