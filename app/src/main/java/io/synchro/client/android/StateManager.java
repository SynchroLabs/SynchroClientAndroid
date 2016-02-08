package io.synchro.client.android;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.Map;

import io.synchro.json.JArray;
import io.synchro.json.JObject;
import io.synchro.json.JToken;
import io.synchro.json.JValue;

/**
 * Created by blake on 2/22/15.
 */
public class StateManager
{
    public static final String TAG = StateManager.class.getSimpleName();

    public interface ICommandHandler
    {
        public void CommandHandler(String command) throws IOException;
    }

    public interface IProcessPageView
    {
        public void ProcessPageView(JObject pageView);
    }

    public interface IProcessMessageBox
    {
        public void ProcessMessageBox(JObject messageBox, ICommandHandler commandHandler);
    }

    public interface IProcessUrl
    {
        public void ProcessUrl(String primaryUrl, String secondaryUrl);
    }

    SynchroAppManager _appManager;
    SynchroApp _app;
    JObject _appDefinition;
    Transport _transport;

    long _transactionNumber = 1;
    long getNewTransactionId()
    {
        return _transactionNumber++;
    }

    String _path;
    int   _instanceId;
    int   _instanceVersion;
    boolean   _isBackSupported;

    ViewModel _viewModel;
    IProcessPageView _onProcessPageView;
    IProcessMessageBox _onProcessMessageBox;
    IProcessUrl _onProcessUrl;

    SynchroDeviceMetrics _deviceMetrics;

    public StateManager(SynchroAppManager appManager, SynchroApp app, Transport transport, SynchroDeviceMetrics deviceMetrics)
    {
        _viewModel = new ViewModel();

        _appManager = appManager;
        _app = app;
        _appDefinition = app.getAppDefinition();
        _transport = transport;
        // _transport.setDefaultHandlers(this.ProcessResponseAsync, this.ProcessRequestFailure);

        _deviceMetrics = deviceMetrics;
    }

    public boolean IsBackSupported()
    {
        return _isBackSupported;
    }

    public boolean IsOnMainPath()
    {
        return ((_path != null) && (_appDefinition != null) && _path.equals(_appDefinition.get("main").asString()));
    }

    public ViewModel getViewModel()
    {
        return _viewModel;
    }

    public SynchroDeviceMetrics getDeviceMetrics()
    {
        return _deviceMetrics;
    }

    public void SetProcessingHandlers(IProcessPageView OnProcessPageView, IProcessMessageBox OnProcessMessageBox, IProcessUrl OnProcessUrl)
    {
        _onProcessPageView = OnProcessPageView;
        _onProcessMessageBox = OnProcessMessageBox;
        _onProcessUrl = OnProcessUrl;
    }

    JObject PackageDeviceMetrics()
    {
        JObject returnObject = new JObject();

        returnObject.put("os", new JValue(getDeviceMetrics().getOS()));
        returnObject.put("osName", new JValue(this.getDeviceMetrics().getOSName()));
        returnObject.put("deviceName", new JValue(this.getDeviceMetrics().getDeviceName()));
        // !!! BCR -- enum toString is it reasonable?
        returnObject.put("deviceType", new JValue(this.getDeviceMetrics().getDeviceType().toString()));
        returnObject.put("deviceClass", new JValue(this.getDeviceMetrics().getDeviceClass().toString()));
        returnObject.put("naturalOrientation", new JValue(this.getDeviceMetrics().getNaturalOrientation().toString()));
        returnObject.put("widthInches", new JValue(this.getDeviceMetrics().getWidthInches()));
        returnObject.put("heightInches", new JValue(this.getDeviceMetrics().getHeightInches()));
        returnObject.put("widthDeviceUnits", new JValue(this.getDeviceMetrics().getWidthDeviceUnits()));
        returnObject.put("heightDeviceUnits", new JValue(this.getDeviceMetrics().getHeightDeviceUnits()));
        returnObject.put("deviceScalingFactor", new JValue(this.getDeviceMetrics().getDeviceScalingFactor()));
        returnObject.put("widthUnits", new JValue(this.getDeviceMetrics().getWidthUnits()));
        returnObject.put("heightUnits", new JValue(this.getDeviceMetrics().getHeightUnits()));
        returnObject.put("scalingFactor", new JValue(this.getDeviceMetrics().getScalingFactor()));
        returnObject.put("clientName", new JValue(this.getDeviceMetrics().getClientName()));
        returnObject.put("clientVersion", new JValue(this.getDeviceMetrics().getClientVersion()));

        return returnObject;
    }

