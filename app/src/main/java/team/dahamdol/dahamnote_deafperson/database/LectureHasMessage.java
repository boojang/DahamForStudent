package team.dahamdol.dahamnote_deafperson.database;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class LectureHasMessage {
    @Embedded
    public LectureEntity lectureEntity;
    @Relation(
            parentColumn = "lectureId",
            entityColumn = "messageOwnerId"
    )
    public List<MessageEntity> messageEntities;
}
