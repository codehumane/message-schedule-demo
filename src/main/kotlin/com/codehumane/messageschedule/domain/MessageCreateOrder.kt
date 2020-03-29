package com.codehumane.messageschedule.domain

import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EmbeddedId
import javax.persistence.Entity

@Entity
class MessageCreateOrder(

    @EmbeddedId
    val messageId: MessageId,
    val title: String,
    val contents: String,
    val receiver: String,
    val scheduledAt: LocalDateTime? = null,
    val orderedAt: LocalDateTime = LocalDateTime.now()

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MessageCreateOrder
        return messageId == other.messageId
    }

    override fun hashCode() = messageId.hashCode()

    override fun toString() =
        "MessageCreateOrder(" +
                "id=$messageId, " +
                "title='$title', " +
                "contents='$contents', " +
                "receiver='$receiver', " +
                "scheduledAt=$scheduledAt, " +
                "orderedAt=$orderedAt" +
                ")"

}
