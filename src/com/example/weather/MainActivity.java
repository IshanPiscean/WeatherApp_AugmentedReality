package com.example.weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
//import android.R;

public class MainActivity extends FragmentActivity implements ConnectionCallbacks,
    OnConnectionFailedListener {
   
	static EditText latitudeView;
	static EditText longitudeView;
	static EditText cityView;
	static EditText weatherDescriptionView;
	static EditText temperatureView;
	Button weatherButton;
	static String weatherJASONResponse;
	
	private SeekBar distance_seekBar;
	static EditText searchDistance;
	
	// Google client api reference
	private GoogleApiClient mGoogleApiClient;
	
	// Error resolution class variables to be used in onConnectionFailed()
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Boolean to track whether the app is already resolving an error
    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    
    private static final int MAX_SEARCH_DISTANCE = 300;
    // Search distance
    static Integer seekBar_distance_covered;
    
    /**
	 * Holds the current location
	 */
	private static Location currentLocation = Region.getMyLocation(); 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load XML resource for UI display
        setContentView(R.layout.activity_main);
        
        initializeVariables(savedInstanceState);
        
    }

	/**
	 * Initializes UI elements and google client API specific variables
	 * 
	 * @param savedInstanceState
	 */
	private void initializeVariables(Bundle savedInstanceState) {
		latitudeView = (EditText) findViewById(R.id.latitudeId);
        longitudeView = (EditText) findViewById(R.id.longitudeId);
        weatherButton = (Button) findViewById(R.id.weather_buttonId);
        cityView = (EditText) findViewById(R.id.city_name);
        weatherDescriptionView = (EditText) findViewById(R.id.weather_description);
        temperatureView = (EditText) findViewById(R.id.temperature);
        
        // Default search distance
        seekBar_distance_covered = 0;
        
        mGoogleApiClient = new GoogleApiClient.Builder(this).
		addConnectionCallbacks(this).addOnConnectionFailedListener(this).
		addApi(LocationServices.API).build();
        // Recover resolution recover boolean from saveIntance State method()
        mResolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        
        InitializeSeekBar();
	}

	// TODO: Move further to the end 
	public void InitializeSeekBar()
	{
		distance_seekBar = (SeekBar) findViewById(R.id.distance_seekbar);
        searchDistance = (EditText) findViewById(R.id.search_distance);
        // Set MAX searchable distance 
        distance_seekBar.setMax(MAX_SEARCH_DISTANCE);
        searchDistance.setText("Search distance :" + String.valueOf(distance_seekBar.getProgress()));
        
        // Add a listener to distance_seekbar
        distance_seekBar.setOnSeekBarChangeListener(
        		new OnSeekBarChangeListener() {
					
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						
						searchDistance.setText("Search distance :" + seekBar_distance_covered.toString());
//						Toast.makeText(MainActivity.this, "Seekbar in stop tracking", Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						
//						Toast.makeText(MainActivity.this, "Seekbar in start tracking", Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int currentProgress,
							boolean fromUser) {
						
						// Assign current progress value to our search distance variable
						seekBar_distance_covered = currentProgress;
						searchDistance.setText("Search distance :" + currentProgress);
						
//						Toast.makeText(MainActivity.this, "Seekbar in progress tracking", Toast.LENGTH_SHORT).show();
					}
				} );
	}
	
    @Override
	protected void onStart() {
		super.onStart();
		if (!mResolvingError) {  
            mGoogleApiClient.connect();
            System.out.println("INSIDE onStart googleclient connect()");
        }
	}
    
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    
    /***
	 * Invoked when get weather button is pressed
	 * 
	 * @param view
	 */
	public void getWeather(View view) {

		   new getWeather().execute(currentLocation);

	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int lId = item.getItemId();
        if (lId == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private class getWeather extends AsyncTask<Location, Void, String>
    {

    	@Override
    	protected void onPreExecute()
    	{
    		
    	}
		@Override
		protected String doInBackground(Location... arg0) {
			
			 currentLocation = arg0[0];
			 System.out.println("Location info in doInBackground()"+ String.valueOf(currentLocation.getLatitude()) + String.valueOf(currentLocation.getLongitude()));
			 weatherJASONResponse = WeatherInfo.getWeatherInfo(currentLocation);
			 
			 return weatherJASONResponse;
        }
	   /**
		* Read JSON weather response
		* &
		* Set UI text elements
		*/
		@Override
		protected void onPostExecute(String aInWeatherJsonStr)
		{
			try{
				
		      // Get Root JSON Object
				JSONObject lWeatherJSONRootObj = new JSONObject(aInWeatherJsonStr); 
		        // Save city weather information
				Region.setMyCity(lWeatherJSONRootObj.getString("name").toUpperCase());
				
				// main object contains city temperature information
				JSONObject lWeatherMainObj = lWeatherJSONRootObj.getJSONObject("main");
	
				// Convert kelvin to Celsius temperature
				Region.setTemperature((float)lWeatherMainObj.getDouble("temp") - 273.15f);
				
				// Contains weather description ( Example: "Moderate rain")
				JSONArray lWeatherArray = lWeatherJSONRootObj.getJSONArray("weather");
				JSONObject lWeatherDescription = lWeatherArray.getJSONObject(0);
				Region.setWeather_description(lWeatherDescription.getString("description"));
				
				
				// Display city weather information
				cityView.setText(Region.getMyCity());
				temperatureView.setText(String.valueOf(Math.round(Region.getTemperature())));
				weatherDescriptionView.setText(Region.getWeather_description());
		      
			  }
			  catch(JSONException ex)
			  {
				  ex.printStackTrace();
			  }
			
		}
		
}

	@Override
	public void onConnected(Bundle arg0) {
		
		// Get_last_location return location object from which we retrieve latitude and longitude position
		currentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
		
		if(currentLocation != null)
		{
			// TODO REMOVE LATER. Instead populate city weather info separately on button click
			System.out.println("INSIDE onConnected");
			latitudeView.setText("LAT : " + String.valueOf(currentLocation.getLatitude()));
			longitudeView.setText("LONG : " + String.valueOf(currentLocation.getLongitude()));
			
			  System.out.println("Location info in onConnected()"+ String.valueOf(currentLocation.getLatitude()) + String.valueOf(currentLocation.getLongitude()));
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
	
		// Here error resolution takes place.
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
		
	}
	
	/* Creates a dialog for an error message */
	private void showErrorDialog(int errorCode) {	
		  // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "Error Message (TODO)");
	}
	
	 /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    public static class ErrorDialogFragment extends DialogFragment
    {
    	public ErrorDialogFragment() { }
    	
    	 @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             // Get the error code and retrieve the appropriate dialog
             int errorCode = this.getArguments().getInt(DIALOG_ERROR);
             return GooglePlayServicesUtil.getErrorDialog(errorCode,
                     this.getActivity(), REQUEST_RESOLVE_ERROR);
         }

         @Override
         public void onDismiss(DialogInterface dialog) {
             ((MainActivity)getActivity()).onDialogDismissed();
         }
    }
    
    // Maintain state while resolving an error
    // After error resolution we should set error flag back to FALSE.
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	if(resultCode == RESULT_OK)
    	{
    		 mResolvingError = false;
    	}
    	else if(resultCode == RESULT_CANCELED)
    	{
    		mResolvingError = false;
    	}
    }
    
    /**
     * To maintain state of app when activity restarts or app was killed abruptly 
     */
    protected void onSaveInstanceState(Bundle outState)
    {
    	super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
        // Then we recover the saved state in onCreate()
    }
}