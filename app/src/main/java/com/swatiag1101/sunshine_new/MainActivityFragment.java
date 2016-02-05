package com.swatiag1101.sunshine_new;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import com.swatiag1101.sunshine_new.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
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
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    ListView lv;
    private ForecastAdapter ad;
    URL url1;
    private static String url = "http://api.openweathermap.org/data/2.5/forecast/daily";
    private static String q = "q";
    private static String q_val = "734001";
    private static String mode = "mode";
    private static String mode_val = "json";
    private static String units = "units";
    private static String units_val = "metric";
    private static String cnt = "cnt";
    private static String cnt_val = "7";
    private static String appid = "appid";
    private static String appid_val = "65b305ff41d4562e0ba4344316d8a1b6";
    private static final int FORECAST_LOADER = 0;
    Uri builtUri;

    public MainActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> al = new ArrayList<String>();
        al.add("Today - Sunny - 88/63");
        al.add("Tommorrow - Foggy - 70/46");
        al.add("Weds - Cloudy - 72/63");
        al.add("Thurs - Rainy - 64/51");
        al.add("Fris - Foggy - 70/46");
        al.add("Sats - Sunny - 76/68");

        /*String locationSetting = Utility.getPreferredLocation(getActivity());
        Log.v("Location", locationSetting);

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);*/
      //  CursorLoader cur= (CursorLoader) onCreateLoader(FORECAST_LOADER,null);

      //  Cursor c= cur.loadInBackground();

        ad = new ForecastAdapter(getActivity(), null, 0);
        lv = (ListView) rootView.findViewById(R.id.listView);
       // ad = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.textView,al);

        lv.setAdapter(ad);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                    startActivity(intent);
                }
            }
        });
        //c.close();
        return rootView;


    }
    @Override
      public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather(){
     //   SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
      //  String location = sp.getString(getString(R.string.location), "734001");
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
    }
    Uri weatherForLocationUri;
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v("Inside onCreateLoader ","onCreateLoader");
        String locationSetting = Utility.getPreferredLocation(getActivity());

      // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                      locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),weatherForLocationUri,FORECAST_COLUMNS,null,
                        null,sortOrder);
    }

 @Override
   public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
     Log.v("Inside onLoadFinished ", "onLoadFinished");
     ad.swapCursor(cursor);
          //cursor.setNotificationUri(getActivity().getApplicationContext().getContentResolver(), weatherForLocationUri);
   }

   @Override
   public void onLoaderReset(Loader<Cursor> cursorLoader) {
       Log.v("Inside onLoaderReset ","onLoaderReset");
        ad.swapCursor(null);
   }

    public void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }
   /* public class AsyncTaskForTemp extends AsyncTask<String,Void,String[]> {

        private final String LOG_TAG = AsyncTaskForTemp.class.getSimpleName();
        ArrayList<String> al1;
        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
                builtUri = Uri.parse(url).buildUpon().appendQueryParameter(q,params[0]).appendQueryParameter(mode,mode_val)
                        .appendQueryParameter(units,units_val).appendQueryParameter(cnt,cnt_val).appendQueryParameter(appid,appid_val).build();

                url1 = new URL(builtUri.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url1.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }


                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e("MainActivityFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MainActivityFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                return getWeatherDataFromJson(forecastJsonStr,7);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
    /*    private String formatHighLows(double high, double low) {

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sp.getString(getString(R.string.temp),getString(R.string.temp_default));

            if(!unitType.equals(getString(R.string.temp_default))){
                high = (high*1.8)+32;
                low = (low * 1.8)+32;
            }
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
     /*   private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }

      /*  @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);

            al1 = new ArrayList<String>(Arrays.asList(s));
            //ad  = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.textView,al1);
            lv = (ListView) getActivity().findViewById(R.id.listView);
            lv.setAdapter(ad);
        }
    }*/

}
