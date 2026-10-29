import type { Location } from "./Location";

export interface ActiveBus {
    id: number;
    busPlate: string;
    location: Location;
    nextSequence: number;
    nextRouteStationId: number;
    active: boolean;
    color: string;
    isAtStation: boolean;
    lastVisitedRouteStationId?: number;
    etas: Record<number, number>;
    lastEtaCalculationTime: number;
    currentOccupancy?: number;
    maxCapacity?: number;
}
