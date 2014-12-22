- [ ] Util (no port, but added new platform-specific stuff to it)
- [ ] Logger
- [ ] Json
- [ ] JsonParser
- [ ] JsonWriter
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
