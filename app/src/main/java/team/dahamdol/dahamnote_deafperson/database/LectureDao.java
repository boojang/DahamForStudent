package team.dahamdol.dahamnote_deafperson.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LectureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long createLecture(LectureEntity lectureEntity);

    @Delete
    void deleteLecture(LectureEntity lectureEntity);

    @Query("SELECT * FROM LectureEntity WHERE lectureId = :lectureId")
    LectureEntity readLectureByLectureId(long lectureId);

    @Query("SELECT * FROM LectureEntity WHERE lectureName = :lectureName")
    List<LectureEntity> readLectureByLectureName(String lectureName);

    @Query("SELECT * FROM LectureEntity")
    List<LectureEntity> readAllLectures();
}
