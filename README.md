- [X] Util (not sure what I'm gonna need so fine, done)
- [X] Logger
- [X] Json
- [X] JsonParser
- [X] JsonWriter
- [ ] SynchroApp (was MaaasApp, requires Json)

- [ ] DeviceMetrics/iOSDeviceMetrics
- [ ] Transport/TransportHttp (requires SynchroApp)

- [ ] BindingContext (requires Json)
- [ ] TokenConverter
- [ ] Binding (requires BindingContext)

- [ ] ViewModel (requires Binding and BindingContext)

- [ ] StateManager (requires Transport,ViewModel, DeviceMetrics)

- [ ] CommandInstance (requires BindingContext - needed by controls)

- [ ] ControlWrapper/iOSControlWrapper (requires CommandInstance, lots of stuff)

- [ ] PageView/iOSPageView (requires controls, ControlWrapper)

- [ ] All controls

- [ ] Synchro “Page" (MaaasPageViewController)

Rest of Synchro launcher app UX


Some peculiarities:

* BOM on seed.json caused fits. Not sure what to do with BOM on UTF-8 asset files. I presume http://stackoverflow.com/questions/4897876/reading-utf-8-bom-marker is relevant. I deleted the BOM.
* Slashes were not escaped on seed.json. Parser parsed it. Not sure if that's a problem.
