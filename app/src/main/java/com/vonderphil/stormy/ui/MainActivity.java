package com.vonderphil.stormy.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vonderphil.stormy.R;
import com.vonderphil.stormy.weather.Current;
import com.vonderphil.stormy.weather.Day;
import com.vonderphil.stormy.weather.Forecast;
import com.vonderphil.stormy.weather.Hour;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";

    private String mApiKey = "443b00e6a5620995135f90d93503480f";
    private double mLatitude = 37.8267;
    private double mLongtitude = -122.423;
    private String mForecastURL = "https://api.forecast.io/forecast/" + mApiKey + "/" +mLatitude+ "," + mLongtitude;

    //Instantiate Current java class. To be used when the data retrieval is successful.
    private Forecast mForecast;

    //Butterknife Inject Views
    @InjectView(R.id.timeLabel) TextView mTimeLabel;
    @InjectView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @InjectView(R.id.humidityValue) TextView mHumidityValue;
    @InjectView(R.id.precipValue) TextView mPrecipValue;
    @InjectView(R.id.summaryLabel) TextView mSummaryLabel;
    @InjectView(R.id.iconImageView) ImageView mIconImageView;
    @InjectView(R.id.refreshImageView) ImageView mRefreshImageView;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecast(mLatitude, mLongtitude);
            }
        });

        getForecast(mLatitude, mLongtitude);
    }

    private void getForecast(double latitude, double longtitude) {
        if(isNetworkAvailable()) {
            //Activity Method. Requires a Runnable object. Asynchronous updating of views through the main thread of execution.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toggleRefresh();
                }
            });
            //Instantiate OkHttpClient for first use
            OkHttpClient client = new OkHttpClient();
            //Call the request builder, then request the url and construct the request
            Request request = new Request.Builder()
                    .url(mForecastURL)
                    .build();
            //Set the variable to request the URL
            Call call = client.newCall(request);
            //enqueue the call request, requires a Callback object to call it back once it is ready.
            call.enqueue(new Callback() {
                //if the callback somehow fails
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                //if the callback is successful
                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    try {
                        //Inserts the data inside the jsonData variable.
                        String jsonData = response.body().string();
                        //Response returns a response body if it is successful, then convert it to a string.
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            //Set mCurrent's data through the getCurrentDetails method.
                            mForecast = parseForecastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            //Call a function to alert the user about the error. Calls a dialog.
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        }
        else {
            Toast.makeText(this, getString(R.string.toast_message), Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRefresh() {
        if(mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {
        Current current = mForecast.getCurrent();
        mTemperatureLabel.setText(current.getTemperature() + "");
        mTimeLabel.setText("At " + current.getFormattedTime() + " it will be");
        mHumidityValue.setText(current.getHumidity() + "");
        mPrecipValue.setText(current.getPrecipChance() + "%");
        mSummaryLabel.setText(current.getSummary());

        Drawable drawable = getResources().getDrawable(current.getIconId());

        mIconImageView.setImageDrawable(drawable);


    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));

        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];

        for(int i = 0; i < data.length(); i++) {
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();

            day.setSummary(jsonDay.getString("summary"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTime(jsonDay.getLong("time"));
            day.setTimezone(timezone);

            days[i] = day;

        }

        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[] hours = new Hour[data.length()];

        for(int i = 0; i < data.length(); i++) {
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();

            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimezone(timezone);

            hours[i] = hour;

        }

        return hours;
    }

    //throws sends the error handling responsibility to the part of the code that called this method.
    private Current getCurrentDetails(String jsonData) throws JSONException {
        //JSONObject is an android class used to manipulate and navigate through JSON Objects in Android.
        //JSONObject docs developer.android.com/reference/org/json/JSONObject.html
        JSONObject forecast = new JSONObject(jsonData);

        //Using getString
        String timezone = forecast.getString("timezone");

        //Using getJSONObject
        JSONObject currently = forecast.getJSONObject("currently");

        //Creating a Current object to store JSON values inside the object
        Current current = new Current();

        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);
        current.getFormattedTime();
        Log.d(TAG, current.getFormattedTime());
        return current;

    }

    private boolean isNetworkAvailable() {
        //Android class. http://developer.android.com/training/basics/network-ops/connecting.html#connection
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //Requires ACCESS_NETWORK_STATE permission in AndroidManifest
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        //networkInfo is null if there is no network and if it is connection to the network
        if(networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        //Instantiate the AlertDialogFragment
        AlertDialogFragment dialog = new AlertDialogFragment();
        //show the AlertDialogFragment
        dialog.show(getFragmentManager(), "error_dialog");
    }


    //ButterKnife inject onclick listener boilerplate.
    @OnClick (R.id.dailyButton)
    public void startDailyActivity(View view) {
        Intent intent = new Intent(this, DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST, mForecast.getDailyForecast());
        startActivity(intent);
    }

    @OnClick (R.id.hourlyButton)
    public void startHourlyActivity(View view) {
        Intent intent = new Intent(this, HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST, mForecast.getHourlyForecast());
        startActivity(intent);
    }
}
