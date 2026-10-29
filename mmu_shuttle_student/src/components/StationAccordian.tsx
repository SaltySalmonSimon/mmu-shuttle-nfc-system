import { useState } from 'react';
import type { StationAccordianProps } from '../interfaces/props/StationAccordianProps';
import Schedule from './Schedule';
import BusArrivalTimeCard from './BusArrivalTimeCard';
import BusIcon from './BusIcon';
import { Skeleton } from "@mui/material";
import Alert from "./Alert";

const StationAccordian = ({ stations, activeBuses, isLoading, isError, errorMessage }: StationAccordianProps) => {
    const [expandedId, setExpandedId] = useState<number | null>(0);

    if (isLoading) {
        return (
            <div className="w-full h-full py-5 pr-6 pl-6 md:py-6 md:pr-8 md:pl-8 bg-white overflow-hidden">
                <Skeleton variant="text" className="!w-32 !h-8 mb-8" />
                <div className="relative ml-[34px] md:ml-[42px] mt-4 flex flex-col gap-10 w-full">
                    {Array.from({ length: 5 }).map((_, i) => (
                        <div key={i} className="flex items-center gap-6 w-full relative">
                            <Skeleton variant="circular" className="!w-3 !h-3 shrink-0 absolute -left-[1.5px]" />
                            <Skeleton variant="text" className="!w-[60%] !h-6 ml-6" />
                        </div>
                    ))}
                </div>
            </div>
        );
    }

    if (isError) {
        return (
            <div className="p-6 md:p-8 w-full h-full bg-white flex flex-col items-start gap-4">
                <Alert type="error" title="Error" message={errorMessage!} />
            </div>
        );
    }

    if (!stations || stations.length === 0) {
        return (
            <div className="p-6 md:p-8 w-full h-full bg-white flex flex-col items-start gap-4">
                <Alert type="info" title="No stations" message="No stations found for this route." />
            </div>
        );
    }

    const hasActiveBuses = activeBuses && activeBuses.length > 0;

    return (
        <div className={`w-full h-full pt-5 pb-24 pr-6 md:py-6 md:pr-8 transition-all duration-300 bg-white font-sans ${hasActiveBuses ? 'pl-16 md:pl-24' : 'pl-6 md:pl-8'}`}>
            <h2 className="text-[20px] md:text-[22px] font-bold text-slate-900 mb-8 tracking-tight">Upcoming Stops</h2>

            <div className="relative ml-10 md:ml-12 mt-4">
                <div className="flex flex-col">
                    {stations.map((station, index) => {
                        const isExpanded = expandedId === index;
                        const isLast = index === stations.length - 1;
                        const busesAtStation = activeBuses?.filter((bus) => {
                            const hasLastVisited = bus.lastVisitedRouteStationId != null;
                            return bus.isAtStation && hasLastVisited && bus.lastVisitedRouteStationId === station.id;
                        });
                        const busesInTransitFromStation = activeBuses?.filter((bus) => {
                            const hasLastVisited = bus.lastVisitedRouteStationId != null;
                            return !bus.isAtStation && hasLastVisited && bus.lastVisitedRouteStationId === station.id;
                        });
                        const busesHeadingToFirst = activeBuses?.filter((bus) => {
                            const hasLastVisited = bus.lastVisitedRouteStationId == null;
                            return !bus.isAtStation && hasLastVisited && bus.nextRouteStationId === station.id && bus.nextSequence == 1;
                        });
                        const stationBusIcons = (busesAtStation ?? []).map((bus) => ({ bus }));
                        const etaBuses = activeBuses
                            ?.filter((bus) => bus.lastVisitedRouteStationId !== station.id && bus.etas?.[station.id] != null)
                            .sort((a, b) => a.etas[station.id] - b.etas[station.id]) ?? [];

                        return (
                            <div key={station.id} className="relative flex items-start">
                                {!isLast && (
                                    <div className="absolute left-[4px] top-[14px] bottom-0 w-[2px] bg-[#fbbf24]"></div>
                                )}
                                {index === 0 && (
                                    <div className="absolute left-[4px] top-[-22px] h-[22px] w-[2px] bg-[#fbbf24]"></div>
                                )}
                                <div className="absolute left-[-1.5px] top-[10px] w-3 h-3 rounded-full bg-[#fbbf24] shadow-[0_0_0_4px_white] z-10"></div>

                                {stationBusIcons.length > 0 && (
                                    <div className="absolute right-[calc(100%+14px)] top-[-4px] z-20 flex flex-row-reverse items-center justify-end -space-x-[26px] space-x-reverse md:-space-x-6 md:space-x-reverse">
                                        {stationBusIcons.map(({ bus }) => (
                                            <div
                                                key={`station-${bus.id}`}
                                                className="scale-[0.7] bg-white rounded-full shadow-[0_0_0_6px_white] shrink-0 transition-transform hover:-translate-y-0.5"
                                            >
                                                <BusIcon carPlate={bus.busPlate} alignTooltip="right" color={bus.color} />
                                            </div>
                                        ))}
                                    </div>
                                )}

                                {busesHeadingToFirst && busesHeadingToFirst.length > 0 && index === 0 && (
                                    <div className="absolute right-[calc(100%+14px)] top-[-34px] z-20 flex flex-row-reverse items-center justify-end -space-x-[26px] space-x-reverse md:-space-x-6 md:space-x-reverse">
                                        {busesHeadingToFirst.map((bus) => (
                                            <div
                                                key={`heading-${bus.id}`}
                                                className="scale-[0.7] bg-white rounded-full shadow-[0_0_0_6px_white] shrink-0 animate-pulse"
                                            >
                                                <BusIcon carPlate={bus.busPlate} alignTooltip="right" color={bus.color} />
                                            </div>
                                        ))}
                                    </div>
                                )}

                                {busesInTransitFromStation && busesInTransitFromStation.length > 0 && !isLast && (
                                    <div className="absolute right-[calc(100%+14px)] top-[calc(50%+6px)] -translate-y-1/2 z-20 flex flex-row-reverse items-center justify-end -space-x-[26px] space-x-reverse md:-space-x-6 md:space-x-reverse">
                                        {busesInTransitFromStation.map((bus) => (
                                            <div
                                                key={`transit-${bus.id}`}
                                                className="scale-[0.7] bg-white rounded-full shadow-[0_0_0_6px_white] shrink-0 animate-pulse"
                                            >
                                                <BusIcon carPlate={bus.busPlate} alignTooltip="right" color={bus.color} />
                                            </div>
                                        ))}
                                    </div>
                                )}

                                <div className={`pl-6 md:pl-8 w-full ${isLast ? 'pb-20' : 'pb-10'}`}>
                                    <button
                                        onClick={() => setExpandedId(isExpanded ? null : index)}
                                        className="text-[15px] md:text-[16px] font-bold text-slate-800 hover:text-slate-600 transition-colors text-left flex items-center justify-between w-full h-8"
                                    >
                                        {station.name}
                                    </button>

                                    <div
                                        className={`grid transition-all duration-300 ease-in-out ${isExpanded ? 'grid-rows-[1fr] opacity-100 mt-2 md:mt-3' : 'grid-rows-[0fr] opacity-0 mt-0'
                                            }`}
                                    >
                                        <div className="overflow-hidden">
                                            <div className="pt-2 pr-2">
                                                {index !== 0 && etaBuses.length > 0 && (
                                                    <BusArrivalTimeCard
                                                        isLiveEta
                                                        arrivals={etaBuses.map((bus) => ({
                                                            busPlate: bus.busPlate,
                                                            time: `${bus.etas[station.id]} min`,
                                                            occupancy: bus.currentOccupancy ?? 0,
                                                            capacity: bus.maxCapacity ?? 40
                                                        }))}
                                                    />
                                                )}
                                                <Schedule schedule={station.schedule} />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}

export default StationAccordian;