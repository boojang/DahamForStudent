package team.dahamdol.dahamnote_deafperson.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = LectureEntity.class,
        parentColumns = "lectureId",
        childColumns = "messageOwnerId",
        onDelete = ForeignKey.CASCADE))

public class MessageEntity {
    @PrimaryKey(autoGenerate = true)
    public long messageId;

    @ColumnInfo(index = true)
    public long messageOwnerId;
    public String messageName;
    public String messageContent;

    public MessageEntity(long messageOwnerId, String messageName, String messageContent) {
        this.messageOwnerId = messageOwnerId;
        this.messageName = messageName;
        this.messageContent = messageContent;
    }
}
