package pw.moter8.sunnyweather;


public class CurrentLocation {

    private double mLatitude;
    private double mLongitude;
    private String mProvidedLocation;

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public String getProvidedLocation() {
        return mProvidedLocation;
    }

    public void setProvidedLocation(String providedLocation) {
        mProvidedLocation = providedLocation;
    }
}
