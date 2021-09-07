package de.tadris.fitness.data;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class BaseWorkout {

    @PrimaryKey
    public long id;

    public long start;
    public long end;

    public long duration;

    public long pauseDuration;

    public String comment;

    @ColumnInfo(name = "workoutType")
    @JsonProperty(value = "workoutType")
    public String workoutTypeId;

    @ColumnInfo(name = "avg_heart_rate")
    public int avgHeartRate = -1;

    @ColumnInfo(name = "max_heart_rate")
    public int maxHeartRate = -1;

    public int calorie;

    public boolean edited;

    // No foreign key is intended
    @ColumnInfo(name = "interval_set_used_id")
    public long intervalSetUsedId = 0;

    @JsonIgnore
    public String getDateString() {
        return SimpleDateFormat.getDateTimeInstance().format(new Date(start));
    }

    @JsonIgnore
    public String getSafeDateString() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(new Date(start));
    }

    @JsonIgnore
    public String getSafeComment() {
        if (comment == null) return "";
        String safeComment = this.comment.replaceAll("[^0-9a-zA-Z-_]+", "_"); // replace all unwanted chars by `_`
        return safeComment.substring(0, Math.min(safeComment.length(), 50)); // cut the comment after 50 Chars
    }

    @JsonIgnore
    public WorkoutType getWorkoutType(Context context) {
        return WorkoutType.getWorkoutTypeById(context, workoutTypeId);
    }

    @JsonIgnore
    public void setWorkoutType(WorkoutType workoutType) {
        this.workoutTypeId = workoutType.id;
    }

    @JsonIgnore
    public boolean hasHeartRateData() {
        return avgHeartRate > 0;
    }

}
