package com.codehumane.messageschedule.config

import com.codehumane.messageschedule.application.MessageCreateOrderPrepareService
import com.codehumane.messageschedule.application.MessageCreateService
import com.codehumane.messageschedule.application.converter.MessageCreateOrderConverter
import com.codehumane.messageschedule.application.data.MessageCreateCommand
import com.codehumane.messageschedule.domain.MessageCreateOrder
import com.codehumane.messageschedule.logger
import com.codehumane.messageschedule.properties.MessageScheduleJobProperties
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.listener.JobExecutionListenerSupport
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.EntityManagerFactory

@Configuration
@EnableBatchProcessing
@EnableConfigurationProperties(MessageScheduleJobProperties::class)
class MessageScheduleJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {

    private val log by logger()

    @Bean
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
        scheduledMessageCreateOrderItemReader: ItemReader<MessageCreateOrder>,
        scheduledMessageCreateOrderItemProcessor: ItemProcessor<MessageCreateOrder, MessageCreateCommand>,
        scheduledMessageCreateOrderItemWriter: ItemWriter<MessageCreateCommand>,
        properties: MessageScheduleJobProperties
    ) = stepBuilderFactory
        .get("scheduledMessageCreateStep")
        .chunk<MessageCreateOrder, MessageCreateCommand>(properties.chunkSize)
        .reader(scheduledMessageCreateOrderItemReader)
        .processor(scheduledMessageCreateOrderItemProcessor)
        .writer(scheduledMessageCreateOrderItemWriter)
        .build()

    @Bean
    @JobScope
    fun scheduledMessageCreateOrderItemReader(
        entityManagerFactory: EntityManagerFactory,
        properties: MessageScheduleJobProperties,
        @Value("#{jobParameters[inputDateTime]}") inputDateTime: String?
    ) = JpaPagingItemReaderBuilder<MessageCreateOrder>()
        .name("MessageSchedule")
        .maxItemCount(properties.readerMaxItemCount)
        .queryString(toScheduledMessageReadQuery())
        .parameterValues(toScheduledMessageReadParameters(inputDateTime))
        .pageSize(properties.readerFetchSize)
        .entityManagerFactory(entityManagerFactory)
        .build()

    private fun toScheduledMessageReadQuery() = """
        select o
        from MessageCreateOrder o
        where o.scheduledAt is not null and o.scheduledAt > :from and o.scheduledAt <= :to
        order by o.orderedAt asc, o.messageId asc
        """
        .trimIndent()
        .replace("\n", " ")

    private fun toScheduledMessageReadParameters(inputDateTime: String?): Map<String, LocalDateTime> {
        log.info("scheduled message inputDateTime: $inputDateTime")

        val baseDateTime = inputDateTime
            ?.let { LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) }
            ?: LocalDateTime.now()

        return mapOf(
            "from" to baseDateTime.minusMinutes(1),
            "to" to baseDateTime
        )
    }

    @Bean
    @JobScope
    fun scheduledMessageCreateOrderItemProcessor(
        messageCreateOrderConverter: MessageCreateOrderConverter
    ) = ItemProcessor(messageCreateOrderConverter::convert)

    @Bean
    @JobScope
    fun scheduledMessageCreateOrderItemWriter(
        messageCreateService: MessageCreateService
    ) = ItemWriter<MessageCreateCommand> { items ->
        items.forEach { messageCreateService.create(it) }
        log.info("items written: $items")
    }

    @Component
    @JobScope
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

    @Component
    @JobScope
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

}
