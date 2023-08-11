package team.dahamdol.dahamnote_deafperson.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LectureEntity {
    @PrimaryKey(autoGenerate = true)
    public long lectureId;

    public String lectureName;
    public String lectureDate;

    public LectureEntity(String lectureName, String lectureDate) {
        this.lectureName = lectureName;
        this.lectureDate = lectureDate;
    }
}
