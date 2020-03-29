package com.codehumane.messageschedule.application.converter

import com.codehumane.messageschedule.application.data.MessageCreateCommand
import com.codehumane.messageschedule.domain.MessageCreateOrder
import org.springframework.stereotype.Component

@Component
class MessageCreateOrderConverter {

    fun convert(source: MessageCreateOrder) = MessageCreateCommand(
        source.messageId,
        source.title,
        source.contents,
        source.receiver
    )

}