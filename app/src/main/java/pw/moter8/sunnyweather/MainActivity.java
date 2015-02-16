package pw.moter8.sunnyweather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
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


    @InjectView(R.id.timeLabel) TextView mTimeLabel;
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


        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        relativeLayout.setBackgroundColor(mColorWheel.getColor()); // random backgroundColor

        final double queryLat = 38.83831;
        final double queryLon = 0.02316;

        callApi(queryLat, queryLon);


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callApi(queryLat, queryLon);
            }
        };

        mButton.setOnClickListener(listener);
    }

    private void callApi(double latitude, double longitude) {

        if (isNetworkAvail()) {
            //callMapquestApi("Ondara, Spain");
            callForecastApi(latitude, longitude);
            toggleRefresh();
        }
        else {

            alertUserAboutError();
        }
    }

    private void callForecastApi(double queryLatitude, double queryLongitude) {

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
                alertUserAboutError();
                toggleRefresh();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    if (response.isSuccessful()) {
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
                        toggleRefresh();
                    }
                } catch (IOException | JSONException e) {
                    alertUserAboutError();
                    toggleRefresh();
                }
            }
        });
    }

    private void callMapquestApi (String queryLocation){

        String baseUrl = "http://open.mapquestapi.com/geocoding/v1/adress?key=";
        String API_KEY = "Fmjtd%7Cluu821uz2q%2C8n%3Do5-94bn5w";
        String queryOptions0 = "&json={location:{street:\"";
        // String mQueryLoc = queryLocation;
        String queryOptions1 = "\"},options:{thumbMaps:false,maxResults:1}}";
        final String mapquestUrl = baseUrl + API_KEY + queryOptions0 + queryLocation + queryOptions1;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(mapquestUrl)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                alertUserAboutError();
                toggleRefresh();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    if (response.isSuccessful()) {
                        //Log.d(TAG, jsonData);
                        mCurrentLocation = getLocationLatLong(jsonData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplay();
                            }
                        });

                    } else {
                        alertUserAboutError();
                        toggleRefresh();
                    }
                } catch (IOException | JSONException e) {
                    alertUserAboutError();
                    toggleRefresh();
                }
            }
        });

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
        JSONObject location = locationQuery.getJSONArray("results").getJSONArray(0).getJSONObject(0);

        JSONObject latLng = location.getJSONArray("locations").getJSONObject(0).getJSONObject("latLng");
        JSONObject providedLocation = location.getJSONObject("providedLocation");

        CurrentLocation currentLocation = new CurrentLocation();

        currentLocation.setProvidedLocation(providedLocation.getString("providedLocation"));
        currentLocation.setLatitude(latLng.getDouble("lat"));
        currentLocation.setLongitude(latLng.getDouble("lng"));

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
}
