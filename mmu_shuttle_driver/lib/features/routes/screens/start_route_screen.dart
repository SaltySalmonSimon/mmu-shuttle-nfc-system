import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:mmu_shuttle_driver/core/exceptions/LocationException.dart';
import 'package:mmu_shuttle_driver/core/exceptions/NotificationException.dart';
import 'package:mmu_shuttle_driver/core/utils/toast.dart';
import 'package:mmu_shuttle_driver/core/widgets/google_map.dart';
import 'package:mmu_shuttle_driver/features/announcement/widgets/create_announcement_dialog.dart';
import 'package:mmu_shuttle_driver/features/routes/models/route_model.dart';
import 'package:mmu_shuttle_driver/features/routes/providers/route_provider.dart';
import 'package:mmu_shuttle_driver/features/routes/widgets/back_button.dart';
import 'package:mmu_shuttle_driver/features/routes/widgets/passenger_counter_hud.dart';
import 'package:mmu_shuttle_driver/features/routes/widgets/select_vehicle_dialog.dart';
import 'package:mmu_shuttle_driver/features/routes/widgets/start_route_card.dart';
import 'package:provider/provider.dart';

class StartRouteScreen extends StatefulWidget {
  final bool isOngoing;
  StartRouteScreen({super.key, this.isOngoing = false});

  @override
  State<StartRouteScreen> createState() => _StartRouteScreenState();
}

class _StartRouteScreenState extends State<StartRouteScreen> {
  late bool _isOngoing;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    final routeProvider = context.read<RouteProvider>();
    routeProvider.loadSelectedRoute();
    _isOngoing = widget.isOngoing;
    if (_isOngoing) {
      _startJourney(vehicleId: -1, isResumed: true);
    }
  }

  @override
  void dispose() async {
    super.dispose();
    try {
      final routeProvider = context.read<RouteProvider>();
      await routeProvider.stopTracking();
    } catch (e) {
      if (context.mounted) {
        showErrorToast(context, e.toString());
      }
    }
  }

  //methods
  void _onStartJourney() {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => SelectVehicleDialog(
        onSelected: (vehicle) => _startJourney(vehicleId: vehicle.id),
      ),
    );
  }

  void _startJourney({required int vehicleId, bool isResumed = false}) async {
    try {
      setState(() {
        _isLoading = true;
      });
      await context.read<RouteProvider>().startTracking(
        vehicleId: vehicleId,
        isResumed: isResumed,
      );
      if (!mounted) return;
      setState(() {
        _isOngoing = true;
      });

      showSuccessToast(
        context,
        isResumed
            ? 'Journey resumed successfully'
            : 'Journey started successfully',
      );
    } on LocationException catch (e) {
      showErrorToast(context, e.message);
    } on NotificationException catch (e) {
      showErrorToast(context, e.message);
    } catch (e) {
      showErrorToast(context, e.toString());
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  void _onSendUpdate() {
    showDialog(
      context: context,
      builder: (context) => CreateAnnouncementDialog(isFromLiveRide: true),
    );
  }

  void _onStopJourney() async {
    try {
      setState(() {
        _isLoading = true;
      });
      await context.read<RouteProvider>().stopTracking();
      setState(() {
        _isLoading = false;
        _isOngoing = false;
      });

      showSuccessToast(context, "Journey stopped successfully");
      _onBack();
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
      showErrorToast(context, e.toString());
    }
  }

  void _onBack() {
    if (context.mounted && !_isLoading) {
      if (context.canPop()) {
        context.pop();
      } else {
        context.go('/routes');
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    //initiate variable
    final routeProvider = context.read<RouteProvider>();
    final selectedRoute =
        routeProvider.selectedRoute ??
        RouteModel(
          id: -1,
          routeName: "no routes",
          totalStations: 0,
          stations: [],
          routeLine: [],
        );

    // passed value
    final routeName = selectedRoute.routeName;
    final totalStation = '${selectedRoute.totalStations} stations';

    return PopScope(
      canPop: !_isOngoing && !_isLoading,
      child: Scaffold(
        extendBodyBehindAppBar: true,
        appBar: AppBar(
          backgroundColor: Colors.transparent,
          elevation: 0,
          leading: Padding(
            padding: const EdgeInsets.only(left: 10.0, top: 0, bottom: 0),
            child: _isOngoing ? null : BackButtonWidget(onTap: _onBack),
          ),
        ),
        body: Stack(
          children: [
            GoogleMapWidget(),
            if (_isOngoing)
              Positioned(
                top: 10,
                left: 0,
                right: 0,
                child: SafeArea(
                  child: PassengerCounterHud(
                    vehicleId: routeProvider.currentVehicleId ?? 1,
                  ),
                ),
              ),
            StartRouteCard(
              title: routeName,
              totalStation: totalStation,
              isOngoing: _isOngoing,
              onStartJourney: _onStartJourney,
              onSendUpdate: _onSendUpdate,
              onStopJourney: _onStopJourney,
              isLoading: _isLoading,
            ),
          ],
        ),
      ),
    );
  }
}