    JObject PackageViewMetrics(SynchroOrientation orientation)
    {
        JObject returnObject = new JObject();

        if (orientation == this.getDeviceMetrics().getNaturalOrientation())
        {
            returnObject.put("orientation", new JValue(orientation.toString()));
            returnObject.put("widthInches", new JValue(this.getDeviceMetrics().getWidthInches()));
            returnObject.put("heightInches", new JValue(this.getDeviceMetrics().getHeightInches()));
            returnObject.put("widthUnits", new JValue(this.getDeviceMetrics().getWidthUnits()));
            returnObject.put("heightUnits", new JValue(this.getDeviceMetrics().getHeightUnits()));
        }
        else
        {
            returnObject.put("orientation", new JValue(orientation.toString()));
            returnObject.put("widthInches", new JValue(this.getDeviceMetrics().getHeightInches()));
            returnObject.put("heightInches", new JValue(this.getDeviceMetrics().getWidthInches()));
            returnObject.put("widthUnits", new JValue(this.getDeviceMetrics().getHeightUnits()));
            returnObject.put("heightUnits", new JValue(this.getDeviceMetrics().getWidthUnits()));
        }

        return returnObject;
    }

    void messageBox(String title, String message, String buttonLabel, String buttonCommand, ICommandHandler onCommand)
    {
        JObject messageBox = new JObject();
        JArray optionsArray = new JArray();
        JObject optionsObject = new JObject();

        messageBox.put("title", new JValue(title));
        messageBox.put("message", new JValue(message));
        messageBox.put("options", optionsArray);

        optionsObject.put("label", new JValue(buttonLabel));
        optionsObject.put("command", new JValue(buttonCommand));

        optionsArray.add(optionsObject);

        _onProcessMessageBox.ProcessMessageBox(messageBox, onCommand);
    }

    void ProcessRequestFailure(final JObject request, Exception ex)
    {
        try
        {
            Log.w(TAG, String.format("Got request failure for request: %s", request.toJson()), ex);
        }
        catch (IOException e)
        {
            Log.wtf(TAG, e);
        }

        // !!! I think this needs to be sendMessageProcessResponseAsync
        messageBox("Connection Error", "Error connecting to application server", "Retry", "retry", new ICommandHandler()
                   {
                       @Override
                       public void CommandHandler(String command)
                       {
                           Log.d(TAG, String.format("Retrying request after user confirmation (%s)...", command));
                           try
                           {
                               ProcessResponseAsync(_transport.sendMessage(_app.getSessionId(), request));
                           }
                           catch (IOException e)
                           {
                               ProcessRequestFailure(request, e);
                           }
                       }
                   });
    }

