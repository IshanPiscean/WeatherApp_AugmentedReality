package com.example.weather;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.location.Location;

/**
 * Fetch weather info from location coordinates
 * Weather service : OpenWeather Map.org
 * TODO : Kep your app logic in this class 
 * @author Ishan Mehta
 *
 */
public class WeatherInfo {
  // Final request :api.openweathermap.org/data/2.5/weather?lat=35&lon=139
  private static final String WEATHER_API = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s"; 
	
  /**
   * Send Http Request using location coordinates parameters
   * 
   * @param aInlatitude - Location latitude
   * @param aInLongitude - Location longitude 
   */
  public static String getWeatherInfo(Location aInLocation)
  { 
	 String lWeatherResponseStr = "";
	 try
	 {	 
		HttpURLConnection lConnection = null;
	    URL lUrl = new URL(String.format(WEATHER_API, String.valueOf(aInLocation.getLatitude()), String.valueOf(aInLocation.getLongitude())));
	    System.out.println(lUrl); //TODO: Remove later
        lConnection = (HttpURLConnection)lUrl.openConnection();
        lConnection.setRequestMethod("GET");
        lConnection.setDoInput(true);
        lConnection.setDoOutput(true);
		lConnection.connect();
		  
		// Let's read the response
        StringBuilder lBuilder = new StringBuilder();
        InputStream lInputStream = lConnection.getInputStream();
        
        // Read content from input character stream
        BufferedReader lReader = new BufferedReader(
      		  new InputStreamReader(lInputStream)); // Input stream reader - reads bytes and decodes them into characters 
        
        String ltemp = "";
        
        
        while((ltemp = lReader.readLine()) != null)
        {
      	  lBuilder.append(ltemp);
        }
    	
        lInputStream.close();
        System.out.println(lBuilder.toString());
        lWeatherResponseStr = lBuilder.toString();
		lConnection.disconnect();
  }
catch(Exception ex)
  {
 	ex.printStackTrace();
  }	
	 
 return  lWeatherResponseStr;
 }
  
}
