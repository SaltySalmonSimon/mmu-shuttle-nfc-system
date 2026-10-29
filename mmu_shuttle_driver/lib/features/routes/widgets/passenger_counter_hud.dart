import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:mmu_shuttle_driver/core/network/api.dart';

class PassengerCounterHud extends StatefulWidget {
  final int vehicleId;
  final int maxCapacity;

  const PassengerCounterHud({
    super.key,
    required this.vehicleId,
    this.maxCapacity = 40,
  });

  @override
  State<PassengerCounterHud> createState() => _PassengerCounterHudState();
}

class _PassengerCounterHudState extends State<PassengerCounterHud> {
  int _currentOccupancy = 0;
  Timer? _pollingTimer;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _fetchOccupancy();
    // Poll every 2.5 seconds for live card scan updates
    _pollingTimer = Timer.periodic(const Duration(milliseconds: 2500), (_) {
      _fetchOccupancy();
    });
  }

  @override
  void dispose() {
    _pollingTimer?.cancel();
    super.dispose();
  }

  Future<void> _fetchOccupancy() async {
    if (widget.vehicleId <= 0) return;
    try {
      final response = await dio.get('/boarding/occupancy/${widget.vehicleId}');
      if (response.statusCode == 200 && mounted) {
        final newCount = response.data is int ? response.data as int : int.tryParse(response.data.toString()) ?? 0;
        if (newCount != _currentOccupancy) {
          // Trigger subtle haptic buzz when count changes (e.g. student just tapped)
          HapticFeedback.lightImpact();
          setState(() {
            _currentOccupancy = newCount;
          });
        }
      }
    } catch (_) {
      // Silently retry on next poll cycle
    }
  }

  Future<void> _resetOccupancy() async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Reset Passenger Count?'),
        content: const Text('This will reset current passenger tally to 0 for the new trip.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('Reset to 0', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );

    if (confirm != true) return;

    try {
      setState(() => _isLoading = true);
      await dio.post('/boarding/reset/${widget.vehicleId}');
      HapticFeedback.heavyImpact();
      if (mounted) {
        setState(() {
          _currentOccupancy = 0;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Color _getStatusColor() {
    final ratio = _currentOccupancy / widget.maxCapacity;
    if (ratio < 0.5) return const Color(0xFF10B981); // Emerald green
    if (ratio < 0.85) return Colors.amber.shade700;
    return Colors.red.shade600;
  }

  @override
  Widget build(BuildContext context) {
    final statusColor = _getStatusColor();
    final ratio = (_currentOccupancy / widget.maxCapacity).clamp(0.0, 1.0);

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.08),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: statusColor.withOpacity(0.12),
                      shape: BoxShape.circle,
                    ),
                    child: Icon(Icons.nfc, color: statusColor, size: 22),
                  ),
                  const SizedBox(width: 10),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'PASSENGER LOAD',
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w700,
                          color: Colors.grey,
                          letterSpacing: 0.8,
                        ),
                      ),
                      Row(
                        children: [
                          Text(
                            '$_currentOccupancy',
                            style: TextStyle(
                              fontSize: 22,
                              fontWeight: FontWeight.bold,
                              color: statusColor,
                            ),
                          ),
                          Text(
                            ' / ${widget.maxCapacity} seats',
                            style: const TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.w500,
                              color: Colors.black54,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ],
              ),
              IconButton(
                icon: _isLoading
                    ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))
                    : const Icon(Icons.refresh, color: Colors.black45),
                tooltip: 'Reset Count for Next Trip',
                onPressed: _isLoading ? null : _resetOccupancy,
              ),
            ],
          ),
          const SizedBox(height: 10),
          ClipRRect(
            borderRadius: BorderRadius.circular(6),
            child: LinearProgressIndicator(
              value: ratio,
              minHeight: 6,
              backgroundColor: Colors.grey.shade200,
              valueColor: AlwaysStoppedAnimation<Color>(statusColor),
            ),
          ),
        ],
      ),
    );
  }
}
