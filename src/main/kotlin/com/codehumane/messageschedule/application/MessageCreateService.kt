package com.codehumane.messageschedule.application

import com.codehumane.messageschedule.application.data.MessageCreateCommand
import com.codehumane.messageschedule.logger
import org.springframework.stereotype.Service

@Service
class MessageCreateService {

    private val log by logger()

    fun create(command: MessageCreateCommand) = log.info("create command: $command")

}
