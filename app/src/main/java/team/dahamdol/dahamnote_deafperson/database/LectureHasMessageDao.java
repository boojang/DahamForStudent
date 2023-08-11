package team.dahamdol.dahamnote_deafperson.database;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public interface LectureHasMessageDao {
    @Transaction
    @Query("SELECT * FROM LectureEntity WHERE lectureId = :lectureId")
    LectureHasMessage readLectureHasMessageByLectureId(long lectureId);
}