    void ProcessResponseAsync(JObject responseAsJSON)
            throws IOException
    {
        // logger.Info("Got response: {0}", (string)responseAsJSON);

        if (responseAsJSON.get("NewSessionId") != null)
        {
            String newSessionId = responseAsJSON.get("NewSessionId").asString();
            if (_app.getSessionId() != null)
            {
                // Existing client SessionId was replaced by server.  Do we care?  Should we do something (maybe clear any
                // other client session state, if there was any).
                //
                Log.d(TAG, String.format("Client session ID of: %s was replaced with new session ID: %s", _app.getSessionId(), newSessionId));
            }
            else
            {
                Log.d(TAG, String.format(
                              "Client was assigned initial session ID of: %s", newSessionId
                                        ));
            }

            // SessionId was created/updated by server.  Record it and save state.
            //
            _app.setSessionId(newSessionId);
            _appManager.saveState();
        }

        if (responseAsJSON.get("Error") != null)
        {
            JObject jsonError = (JObject) responseAsJSON.get("Error");
            Log.w(TAG, String.format("Response contained error: %s", jsonError.get("message").asString()));
            if (jsonError.get("name").asString().equals("SyncError"))
            {
                if (responseAsJSON.get("InstanceId") == null)
                {
                    // This is a sync error indicating that the server has no instance (do to a corrupt or
                    // re-initialized session).  All we can really do here is re-initialize the app (clear
                    // our local state and do a Page request for the app entry point).
                    //
                    Log.e(TAG, "ERROR - corrupt server state - need app restart");
                    messageBox("Synchronization Error", "Server state was lost, restarting application", "Restart", "restart", new ICommandHandler()
                               {
                                   @Override
                                   public void CommandHandler(String command)
                                           throws IOException
                                   {
                                       Log.w(TAG, "Corrupt server state, restarting application...");
                                       sendAppStartPageRequestAsync();
                                   }
                               });

                }
                else if (this._instanceId == responseAsJSON.get("InstanceId").asInt())
                {
                    // The instance that we're on now matches the server instance, so we can safely ignore
                    // the sync error (the request that caused it was sent against a previous instance).
                }
                else
                {
                    // We got a sync error, and the current instance on the server is different that our
                    // instance.  It's possible that the response with the new (correct) instance is still
                    // coming, but unlikey (it would mean it had async/wait user code after page navigation,
                    // which it should not, or that it somehow got sent out of order with respect to this
                    // error response, perhaps over a separate connection that was somehow delayed, but
                    // will eventually complete).
                    //
                    // The best option in this situation is to request a Resync with the server...
                    //
                    Log.w(TAG, "ERROR - client state out of sync - need resync");
                    this.sendResyncInstanceRequestAsync();
                }
            }
            else
            {
                // Some other kind of error (ClientError or UserCodeError).
                //
                // !!! Maybe we should allow them to choose an option to get more details?  Configurable on the server?
                //
                messageBox("Application Error", "The application experienced an error.  Please contact your administrator.", "Close", "close", new ICommandHandler()
                           {
                               @Override
                               public void CommandHandler(String command)
                               {
                               }
                           });
            }

            return;
        }

        boolean updateRequired = false;

        if (responseAsJSON.get("App") != null) // This means we have a new app
        {
            // Note that we already have an app definition from the MaaasApp that was passed in.  The App in this
            // response was triggered by a request at app startup for the current version of the app metadata
            // fresh from the endpoint (which may have updates relative to whatever we stored when we first found
            // the app at this endpoint and recorded its metadata).
            //
            // !!! Do we want to update our stored app defintion (in MaaasApp, via the AppManager)?  Maybe only if changed?
            //
            _appDefinition = (JObject) responseAsJSON.get("App");
            Log.i(TAG, String.format("Got app definition for: %s - %s", _appDefinition.get("name").asString(), _appDefinition.get("description").asString()));
            this.sendAppStartPageRequestAsync();
            return;
        }
        else if ((responseAsJSON.get("ViewModel") != null) && (responseAsJSON.get("View") != null)) // ViewModel and View - means we have a new page/screen
        {
            this._instanceId = responseAsJSON.get("InstanceId").asInt();
            this._instanceVersion = responseAsJSON.get("InstanceVersion").asInt();

            JObject jsonViewModel = (JObject) responseAsJSON.get("ViewModel");

            this._viewModel.InitializeViewModelData(jsonViewModel);

            this._path = responseAsJSON.get("Path").asString();
            Log.i(TAG, String.format("Got ViewModel for new view - path: '%s', instanceId: %d, instanceVersion: %d", this._path, this._instanceId, this._instanceVersion));

            this._isBackSupported = responseAsJSON.get("Back").asBoolean();

            JObject jsonPageView = (JObject)responseAsJSON.get("View");
            _onProcessPageView.ProcessPageView(jsonPageView);

            // If the view model is dirty after rendering the page, then the changes are going to have been
            // written by new view controls that produced initial output (such as location or sensor controls).
            // We need to signal than a viewModel "Update" is required to get these changes to the server.
            //
            updateRequired = this._viewModel.IsDirty();
        }
        else if (responseAsJSON.get("ViewModel") != null) // ViewModel without View (resync)
        {
            int responseInstanceId = responseAsJSON.get("InstanceId").asInt();
            if (responseInstanceId == this._instanceId)
            {
                int responseInstanceVersion = responseAsJSON.get("InstanceVersion").asInt();

                JObject jsonViewModel = (JObject) responseAsJSON.get("ViewModel");

                this._viewModel.SetViewModelData(jsonViewModel);

                Log.i(TAG, String.format("Got ViewModel resync for existing view - path: '%s', instanceId: %d, instanceVersion: %d", this._path, this._instanceId, this._instanceVersion));
                this._viewModel.UpdateViewFromViewModel(null, null);
            }
            else if (responseInstanceId < this._instanceId)
            {
                // Resync response was for a previous instance, so we can safely ignore it (we've moved on).
            }
            else
            {
                // Incorrect instance id on resync - For this to happen, we'd have to get a resync for a "future" instance (meaning one for which
                // we haven't seen the initial view/viewModel).  This should never happen, but if it does, it's not clear how to recover from it.
                // Requesting an "instance" resync might very well result in just hitting this case again repeatedy.  The only potential way out of
                // this (if it ever does happen) is to request the "big" resync.
                //
                Log.w(TAG, "ERROR - instance id mismatch (response instance id > local instance id), updates not applied - app resync requested");
                this.sendResyncRequestAsync();
                return;
            }
            Log.i(TAG, String.format("Got ViewModel for existing view - path: '%s', instanceId: %d, instanceVersion: %d", this._path, this._instanceId, this._instanceVersion));
            this._viewModel.UpdateViewFromViewModel(null, null);
        }
        else // Updating existing page/screen
        {
            int responseInstanceId = responseAsJSON.get("InstanceId").asInt();
            if (responseInstanceId == this._instanceId)
            {
                int responseInstanceVersion = responseAsJSON.get("InstanceVersion").asInt();

                // You can get a new view on a view model update if the view is dynamic and was updated
                // based on the previous command/update.
                //
                boolean viewUpdatePresent = (responseAsJSON.get("View") != null);

                if (responseAsJSON.get("ViewModelDeltas") != null)
                {
                    Log.i(TAG, String.format("Got ViewModelDeltas for path: '%s' with instanceId: %d and instanceVersion: %d", this._path, responseInstanceId, responseInstanceVersion));

                    if ((this._instanceVersion + 1) == responseInstanceVersion)
                    {
                        this._instanceVersion++;

                        JToken jsonViewModelDeltas = responseAsJSON.get("ViewModelDeltas");
                        // logger.Debug("ViewModel deltas: {0}", jsonViewModelDeltas);

                        // If we don't have a new View, we'll update the current view as part of applying
                        // the deltas.  If we do have a new View, we'll skip that, since we have to
                        // render the new View and do a full update anyway (below).
                        //
                        this._viewModel.UpdateViewModelData(jsonViewModelDeltas, !viewUpdatePresent);
                    }
                    else
                    {
                        // Instance version was not one more than current version on view model update
                        //
                        Log.w(TAG, "ERROR - instance version mismatch, updates not applied - need resync");
                        this.sendResyncInstanceRequestAsync();
                        return;
                    }
                }

                if (viewUpdatePresent)
                {
                    if (this._instanceVersion == responseInstanceVersion)
                    {
                        // Render the new page and bind/update it
                        //
                        this._path = responseAsJSON.get("Path").asString();
                        JObject jsonPageView = (JObject)responseAsJSON.get("View");
                        _onProcessPageView.ProcessPageView(jsonPageView);
                        updateRequired = this._viewModel.IsDirty();
                    }
                    else
                    {
                        // Instance version was not correct on view update
                        //
                        Log.w(TAG, "ERROR - instance version mismatch on view update - need resync");
                        this.sendResyncInstanceRequestAsync();
                        return;
                    }
                }
            }
            else if (responseInstanceId < this._instanceId)
            {
                // Response was for a previous instance, so we can safely ignore it (we've moved on).
            }
            else
            {
                // Incorrect instance id
                //
                Log.w(TAG, "ERROR - instance id mismatch (response instance id > local instance id), updates not applied - need resync");
                this.sendResyncInstanceRequestAsync();
                return;
            }
        }

        if (responseAsJSON.get("MessageBox") != null)
        {
            Log.i(TAG, "Launching message box...");
            JObject jsonMessageBox = (JObject)responseAsJSON.get("MessageBox");
            _onProcessMessageBox.ProcessMessageBox(jsonMessageBox, new ICommandHandler()
                                                   {
                                                       @Override
                                                       public void CommandHandler(String command)
                                                               throws IOException
                                                       {
                                                           Log.i(TAG, String.format("Message box completed with command: '%s'", command));
                                                           sendCommandRequestAsync(command, null);
                                                        }
                                                   });
        }
        else if (responseAsJSON.get("LaunchUrl") != null)
        {
            JObject jsonLaunchUrl = (JObject)responseAsJSON.get("LaunchUrl");
            _onProcessUrl.ProcessUrl(
                    (jsonLaunchUrl.get("primaryUrl") != null) ? jsonLaunchUrl.get("primaryUrl").asString() : null,
                    (jsonLaunchUrl.get("secondaryUrl") != null) ? jsonLaunchUrl.get("secondaryUrl").asString() : null
                                    );
        }

        if (responseAsJSON.get("NextRequest") != null)
        {
            Log.d(TAG, "Got NextRequest, composing and sending it now...");
            JObject requestObject = (JObject)responseAsJSON.get("NextRequest").deepClone();

            if (updateRequired)
            {
                Log.d(TAG, "Adding pending viewModel updates to next request (after request processing)");
                addDeltasToRequestObject(requestObject);
            }

            sendMessageProcessResponseAsync(_app.getSessionId(), requestObject);
        }
        else if (updateRequired)
        {
            Log.d(TAG, "Sending pending viewModel updates (after request processing)");
            this.sendUpdateRequestAsync();
        }
    }

