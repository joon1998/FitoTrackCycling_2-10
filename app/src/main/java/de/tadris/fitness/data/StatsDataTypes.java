package de.tadris.fitness.data;

import java.util.Comparator;

public class StatsDataTypes {
    public static class DataPoint {
        public WorkoutType workoutType;
        public long workoutID;
        public long time;
        public double value;

        public DataPoint(WorkoutType type, long id, long start, double value) {
            this.workoutType = type;
            this.workoutID=id;
            this.time = start;
            this.value = value;
        }

        public static Comparator<DataPoint> timeComparator = (first, second) -> {
            if (first.time > second.time) {
                return 1;
            } else if (first.time < second.time) {
                return -1;
            } else {
                return 0;
            }
        };

        public static Comparator<DataPoint> valueComparator = (first, second) -> {
            if (first.value > second.value) {
                return 1;
            } else if (first.value < second.value) {
                return -1;
            } else {
                return 0;
            }
        };
    }

    public static class TimeSpan {
        public long startTime;
        public long endTime;

        public TimeSpan(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public boolean contains(long time) {
            return startTime <= time && time <= endTime;
        }

        public long length() {
            return this.endTime - this.startTime;
        }

        public enum Type {
            WEEK(0),
            MONTH(1),
            YEAR(2);

            private int id;

            Type(int i) {
                this.id = i;
            }

            public int getId() {
                return id;
            }
        }
    }
}
