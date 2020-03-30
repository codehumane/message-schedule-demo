package com.codehumane.messageschedule.application

import com.codehumane.messageschedule.domain.MessageCreateOrder
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class ScheduledMessageCreateOrderItemProcessor : ItemProcessor<MessageCreateOrder, MessageCreateCommand> {

    override fun process(item: MessageCreateOrder) = MessageCreateCommand(
        item.messageId,
        item.title,
        item.contents,
        item.receiver
    )

}