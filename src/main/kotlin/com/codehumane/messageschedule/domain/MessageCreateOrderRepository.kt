package com.codehumane.messageschedule.domain

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageCreateOrderRepository : CrudRepository<MessageCreateOrder, MessageId> {

    fun findByScheduledAtIsNotNull(): List<MessageCreateOrder>

}
