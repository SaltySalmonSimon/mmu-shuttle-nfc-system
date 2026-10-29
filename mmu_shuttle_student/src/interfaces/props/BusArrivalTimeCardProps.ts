export interface BusArrivalTimeCardProps {
    time?: string;
    isLiveEta?: boolean;
    arrivals?: {
        busPlate: string;
        time: string;
        occupancy?: number;
        capacity?: number;
    }[];
}
