package com.codehumane.messageschedule.domain

import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EmbeddedId
import javax.persistence.Entity

@Entity
class MessageCreateOrder(

    @EmbeddedId
    val id: MessageId,
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
        return id == other.id
    }

    override fun hashCode() = id.hashCode()

    override fun toString() =
        "MessageCreateOrder(" +
                "id=$id, " +
                "title='$title', " +
                "contents='$contents', " +
                "receiver='$receiver', " +
                "scheduledAt=$scheduledAt, " +
                "orderedAt=$orderedAt" +
                ")"

}
