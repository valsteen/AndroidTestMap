package org.codesoup;

import java.security.InvalidAlgorithmParameterException;
import java.util.List;
import java.util.Vector;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.maps.*;
import fi.foyt.foursquare.api.*;
import fi.foyt.foursquare.api.entities.Checkin;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.Location;


public class TestMapActivity extends MapActivity {
 
	private static final String CLIENT_ID = "CLIENT_ID_PLACEHOLDER";
	private static final String CLIENT_SECRET = "CLIENT_SECRET_PLACEHOLDER";
	private static final String CALLBACK_URL = "http://codesoup.org/android/4sq/";
	private static final String FSQTOKEN_PREF = "FSQTOKEN";
	
	private MapView mapView;
	private String _fsqToken; 
	private FoursquareApi foursquareapi ;
	
	private Vector<TokenCallback> tokenConsummers = new Vector<TokenCallback>();
	
	private void callTokenConsummers() {
		synchronized (tokenConsummers) {
			for (TokenCallback cb : tokenConsummers) {
				cb.doIt();
			}
			tokenConsummers.clear();
		}
	}
	
	public String getFsqToken(TokenCallback cb) {
		if (cb != null) {
			tokenConsummers.add(cb);
		}
		
		// TODO : webview seems to be called even when token is present ? ( click 2 times on get token )
		if (_fsqToken == null) {
		    _fsqToken = getPreferences(MODE_PRIVATE).getString(FSQTOKEN_PREF, null);
		    if (_fsqToken == null) {
		    	Toast.makeText(this, "Getting token. Please retry when token is available.", Toast.LENGTH_SHORT).show();
		    	Intent intent = new Intent(TestMapActivity.this, ActivityWebView.class);
				intent.putExtra("url", TestMapActivity.this.getFourSquareApi().getAuthenticationUrl());
		        TestMapActivity.this.startActivityForResult(intent, 1);
		    } else {
		    	getFourSquareApi().setoAuthToken(_fsqToken);
		    	Toast.makeText(this, "Got saved token", Toast.LENGTH_SHORT).show();
		    }
		}
		if (_fsqToken != null) callTokenConsummers();
		return _fsqToken;
	}
	
	public String getFsqToken() {
		return getFsqToken(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	interface TokenCallback {
		public void doIt();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.get_token:
	    	getFsqToken(new TokenCallback() {				
				public void doIt() {
					test4sq();
				}
			});
	        return true;
	    case R.id.reset_token:
	        setFsqToken(null);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void setFsqToken(String fsqToken) {
		this._fsqToken = fsqToken;
		
		SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
	    
	    if (fsqToken != null) {
	    	foursquareapi.setoAuthToken(_fsqToken);
		    editor.putString(FSQTOKEN_PREF, fsqToken);
		    
	    	Toast.makeText(this, "Saving token " + fsqToken, Toast.LENGTH_SHORT).show();
	    } else {
	    	editor.remove(FSQTOKEN_PREF);
	    	Toast.makeText(this, "Removing fsq token", Toast.LENGTH_SHORT).show();
	    }
	    
	    editor.commit();
	}

	private FoursquareApi getFourSquareApi() {
		if (foursquareapi == null) {
			foursquareapi = new FoursquareApi(CLIENT_ID, CLIENT_SECRET, CALLBACK_URL);
		}
		return foursquareapi;
	}
	
	TestOverlay overlay;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        Drawable marker = getResources().getDrawable(R.drawable.defaultmarker);
        marker.setBounds((int)-marker.getIntrinsicWidth()/2, (int)-marker.getIntrinsicHeight()/2,
        	 (int)marker.getIntrinsicWidth()/2, (int)marker.getIntrinsicHeight()/2);
        
        overlay = new TestOverlay(new GeoPoint(0,0), marker);
        mapView.getOverlays().add(overlay);
    }

    private void test4sq() {
    	FoursquareApi fsq = getFourSquareApi();
    	
    	try {
    		/* TODO -- should be cached */
			Result<Checkin[]> checkins = fsq.checkinsRecent(null, null, null);
			
			if (checkins.getMeta().getCode() != 200) {
				// token was invalid ? reset
				// TODO -- should catch it elsewhere ( callback/delegate/ ... )
				/* ??? didn't work */
				Toast.makeText(this, "token invalid " + checkins.getMeta().getErrorDetail().toString(), Toast.LENGTH_LONG).show();
	    		setFsqToken(null);
	    		return;
	    	}
			
			overlay.setCheckins(checkins.getResult());
		} catch (FoursquareApiException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	class TestOverlay extends ItemizedOverlay<OverlayItem> {
		Vector<GeoPoint> points; 
		
		public TestOverlay(GeoPoint center, Drawable defaultMarker) {
			super(defaultMarker);
			populate();
		}
		
		@Override
		protected OverlayItem createItem(int i) {
			return new OverlayItem(points.get(i), "", "");
		}

		@Override
		public int size() {
			if (points == null) return 0;
			return points.size();
		}
		
		public void setCheckins(Checkin[] checkins) {
			points = new Vector<GeoPoint>();
			for (Checkin checkin : checkins) {
				Location location = checkin.getLocation();
				if (location != null) {
					GeoPoint geopoint = new GeoPoint((int)(location.getLat()*1E6), (int)(location.getLng()*1E6));
					points.add(geopoint);
				} else {
					CompactVenue venue = checkin.getVenue();
					if (venue != null) {
						location = venue.getLocation();
						if (location != null) {
							GeoPoint geopoint = new GeoPoint((int)(location.getLat()*1E6), (int)(location.getLng()*1E6));
							points.add(geopoint);
						}
					}
				}
			}
			if (points.size() > 0) {
				populate();
				mapView.getController().setCenter(getCenter());
				mapView.getController().setZoom(13);
				mapView.invalidate();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == 1) {
			String fsqCode = data.getStringExtra("code");
			
			try {
				foursquareapi.authenticateCode(fsqCode);
				String token = foursquareapi.getOAuthToken();
				if (token != null) {
					setFsqToken(token);
					callTokenConsummers();
					return;
				}
			} catch (FoursquareApiException e) {
				e.printStackTrace();
				return;
			}
			
			if (getFsqToken() != null) {
				Toast.makeText(this, "Token: " + getFsqToken(), Toast.LENGTH_SHORT).show();
				return;
			} 
		} 
			 
		Toast.makeText(this, "Token activity error", Toast.LENGTH_SHORT).show();		
	}
}
