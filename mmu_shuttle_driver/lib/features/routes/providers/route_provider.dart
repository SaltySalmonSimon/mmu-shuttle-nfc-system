import 'dart:async';

import 'package:battery_plus/battery_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:geolocator/geolocator.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:mmu_shuttle_driver/core/utils/ui.dart';
import 'package:mmu_shuttle_driver/features/routes/models/route_model.dart';
import 'package:mmu_shuttle_driver/features/routes/models/live_ride_model.dart';
import 'package:mmu_shuttle_driver/features/routes/services/location_service.dart';
import 'package:mmu_shuttle_driver/features/routes/services/notification_service.dart';
import 'package:mmu_shuttle_driver/features/routes/services/route_service.dart';

class RouteProvider extends ChangeNotifier {
  List<RouteModel> _routes = [];
  Set<Marker> _stationPoints = {};
  Set<Polyline> _routeLine = {};
  LatLng _initialCameraPosition = const LatLng(2.9278, 101.6443);
  LatLng? _currentBusPosition;
  BitmapDescriptor _busIcon = BitmapDescriptor.defaultMarkerWithHue(
    BitmapDescriptor.hueBlue,
  );

  RouteModel? _selectedRoute;
  final _routeService = RouteService();

  final _locationService = LocationService();
  final _notificationService = NotificationService();
  final _battery = Battery();

  bool _isLoading = true;
  String? _errorMesssage;
  int? _currentVehicleId;

  // getters
  List<RouteModel> get routes => _routes;
  RouteModel? get selectedRoute => _selectedRoute;
  Set<Marker> get stationPoints => _stationPoints;
  Set<Polyline> get routeLine => _routeLine;
  LatLng get initialCameraPosition => _initialCameraPosition;
  bool get isLoading => _isLoading;
  String? get errorMesssage => _errorMesssage;
  int? get currentVehicleId => _currentVehicleId;

  // methods
  void selectRoute(RouteModel route) {
    _selectedRoute = route;
  }

  void loadSelectedRoute() {
    if (selectedRoute == null) return;

    final rawBusStops = selectedRoute!.stations;

    if (rawBusStops.isNotEmpty) {
      _initialCameraPosition = LatLng(
        rawBusStops.first.latitude,
        rawBusStops.first.longitude,
      );
    }

    final namedStops = rawBusStops
        .where((stop) => stop.name.isNotEmpty)
        .toList();

    _stationPoints = namedStops.asMap().entries.map((entry) {
      final index = entry.key;
      final stop = entry.value;

      double markerHue;

      if (index == 0) {
        markerHue = BitmapDescriptor.hueGreen;
      } else {
        markerHue = BitmapDescriptor.hueAzure;
      }

      return Marker(
        markerId: MarkerId(stop.name),
        position: LatLng(stop.latitude, stop.longitude),
        infoWindow: InfoWindow(
          title: stop.name,
          snippet: index == 0 ? 'Starting Point' : 'Bus Stop',
        ),
        icon: BitmapDescriptor.defaultMarkerWithHue(markerHue),
      );
    }).toSet();

    _routeLine = {
      Polyline(
        polylineId: const PolylineId('main_route'),
        points: _selectedRoute!.routeLine,
        color: Colors.blue,
        width: 5,
        jointType: JointType.round,
      ),
    };
  }

  Future<void> startTracking({
    required int vehicleId,
    bool isResumed = false,
  }) async {
    _currentVehicleId = vehicleId;
    Position currentLocation;
    currentLocation = await _locationService.getCurrentLocation();
    await _notificationService.requestNotificationPermission();

    final startRideModel = LiveRideModel(
      routeId: selectedRoute!.id,
      vehicleId: vehicleId,
      latitude: currentLocation.latitude,
      longitude: currentLocation.longitude,
    );

    if (!isResumed) {
      await _routeService.startRoute(startRideModel);
    }
    _locationService.initializeConnection();
    await initializeBusIcon(currentLocation);
    updateBusLocation(currentLocation);
    notifyListeners();

    _locationService.startLocationStreaming(
      (Position position) async {
        _stationPoints.removeWhere(
          (m) => m.markerId == const MarkerId('shuttle_bus'),
        );
        updateBusLocation(position);
        notifyListeners();

        int? batteryLevel;
        bool? isCharging;
        try {
          batteryLevel = await _battery.batteryLevel;
          final state = await _battery.batteryState;
          isCharging =
              state == BatteryState.charging || state == BatteryState.full;
        } catch (_) {
          batteryLevel = null;
          isCharging = null;
        }

        final lifecycle = WidgetsBinding.instance.lifecycleState;
        final appState = lifecycle == AppLifecycleState.resumed
            ? 'foreground'
            : 'background';

        final liveRideModel = LiveRideModel(
          routeId: selectedRoute!.id,
          vehicleId: vehicleId,
          latitude: position.latitude,
          longitude: position.longitude,
          speed: position.speed,
          accuracy: position.accuracy,
          batteryLevel: batteryLevel,
          isCharging: isCharging,
          appState: appState,
          timestamp: position.timestamp,
        );

        _locationService.sendLiveLocation(liveRideModel);
      },
      (error) {
        print("GPS Stream Error: $error");
      },
    );
  }

  Future<void> stopTracking({bool isStoppedByUser = true}) async {
    _locationService.disconnect();
    _currentBusPosition = null;
    _currentVehicleId = null;
    if (isStoppedByUser) {
      if (selectedRoute == null) {
        throw Exception("No route selected");
      }
      await _routeService.stopRoute(selectedRoute!.id);
    }
    notifyListeners();
  }

  Future<void> initializeBusIcon(Position busPosition) async {
    final Uint8List markerIconBytes = await createBusMarkerBitmap(size: 100);

    _busIcon = BitmapDescriptor.fromBytes(markerIconBytes);
  }

  void updateBusLocation(Position busPosition) {
    _currentBusPosition = LatLng(busPosition.latitude, busPosition.longitude);
    if (_currentBusPosition != null) {
      _stationPoints.add(
        Marker(
          markerId: const MarkerId('shuttle_bus'),
          position: _currentBusPosition!,
          icon: _busIcon,
          rotation: 0.0,
          anchor: const Offset(0.5, 0.5),
          flat: true,
          infoWindow: const InfoWindow(title: 'MMU Shuttle'),
        ),
      );
    }
  }

  Future<bool> fetchRoutes() async {
    _setLoadingState(true);
    try {
      _routes = await _routeService.fetchRoutes();
      _setLoadingState(false);
      return true;
    } catch (e) {
      _setLoadingState(false, error: e.toString());
      return false;
    }
  }

  Future<bool> restoreRoutes(int routeId) async {
    bool isSuccess = true;

    if (routes.isEmpty) {
      isSuccess = await fetchRoutes();
    }

    if (isSuccess) {
      final routeIndex = routes.indexWhere((route) => route.id == routeId);

      if (routeIndex != -1) {
        _selectedRoute = routes[routeIndex];
        loadSelectedRoute();
        return true;
      } else {
        return false;
      }
    }

    return false;
  }

  void _setLoadingState(bool loading, {String? error}) {
    _isLoading = loading;
    _errorMesssage = error;
    notifyListeners();
  }

  void clearData() {
    _routes = [];
    _selectedRoute = null;
    _stationPoints = {};
    _routeLine = {};
    _currentBusPosition = null;
    _currentVehicleId = null;
    _selectedRoute = null;
    _isLoading = true;
    _errorMesssage = null;

    stopTracking(isStoppedByUser: false);
  }
}
