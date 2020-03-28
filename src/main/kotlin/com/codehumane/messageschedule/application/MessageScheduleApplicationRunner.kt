package com.codehumane.messageschedule.application

import com.codehumane.messageschedule.domain.MessageCreateOrder
import com.codehumane.messageschedule.domain.MessageCreateOrderRepository
import com.codehumane.messageschedule.domain.MessageId
import com.codehumane.messageschedule.logger
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class MessageScheduleApplicationRunner(
    private val messageCreateOrderRepository: MessageCreateOrderRepository
) : ApplicationRunner {

    private val log by logger()

    override fun run(args: ApplicationArguments?) {
        val orders = toOrders()
        messageCreateOrderRepository.saveAll(orders)
        log.info("orders created: $orders")
    }

    private fun toOrders() = (1..4).map { toOrder(it) }

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