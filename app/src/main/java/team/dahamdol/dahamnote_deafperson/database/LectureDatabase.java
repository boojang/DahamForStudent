package team.dahamdol.dahamnote_deafperson.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LectureEntity.class,MessageEntity.class}, version = 1)
public abstract class LectureDatabase extends RoomDatabase {
    public abstract LectureDao lectureDao();
    public abstract MessageDao messageDao();
    public abstract LectureHasMessageDao lectureHasMessageDao();

    private static LectureDatabase INSTANCE;

    private static final Object sLock = new Object();

    public static LectureDatabase getInstance(Context context) {
        synchronized (sLock) {
            if(INSTANCE==null) {
                INSTANCE= Room.databaseBuilder(context.getApplicationContext(),
                        LectureDatabase.class, "Lectures.db")
                        .build();
            }
            return INSTANCE;
        }
    }
}