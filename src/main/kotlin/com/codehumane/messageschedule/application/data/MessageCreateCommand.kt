package com.codehumane.messageschedule.application.data

import com.codehumane.messageschedule.domain.MessageId

data class MessageCreateCommand(
    val id: MessageId,
    val title: String,
    val contents: String,
    val receiver: String
)