    public void sendMessageProcessResponseAsync(final String sessionId, final JObject requestObject)
    {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params)
            {
                try
                {
                    ProcessResponseAsync(StateManager.this._transport.sendMessage(sessionId, requestObject));
                }
                catch (IOException e)
                {
                    Log.wtf(TAG, e);
                    ProcessRequestFailure(requestObject, e);
                }
                return null;
            }
        }.execute();
    }

    public void startApplicationAsync()
    {
        Log.i(TAG, String.format("Loading Synchro application definition for app at: %s", _app.getEndpoint()));
        JObject requestObject = new JObject();

        requestObject.put("Mode", new JValue("AppDefinition"));
        requestObject.put("TransactionId", new JValue(0));

        sendMessageProcessResponseAsync(null, requestObject);
    }

    private void sendAppStartPageRequestAsync()
    {
        this._path = _appDefinition.get("main").asString();

        Log.i(TAG, String.format("Request app start page at path: '%s'", this._path));

        JObject requestObject = new JObject();

        requestObject.put("Mode", new JValue("Page"));
        requestObject.put("Path", new JValue(this._path));
        requestObject.put("TransactionId", new JValue(getNewTransactionId()));
        requestObject.put("DeviceMetrics", this.PackageDeviceMetrics()); // Send over device metrics (these won't ever change, per session)
        requestObject.put("ViewMetrics", this.PackageViewMetrics(_deviceMetrics.getCurrentOrientation())); // Send over view metrics

        sendMessageProcessResponseAsync(_app.getSessionId(), requestObject);
    }

    private void sendResyncInstanceRequestAsync()
    {
        Log.i(TAG, String.format("Sending resync for path: '%s'", this._path));

        JObject requestObject = new JObject();

        requestObject.put("Mode", new JValue("Resync"));
        requestObject.put("Path", new JValue(this._path));
        requestObject.put("TransactionId", new JValue(getNewTransactionId()));
        requestObject.put("InstanceId", new JValue(this._instanceId));
        requestObject.put("InstanceVersion", new JValue(this._instanceVersion));

        sendMessageProcessResponseAsync(_app.getSessionId(), requestObject);
    }

    private boolean addDeltasToRequestObject(JObject requestObject)
    {
        Map<String, JToken> vmDeltas = this._viewModel.CollectChangedValues();
        if (vmDeltas.size() > 0)
        {
            JArray deltas = new JArray();
            for (Map.Entry<String, JToken> entry : vmDeltas.entrySet())
            {
                JObject newObject = new JObject();

                newObject.put("path", new JValue(entry.getKey()));
                newObject.put("value", entry.getValue().deepClone());

                deltas.add(newObject);
            }

            requestObject.put("ViewModelDeltas", deltas);

            return true;
        }

        return false;
    }

    public void sendUpdateRequestAsync()
    {
        Log.d(TAG, String.format("Process update for path: '%s'", this._path));

        // We check dirty here, even though addDeltas is a noop if there aren't any deltas, in order
        // to avoid generating a new transaction id when we're not going to do a new transaction.
        //
        if (this._viewModel.IsDirty())
        {
            JObject requestObject = new JObject();

            requestObject.put("Mode", new JValue("Update"));
            requestObject.put("Path", new JValue(this._path));
            requestObject.put("TransactionId", new JValue(getNewTransactionId()));
            requestObject.put("InstanceId", new JValue(this._instanceId));
            requestObject.put("InstanceVersion", new JValue(this._instanceVersion));

            if (addDeltasToRequestObject(requestObject))
            {
                // Only going to send the updates if there were any changes...
                sendMessageProcessResponseAsync(_app.getSessionId(), requestObject);
            }
        }
    }

    public void sendCommandRequestAsync(String command, JObject parameters)
    {
        Log.i(TAG, String.format("Sending command: '%s' for path: '%s'", command, this._path));

        JObject requestObject = new JObject();

        requestObject.put("Mode", new JValue("Command"));
        requestObject.put("Path", new JValue(this._path));
        requestObject.put("TransactionId", new JValue(getNewTransactionId()));
        requestObject.put("InstanceId", new JValue(this._instanceId));
        requestObject.put("InstanceVersion", new JValue(this._instanceVersion));
        requestObject.put("Command", new JValue(command));

        if (parameters != null)
        {
            requestObject.put("Parameters", parameters);
        }

        addDeltasToRequestObject(requestObject);

        sendMessageProcessResponseAsync(_app.getSessionId(), requestObject);
    }

    public void sendBackRequestAsync()
    {
        Log.i(TAG, String.format("Sending 'back' for path: '%s'", this._path));

        JObject requestObject = new JObject();

        requestObject.put("Mode", new JValue("Back"));
        requestObject.put("Path", new JValue(this._path));
        requestObject.put("TransactionId", new JValue(getNewTransactionId()));
        requestObject.put("InstanceId", new JValue(this._instanceId));
        requestObject.put("InstanceVersion", new JValue(this._instanceVersion));

        sendMessageProcessResponseAsync(_app.getSessionId(), requestObject);
    }

    public void sendViewUpdateAsync(SynchroOrientation orientation)
    {
        Log.i(TAG, String.format("Sending ViewUpdate for path: '%s'", this._path));

        // Send the updated view metrics
        JObject requestObject = new JObject();

        requestObject.put("Mode", new JValue("ViewUpdate"));
        requestObject.put("Path", new JValue(this._path));
        requestObject.put("TransactionId", new JValue(getNewTransactionId()));
        requestObject.put("InstanceId", new JValue(this._instanceId));
        requestObject.put("InstanceVersion", new JValue(this._instanceVersion));
        requestObject.put("ViewMetrics", this.PackageViewMetrics(orientation));

        sendMessageProcessResponseAsync(_app.getSessionId(), requestObject);
    }

    // If your app has a session, but no other state, such as on recovery from tombstoning, you
    // can call this method instead of startApplicationAsync().  The server will respond with the
    // full state required to resume your app.
    //
    // This method should only be called in a restart from tombstoning state.  For example, if a
    // user had navigated into the app and then shut it down via the operating system, when they
    // restart they do not expect to return to where they were (as they would with this method),
    // they expect to return to the entry sreen of the app.
    //

    public void sendResyncRequestAsync()
    {
        Log.i(TAG, "Sending resync (no path/instance)");

        JObject requestObject = new JObject();

        requestObject.put("Mode", new JValue("Resync"));
        requestObject.put("TransactionId", new JValue(getNewTransactionId()));

        sendMessageProcessResponseAsync(_app.getSessionId(), requestObject);
    }
}