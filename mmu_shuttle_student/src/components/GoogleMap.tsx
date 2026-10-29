import { Map, AdvancedMarker, useMap } from "@vis.gl/react-google-maps";
import { GOOGLE_MAPS_ID, MMU_LOCATION } from "../shared/constants";
import type { GoogleMapProps } from "../interfaces/props/GoogleMapProps";
import MapPolyline from "./MapPolyline";
import BusIcon from "./BusIcon";
import StationPin from "./StationPin";
import GoogleMapLoading from "./GoogleMapLoading";
import { useEffect } from "react";

const GoogleMap = ({ routeLine, activeBuses, stations }: GoogleMapProps) => {
    const map = useMap(GOOGLE_MAPS_ID);

    useEffect(() => {
        if (!map || !routeLine || routeLine.length === 0 || !window.google?.maps) return;

        const bounds = new window.google.maps.LatLngBounds();

        routeLine.forEach((point) => bounds.extend(point));

        map.fitBounds(bounds);
    }, [map, routeLine]);

    if (!routeLine || routeLine.length === 0) {
        return <GoogleMapLoading />;
    }
    return <>
        <Map
            defaultZoom={14.5}
            defaultCenter={MMU_LOCATION}
            mapId={GOOGLE_MAPS_ID}
            disableDefaultUI={true}
            gestureHandling="greedy"
        >
            {routeLine && routeLine.length > 0 && <MapPolyline path={routeLine} />}

            {stations && stations.length > 0 && (() => {
                const seen = new Set<string>();
                return stations.map((station, index) => {
                    const key = `${station.location.lat},${station.location.lng}`;
                    if (seen.has(key)) return null;
                    seen.add(key);
                    return (
                        <AdvancedMarker
                            key={`station-${station.id}`}
                            position={station.location}
                        >
                            <StationPin name={station.name} sequence={index} />
                        </AdvancedMarker>
                    );
                });
            })()}

            {activeBuses && activeBuses.length > 0 && activeBuses.map((bus, index) => (
                <AdvancedMarker
                    key={index}
                    position={bus.location}
                >
                    <div>
                        <BusIcon carPlate={bus.busPlate} color={bus.color} />
                    </div>
                </AdvancedMarker>
            ))}
        </Map></>
}

export default GoogleMap;