/*
 * Copyright (c) 2021 Jannis Scheibe <jannis@tadris.de>
 *
 * This file is part of FitoTrack
 *
 * FitoTrack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FitoTrack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.tadris.fitness.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.ui.record.RecordGpsWorkoutActivity;
import de.tadris.fitness.ui.record.RecordIndoorWorkoutActivity;
import de.tadris.fitness.ui.record.RecordWorkoutActivity;
import de.tadris.fitness.ui.workout.ShowGpsWorkoutActivity;
import de.tadris.fitness.ui.workout.ShowIndoorWorkoutActivity;
import de.tadris.fitness.ui.workout.WorkoutActivity;
import de.tadris.fitness.util.Icon;

@Entity(tableName = "workout_type")
public class WorkoutType implements Serializable {

    public static final String WORKOUT_TYPE_ID_OTHER = "other";
    public static final String WORKOUT_TYPE_ID_RUNNING = "running";

    @PrimaryKey
    @NonNull
    public String id;

    public String title;

    @ColumnInfo(name = "min_distance")
    public int minDistance;

    public int color;

    public String icon;

    @ColumnInfo(name = "met")
    public int MET;

    @ColumnInfo(name = "type")
    public String recordingType;

    @Ignore
    @PluralsRes
    // Only for indoor workouts
    // treadmill -> "steps"
    public int repeatingExerciseName;

    @Ignore
    public WorkoutType(@NonNull String id, String title, int minDistance, int color, String icon, int MET, String recordingType) {
        this(id, title, minDistance, color, icon, MET, recordingType, -1);
    }

    @Ignore
    public WorkoutType(@NonNull String id, String title, int minDistance, int color, String icon, int MET, String recordingType, int repeatingExerciseName) {
        this.id = id;
        this.title = title;
        this.minDistance = minDistance;
        this.color = color;
        this.icon = icon;
        this.MET = MET;
        this.recordingType = recordingType;
        this.repeatingExerciseName = repeatingExerciseName;
    }

    public WorkoutType() {
    }

    @Ignore
    @JsonIgnore
    public RecordingType getRecordingType() {
        return RecordingType.findById(this.recordingType);
    }

    private static WorkoutType[] PRESETS = null;

    public static WorkoutType getWorkoutTypeById(Context context, String id) {
        buildPresets(context);
        for (WorkoutType type : PRESETS) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        WorkoutType type = Instance.getInstance(context).db.workoutTypeDao().findById(id);
        if (type == null && !id.equals(WORKOUT_TYPE_ID_OTHER)) {
            return getWorkoutTypeById(context, WORKOUT_TYPE_ID_OTHER);
        } else {
            return type;
        }
    }

    public static List<WorkoutType> getAllTypesSorted(Context context) {
        List<WorkoutType> list = getAllTypes(context);
        AppDatabase db = Instance.getInstance(context).db;
        Collections.sort(list, (o1, o2) -> -Long.compare(db.getLastWorkoutTimeByType(o1.id), db.getLastWorkoutTimeByType(o2.id)));
        return list;
    }

    public static List<WorkoutType> getAllTypes(Context context) {
        buildPresets(context);
        List<WorkoutType> result = new ArrayList<>(Arrays.asList(PRESETS));
        WorkoutType[] fromDatabase = Instance.getInstance(context).db.workoutTypeDao().findAll();
        result.addAll(Arrays.asList(fromDatabase));
        return result;
    }

    private static void buildPresets(Context context) {
        if (PRESETS != null) return; // Don't build a second time
        PRESETS = new WorkoutType[]{
                new WorkoutType(WORKOUT_TYPE_ID_RUNNING,
                        context.getString(R.string.workoutTypeRunning),
                        5,
                        context.getResources().getColor(R.color.colorPrimaryRunning),
                        Icon.RUNNING.name,
                        -1, RecordingType.GPS.id),
                new WorkoutType("walking",
                        context.getString(R.string.workoutTypeWalking),
                        5,
                        context.getResources().getColor(R.color.colorPrimaryRunning),
                        Icon.WALKING.name,
                        -1, RecordingType.GPS.id),
                new WorkoutType("hiking",
                        context.getString(R.string.workoutTypeHiking),
                        5,
                        context.getResources().getColor(R.color.colorPrimaryHiking),
                        Icon.WALKING.name,
                        -1, RecordingType.GPS.id),
                new WorkoutType("cycling",
                        context.getString(R.string.workoutTypeCycling),
                        10,
                        context.getResources().getColor(R.color.colorPrimaryBicycling),
                        Icon.CYCLING.name,
                        -1, RecordingType.GPS.id),
                new WorkoutType("inline_skating",
                        context.getString(R.string.workoutTypeInlineSkating),
                        7,
                        context.getResources().getColor(R.color.colorPrimarySkating),
                        Icon.INLINE_SKATING.name,
                        -1, RecordingType.GPS.id),
                new WorkoutType("skateboarding",
                        context.getString(R.string.workoutTypeSkateboarding),
                        7,
                        context.getResources().getColor(R.color.colorPrimarySkating),
                        Icon.SKATEBOARDING.name,
                        -1, RecordingType.GPS.id),
                new WorkoutType("rowing",
                        context.getString(R.string.workoutTypeRowing),
                        7,
                        context.getResources().getColor(R.color.colorPrimaryRowing),
                        Icon.ROWING.name,
                        -1, RecordingType.GPS.id),
                new WorkoutType(WORKOUT_TYPE_ID_OTHER,
                        context.getString(R.string.workoutTypeOther),
                        7,
                        context.getResources().getColor(R.color.colorPrimary),
                        Icon.OTHER.name,
                        0, RecordingType.GPS.id),
                new WorkoutType("treadmill",
                        context.getString(R.string.workoutTypeTreadmill),
                        5,
                        context.getResources().getColor(R.color.colorPrimaryRunning),
                        Icon.RUNNING.name,
                        -1, RecordingType.INDOOR.id,
                        R.plurals.workoutStep),
                new WorkoutType("rope_skipping",
                        context.getString(R.string.workoutTypeRopeSkipping),
                        3,
                        context.getResources().getColor(R.color.colorPrimary),
                        Icon.ROPE_SKIPPING.name,
                        11, RecordingType.INDOOR.id,
                        R.plurals.workoutJump),
                new WorkoutType("trampoline_jumping",
                        context.getString(R.string.workoutTypeTrampolineJumping),
                        3,
                        context.getResources().getColor(R.color.colorPrimary),
                        Icon.TRAMPOLINE_JUMPING.name,
                        4, RecordingType.INDOOR.id,
                        R.plurals.workoutJump),
                new WorkoutType("push-ups",
                        context.getString(R.string.workoutTypePushUps),
                        1,
                        context.getResources().getColor(R.color.colorPrimary),
                        Icon.PUSH_UPS.name,
                        6, RecordingType.INDOOR.id,
                        R.plurals.workoutPushUp),
        };
    }

    public enum RecordingType {

        INDOOR("indoor", RecordIndoorWorkoutActivity.class, ShowIndoorWorkoutActivity.class),
        GPS("gps", RecordGpsWorkoutActivity.class, ShowGpsWorkoutActivity.class);

        public final String id;
        public final Class<? extends RecordWorkoutActivity> recorderActivityClass;
        public final Class<? extends WorkoutActivity> showDetailsActivityClass;

        RecordingType(String id, Class<? extends RecordWorkoutActivity> recorderActivityClass, Class<? extends WorkoutActivity> showDetailsActivityClass) {
            this.id = id;
            this.recorderActivityClass = recorderActivityClass;
            this.showDetailsActivityClass = showDetailsActivityClass;
        }

        public static RecordingType findById(String id) {
            for (RecordingType type : values()) {
                if (type.id.equals(id)) {
                    return type;
                }
            }
            return GPS;
        }
    }
}
