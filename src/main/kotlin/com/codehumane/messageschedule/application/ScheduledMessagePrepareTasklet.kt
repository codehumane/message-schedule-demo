package com.codehumane.messageschedule.application

import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.stereotype.Component

@Component
class ScheduledMessagePrepareTasklet(
    private val messageCreateOrderPrepareService: MessageCreateOrderPrepareService
) : Tasklet {

    override fun execute(
        contribution: StepContribution,
        chunkContext: ChunkContext
    ): RepeatStatus {
        messageCreateOrderPrepareService.prepare()
        return RepeatStatus.FINISHED
    }

}
