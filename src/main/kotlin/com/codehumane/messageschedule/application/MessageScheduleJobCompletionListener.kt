package com.codehumane.messageschedule.application

import com.codehumane.messageschedule.logger
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.listener.JobExecutionListenerSupport
import org.springframework.stereotype.Component

@Component
class MessageScheduleJobCompletionListener : JobExecutionListenerSupport() {

    private val log by logger()

    override fun beforeJob(jobExecution: JobExecution) {
        super.beforeJob(jobExecution)
        log.info("beforeJob - jobExecution: $jobExecution")
    }

    override fun afterJob(jobExecution: JobExecution) {
        super.afterJob(jobExecution)
        log.info("afterJob - status: ${jobExecution.status}, jobExecution: $jobExecution")
    }

}
