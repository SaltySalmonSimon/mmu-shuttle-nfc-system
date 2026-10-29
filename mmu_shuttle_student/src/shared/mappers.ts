import type { Route } from "../interfaces/models/Route";
import type { Station } from "../interfaces/models/Station";
import type { Location } from "../interfaces/models/Location";
import type { ActiveBus } from "../interfaces/models/ActiveBus";

export const mapRouteResponseToRouteModel = (apiData: any): Route => {
    return {
        id: apiData?.id,
        routeName: apiData?.name,
        totalStations: apiData?.totalStation,
        isLive: apiData?.live,
        color: apiData?.color,

        stations: apiData?.stationDetailResponse?.map((station: any): Station => ({
            id: station?.id,
            name: station?.name,
            sequence: station?.sequence,
            schedule: station?.schedules || [],
            location: {
                lat: station?.locationModel?.latitude,
                lng: station?.locationModel?.longitude
            }
        })) || [],

        routeLine: apiData?.routeLines?.map((line: any): Location => ({
            lat: line?.latitude,
            lng: line?.longitude
        })) || []
    };
};


export const mapActiveBusResponseToActiveBusModel = (apiData: any): ActiveBus => {
    const etas = Object.entries(apiData?.etas ?? {}).reduce((mappedEtas, [routeStationId, eta]) => {
        mappedEtas[Number(routeStationId)] = Number(eta);
        return mappedEtas;
    }, {} as Record<number, number>);

    return {
        id: Number(apiData?.id),
        busPlate: apiData?.busPlate,
        location: {
            lat: Number(apiData?.location?.latitude),
            lng: Number(apiData?.location?.longitude)
        },
        nextSequence: Number(apiData?.nextSequence),
        nextRouteStationId: Number(apiData?.nextBusRouteStationId),
        active: Boolean(apiData?.active),
        color: apiData?.color,
        isAtStation: Boolean(apiData?.isAtStation ?? apiData?.atStation),
        lastVisitedRouteStationId: apiData?.lastVisitedRouteStationId ?? undefined,
        etas,
        lastEtaCalculationTime: Number(apiData?.lastEtaCalculationTime ?? 0),
        currentOccupancy: Number(apiData?.currentOccupancy ?? 0),
        maxCapacity: Number(apiData?.maxCapacity ?? 40)
    };
};
