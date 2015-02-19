package pw.moter8.sunnyweather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.fabric.sdk.android.Fabric;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather;
    private CurrentLocation mCurrentLocation;
    private ColorWheel mColorWheel = new ColorWheel();
    public double latitude = 0;
    public double longitude = 0.0;
    public String givenLocation = "Ondara";

    @InjectView(R.id.locationLabel) EditText mLocationLabel;
    @InjectView(R.id.timeLabel )TextView mTimeLabel;
    @InjectView(R.id.temperatureLabel) TextView mTemperatureValue;
    @InjectView(R.id.humidityValue) TextView mHumidityValue;
    @InjectView(R.id.precipValue) TextView mPrecipValue;
    @InjectView(R.id.summaryLabel) TextView mSummaryLabel;
    @InjectView(R.id.iconImageView) ImageView mIconImageView;
    @InjectView(R.id.refreshButton) Button mButton;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this); // inject the above views

        final AppPreferences appPreferences = new AppPreferences(this);

        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        relativeLayout.setBackgroundColor(mColorWheel.getColor()); // random backgroundColor

        if (appPreferences.getUserProvidedLocation().equals("")) {
            mLocationLabel.setText(getString(R.string.default_location));
        }

        if (appPreferences.isHasSetLatLong()) {
            callForecastApi(appPreferences.getSetLat(), appPreferences.getSetLong());
        }
        else if (appPreferences.isHasSetLocation()) {
            callMapquestApi(appPreferences.getUserProvidedLocation());
        }


        mLocationLabel.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    String providedLocation = mLocationLabel.getText().toString();
                    callMapquestApi(providedLocation);
                    appPreferences.setUserProvidedLocation(providedLocation);


                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mLocationLabel.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });  //Handles the "Enter" of the EditText submission


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callForecastApi(latitude, longitude);
                //callMapquestApi("Ondara, Spain");
                //Toast.makeText(MainActivity.this, "Location" + givenLocation + ", Longitude: " + longitude + ", Latitude: " + latitude, Toast.LENGTH_SHORT).show();
            }
        };

        mButton.setOnClickListener(listener);
    }


    private void callForecastApi(double queryLatitude, double queryLongitude) {
        if (isNetworkAvail()) {
            toggleRefresh();


            String baseUrl = "https://api.forecast.io/forecast/";
            String API_KEY = "9cda4809de69a167534617f6c1ff2972";
            String queryOptions = "?units=si&lang=";
            String queryLang = getString(R.string.query_language);
            final String forecastUrl = baseUrl + API_KEY + '/' + queryLatitude
                    + "," + queryLongitude + queryOptions + queryLang;

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
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

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        if (response.isSuccessful()) {
                            Log.i(TAG, "Successful Forecast Request");
                            mCurrentWeather = getCurrentDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                    toggleRefresh();
                                }
                            });

                        } else {
                            alertUserAboutError();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toggleRefresh();
                                }
                            });
                        }
                    } catch (IOException | JSONException e) {
                        alertUserAboutError();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleRefresh();
                            }
                        });
                    }
                }
            });

        } else {
            alertUserAboutError();
        }
    }

    private void callMapquestApi(String queryLocation) {

        if (isNetworkAvail()) {

            String baseUrl = "http://open.mapquestapi.com/geocoding/v1/address?key=";
            String API_KEY = "Fmjtd%7Cluu821uz2q%2C8n%3Do5-94bn5w";
            String queryOptions0 = "&location=";
            String mQueryLocation = Uri.encode(queryLocation);
            String queryOptions1 = "&maxResults=1";
            final String mapquestUrl = baseUrl + API_KEY + queryOptions0 + mQueryLocation + queryOptions1;

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(mapquestUrl)
                    .build();
            Log.i(TAG, "Built Request");

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.e(TAG, "Failure Mapquest");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.d(TAG, jsonData);
                        if (response.isSuccessful()) {
                            Log.i(TAG, "Successful Mapquest Request");
                            mCurrentLocation = getLocationLatLong(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    saveLatLongPrefs(latitude, longitude);
                                    callForecastApi(latitude, longitude);
                                    //updateDisplay();
                                    //toggleRefresh();
                                }
                            });

                        } else {
                            Log.e(TAG, "Mapquest elseblock onReponse");
                            alertUserAboutError();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //toggleRefresh();
                                }
                            });
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, e.getMessage());
                        alertUserAboutError();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //toggleRefresh();
                            }
                        });
                    }
                }
            });

        } else {
            alertUserAboutError();
        }

    }

    @Override
    protected void onResume() {
        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        relativeLayout.setBackgroundColor(mColorWheel.getColor());
        super.onResume();
    }

    private void updateDisplay() {
        mTemperatureValue.setText(mCurrentWeather.getTemperature() + "°C");
        mTimeLabel.setText(getString(R.string.time_prefix) + mCurrentWeather.getFormattedTime() + getString(R.string.time_suffix));
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "%");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
        mSummaryLabel.setText(mCurrentWeather.getSummary());

        mIconImageView.setImageDrawable(getResources().getDrawable(mCurrentWeather.getIconId()));

    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mButton.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mButton.setVisibility(View.VISIBLE);
        }
    }

    private CurrentWeather getCurrentDetails(String jsonForecastData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonForecastData);
        JSONObject currently = forecast.getJSONObject("currently");
        JSONObject hourly = forecast.getJSONObject("hourly");

        String timezone = forecast.getString("timezone");

        CurrentWeather currentWeather = new CurrentWeather();

        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(hourly.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);

        return currentWeather;
    }

    private CurrentLocation getLocationLatLong(String jsonData) throws JSONException {
        JSONObject locationQuery = new JSONObject(jsonData);
        JSONObject results = locationQuery.getJSONArray("results").getJSONObject(0);

        JSONObject latLng = results.getJSONArray("locations").getJSONObject(0).getJSONObject("latLng");
        JSONObject providedLocation = results.getJSONObject("providedLocation");

        CurrentLocation currentLocation = new CurrentLocation();

        currentLocation.setProvidedLocation(providedLocation.getString("location"));
        currentLocation.setLatitude(latLng.getDouble("lat"));
        currentLocation.setLongitude(latLng.getDouble("lng"));
        latitude = latLng.getDouble("lat");
        longitude = latLng.getDouble("lng");
        givenLocation = providedLocation.getString("location");

        Log.e(TAG, "Latitude: " + latitude + ", Longitude: " + longitude + ", Location: " + givenLocation);

        return currentLocation;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");

    }


    public boolean isNetworkAvail() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    // Is this actually useful? Seems pretty redundant to me...

    private void saveLatLongPrefs(double latitude, double longitude) {
        AppPreferences mAppPreferences = new AppPreferences(MainActivity.this);
        mAppPreferences.setLatLong(latitude, longitude);
    }

}
