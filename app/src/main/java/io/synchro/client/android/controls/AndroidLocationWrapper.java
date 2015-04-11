package io.synchro.client.android.controls;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import io.synchro.client.android.BindingContext;
import io.synchro.client.android.BindingHelper;
import io.synchro.client.android.CommandInstance;
import io.synchro.client.android.CommandName;
import io.synchro.client.android.ControlWrapper;
import io.synchro.client.android.IGetViewValue;
import io.synchro.json.JObject;
import io.synchro.json.JToken;
import io.synchro.json.JValue;

/**
 * Created by blake on 3/30/15.
 */
public class AndroidLocationWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidLocationWrapper.class.getSimpleName();

    static String[] Commands = new String[]{CommandName.getOnUpdate().getAttribute()};

    boolean _updateOnChange = false;

    LocationManager  _locMgr;
    LocationListener _listener;

    LocationStatus _status = LocationStatus.Unknown;
    Location _location;

    private class AndroidLocationWrapperLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location location)
        {
            OnLocationChanged(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            OnStatusChanged(provider, status, extras);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            OnProviderEnabled(provider);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            OnProviderDisabled(provider);
        }
    }

    public AndroidLocationWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                                 )
    {
        super(parent, bindingContext);
        Log.d(TAG, "Creating location element");
        this._isVisualElement = false;

        int threshold = (int)ToDouble(controlSpec.get("movementThreshold"), 100);

        Context ctx = ((AndroidControlWrapper)parent).getControl().getContext();
        _locMgr = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);

        Criteria locationCriteria = new Criteria();

        locationCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        locationCriteria.setPowerRequirement(Criteria.POWER_MEDIUM);

        String locationProvider = _locMgr.getBestProvider(locationCriteria, true);
        if (locationProvider != null)
        {
            if (_locMgr.isProviderEnabled(locationProvider))
            {
                Log.i(TAG, String.format("Using best location provider: %s", locationProvider));
                _status = LocationStatus.Available;
                _listener = new AndroidLocationWrapperLocationListener();
                _locMgr.requestLocationUpdates(locationProvider, 2000, threshold, _listener);
            }
            else
            {
                Log.i(TAG, String.format("Best location provider: %s - not enabled", locationProvider));
                _status = LocationStatus.NotAvailable;
            }
        }
        else
        {
            // You will always get some kind of provider back if location services are
            // enabled, so this means that they are not enabled...
            //
            Log.i(TAG, "No location providers available");
            _status = LocationStatus.NotApproved;
        }

        JObject bindingSpec = BindingHelper.GetCanonicalBindingSpec(controlSpec, "value", Commands);
        ProcessCommands(bindingSpec, Commands);

        processElementBoundValue("value", bindingSpec.get("value").asString(), new IGetViewValue()
                                 {
                                     @Override
                                     public JToken GetViewValue()
                                     {
                                         JObject obj = new JObject();

                                         obj.put("available", new JValue((_status == LocationStatus.Available) || (_status == LocationStatus.Active)));
                                         obj.put("status", new JValue(_status.toString()));

                                         if (_location != null)
                                         {
                                             JObject latlong = new JObject();

                                             latlong.put("latitude", new JValue(_location.getLatitude()));
                                             latlong.put("longitude", new JValue(_location.getLongitude()));

                                             obj.put("coordinate", latlong);

                                             if (_location.hasAccuracy())
                                             {
                                                 obj.put(
                                                         "accuracy",
                                                         new JValue(_location.getAccuracy())
                                                        );
                                             }


                                            /* Altitude, when provided, represents meters above the WGS 84 reference ellipsoid,
                                             * which is of little or no utility to apps on our platform.  There also doesn't
                                             * appear to be any altittude accuracy on Android as there is on the othert platforms.
                                             *
                                            if (_location.HasAltitude)
                                            {
                                                obj.Add("altitude", MaaasCore.JValue(_location.Altitude));
                                            }
                                             */

                                             if (_location.hasBearing())
                                             {
                                                 obj.put(
                                                         "heading",
                                                         new JValue(_location.getBearing())
                                                        );
                                             }

                                             if (_location.hasSpeed())
                                             {
                                                 obj.put("speed", new JValue(_location.getSpeed()));
                                             }

                                             //_location.Time // UTC time, seconds since 1970
                                         }

                                         return obj;
                                     }
                                 },
                                 null);

        if ((bindingSpec.get("sync") != null) && (bindingSpec.get("sync").asString().equals("change")))
        {
            _updateOnChange = true;
        }

        // This triggers the viewModel update so the initial status gets back to the server
        //
        updateValueBindingForAttribute("value");
    }

    @Override
    public void Unregister()
    {
        Log.i(TAG, "Location control unregistered, discontinuing location updates");
        if (_listener != null)
        {
            _locMgr.removeUpdates(_listener);
        }
        super.Unregister();
    }

    public void OnProviderEnabled(String provider)
    {
        Log.i(TAG, String.format("Provider enabled: %s", provider));
    }

    public void OnProviderDisabled(String provider)
    {
        Log.i(TAG, String.format("Provider disabled: %s", provider));
    }

    public void OnStatusChanged(String provider, int status, Bundle extras)
    {
        // !!! Are we going to get these for providers other than the one we're using?
        //
        // Availability.Available
        // Availability.OutOfService
        // Availability.TemporarilyUnavailable
        //
        Log.i(TAG, String.format("Status change: %d", status));
        if (status == LocationProvider.AVAILABLE)
        {
            if (_status != LocationStatus.Available)
            {
                _status = LocationStatus.Available;
            }
        }
        else if ((status == LocationProvider.OUT_OF_SERVICE) || (status == LocationProvider.TEMPORARILY_UNAVAILABLE))
        {
            _status = LocationStatus.NotAvailable;
        }

        // Update the viewModel, and the server (if update on change specified)
        //
        updateValueBindingForAttribute("value");
        if (_updateOnChange)
        {
            this.getStateManager().sendUpdateRequestAsync();
        }
    }

    public void OnLocationChanged(Location location)
    {
        Log.i(TAG, String.format("Location change: %s", location));
        _status = LocationStatus.Active;
        _location = location;

        updateValueBindingForAttribute("value");

        CommandInstance command = GetCommand(CommandName.getOnUpdate());
        if (command != null)
        {
            this.getStateManager().sendCommandRequestAsync(command.getCommand(), command.GetResolvedParameters(getBindingContext()));
        }
        else if (_updateOnChange)
        {
            this.getStateManager().sendUpdateRequestAsync();
        }
    }}
