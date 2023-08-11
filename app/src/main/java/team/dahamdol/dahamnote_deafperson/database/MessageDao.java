package team.dahamdol.dahamnote_deafperson.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MessageDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long createMessage(MessageEntity messages);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> createMessages(List<MessageEntity> messageEntities);

    @Query("SELECT * FROM MessageEntity WHERE messageOwnerId = :lectureId")
    List<MessageEntity> readMessagesByLectureId(long lectureId);

    @Delete
    void deleteMessage(MessageEntity messageEntity);

    @Delete
    void deleteMessages(List<MessageEntity> messageEntities);
}
