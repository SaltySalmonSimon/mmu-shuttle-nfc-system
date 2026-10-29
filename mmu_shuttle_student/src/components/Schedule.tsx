import { useState } from "react";
import type { ScheduleProps } from "../interfaces/props/ScheduleProps";
import { SCHEDULE_VIEW_LIMIT } from "../shared/constants";

const Schedule = ({ schedule = [] }: ScheduleProps) => {
    const [isExpanded, setIsExpanded] = useState(false);

    const safeSchedule = Array.isArray(schedule) ? schedule : [];
    if (safeSchedule.length === 0) {
        return (
            <div className="w-full mt-2 text-left">
                <span className="text-[13px] text-slate-400 italic">No schedule posted for this stop.</span>
            </div>
        );
    }

    const limit = SCHEDULE_VIEW_LIMIT;
    const hasMore = safeSchedule.length > limit;
    const visibleSchedule = isExpanded ? safeSchedule : safeSchedule.slice(0, limit);

    return (
        <div className="w-full mt-2 text-left">
            <h4 className="text-[16px] font-bold text-slate-800 mb-5 tracking-tight">Scheduled:</h4>

            <div className="grid grid-cols-4 gap-y-7 md:gap-y-8 items-start w-full">
                {visibleSchedule.map((timeStr, index) => {
                    const isFirstCol = index % 4 === 0;

                    const timePart = timeStr.slice(0, 5);
                    const ampmPart = timeStr.slice(5);

                    return (
                        <div
                            key={timeStr}
                            className={`flex flex-col justify-start ${!isFirstCol ? 'border-l border-gray-200 pl-2 sm:pl-3 md:pl-4' : 'pr-1 md:pr-2'
                                }`}
                        >
                            <span className="text-[14px] sm:text-[15px] md:text-[16px] text-slate-700 font-medium leading-tight">
                                {timePart} <span className="text-[13px] md:text-[14px] text-slate-500 font-normal">{ampmPart}</span>
                            </span>
                        </div>
                    );
                })}
            </div>

            {hasMore && (
                <button
                    onClick={() => setIsExpanded(!isExpanded)}
                    className="mt-8 text-[13px] md:text-[14px] font-medium text-blue-700 hover:text-blue-800 transition-colors"
                >
                    {isExpanded ? "View Less" : "View More"}
                </button>
            )}
        </div>
    );
};

export default Schedule;
