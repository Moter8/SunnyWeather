package pw.moter8.sunnyweather;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class AppPreferences extends Activity {

    SharedPreferences mSharedPreferences;

    public AppPreferences (Context context){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getUserProvidedLocation() {
        return mSharedPreferences.getString("Location", "");
    }

    public void setUserProvidedLocation(String providedLocation) {
        mSharedPreferences
                .edit()
                .putString("Location", providedLocation)
                .apply();
    }

    public void setLatLong(double latitude, double longitude) {
        mSharedPreferences
                .edit()
                .putLong("Latitude", Double.doubleToLongBits(latitude))
                .putLong("Longitude", Double.doubleToLongBits(longitude))
                .apply();
    }

    public double getSetLat () {
        return Double.longBitsToDouble(mSharedPreferences.getLong("Latitude", 0));
    }

    public double getSetLong () {
        return Double.longBitsToDouble(mSharedPreferences.getLong("Longitude", 0));
    }


    public boolean isHasSetLocation() {
        return !getUserProvidedLocation().equals("");
    }

    public boolean isHasSetLatLong() {
        return (getSetLong() != 0.0);
    }
}
