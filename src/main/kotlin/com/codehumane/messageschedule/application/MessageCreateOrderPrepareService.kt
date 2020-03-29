package com.codehumane.messageschedule.application

import com.codehumane.messageschedule.domain.MessageCreateOrder
import com.codehumane.messageschedule.domain.MessageCreateOrderRepository
import com.codehumane.messageschedule.domain.MessageId
import com.codehumane.messageschedule.logger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class MessageCreateOrderPrepareService(
    private val messageCreateOrderRepository: MessageCreateOrderRepository
) {

    private val log by logger()

    @Transactional
    fun prepare() {
        val orders = (1..10).map { toOrder(it) }
        messageCreateOrderRepository.saveAll(orders)
        log.info("orders created: $orders")
    }

    private fun toOrder(it: Int): MessageCreateOrder {
        val scheduledAt = if (it.rem(2) == 0) LocalDateTime.now().plusNanos(it * 1_000L) else null

        return MessageCreateOrder(
            MessageId("id$it"),
            "title$it",
            "contents$it",
            "receiver$it",
            scheduledAt
        )
    }

}
