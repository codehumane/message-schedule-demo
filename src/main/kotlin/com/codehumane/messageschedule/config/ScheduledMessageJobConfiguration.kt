package com.codehumane.messageschedule.config

import com.codehumane.messageschedule.application.*
import com.codehumane.messageschedule.config.ScheduledMessageJobConfiguration.Companion.jobName
import com.codehumane.messageschedule.domain.MessageCreateOrder
import com.codehumane.messageschedule.properties.ScheduledMessageCreateJobProperties
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.listener.JobExecutionListenerSupport
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ScheduledMessageCreateJobProperties::class)
@ConditionalOnProperty(name = ["spring.batch.job.names"], havingValue = jobName)
class ScheduledMessageJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {

    companion object {
        const val jobName = "scheduledMessageCreateJob"
    }

    @Bean(name = [jobName])
    fun scheduledMessageCreateJob(
        scheduledMessagePrepareStep: Step,
        scheduledMessageCreateStep: Step,
        messageScheduleJobCompletionListener: JobExecutionListenerSupport
    ) = jobBuilderFactory
        .get("scheduledMessageCreateJob")
        .listener(messageScheduleJobCompletionListener)
        .start(scheduledMessagePrepareStep)
        .next(scheduledMessageCreateStep)
        .build()

    @Bean
    @JobScope
    fun scheduledMessagePrepareStep(
        scheduledMessagePrepareTasklet: ScheduledMessagePrepareTasklet
    ) = stepBuilderFactory
        .get("scheduledMessagePrepareStep")
        .tasklet(scheduledMessagePrepareTasklet)
        .build()

    @Bean
    @JobScope
    fun scheduledMessageCreateStep(
        scheduledMessageCreateOrderItemReaderBuilder: ScheduledMessageCreateOrderItemReaderBuilder,
        scheduledMessageCreateOrderItemProcessor: ScheduledMessageCreateOrderItemProcessor,
        scheduledMessageCreateOrderItemWriter: ScheduledMessageCreateOrderItemWriter,
        properties: ScheduledMessageCreateJobProperties
    ) = stepBuilderFactory
        .get("scheduledMessageCreateStep")
        .chunk<MessageCreateOrder, MessageCreateCommand>(properties.chunkSize)
        .reader(scheduledMessageCreateOrderItemReaderBuilder.build())
        .processor(scheduledMessageCreateOrderItemProcessor)
        .writer(scheduledMessageCreateOrderItemWriter)
        .build()

}
