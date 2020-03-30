package com.codehumane.messageschedule.config

import com.codehumane.messageschedule.application.MessageCreateCommand
import com.codehumane.messageschedule.application.MessageCreateOrderPrepareService
import com.codehumane.messageschedule.domain.MessageCreateOrder
import com.codehumane.messageschedule.domain.MessageCreateOrderRepository
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
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
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
        scheduledMessageCreateOrderItemReader: RepositoryItemReader<MessageCreateOrder>,
        scheduledMessageCreateOrderItemProcessor: ScheduledMessageCreateOrderItemProcessor,
        scheduledMessageCreateOrderItemWriter: ScheduledMessageCreateOrderItemWriter,
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
        messageCreateOrderRepository: MessageCreateOrderRepository,
        @Value("#{jobParameters[inputDateTime]}") inputDateTime: String?
    ) = RepositoryItemReaderBuilder<MessageCreateOrder>()
        .name("scheduledMessageCreateOrderItemReader")
        .repository(messageCreateOrderRepository)
        .methodName("findByScheduledAtIsGreaterThanAndScheduledAtIsLessThanEqual")
        .arguments(toScheduledMessageReadParameters(inputDateTime))
        .sorts(toScheduledMessageCreateOrderItemReadSort())
        .maxItemCount(properties.readerMaxItemCount)
        .pageSize(properties.readerFetchSize)
        .build()

    private fun toScheduledMessageReadParameters(inputDateTime: String?): List<LocalDateTime> {
        log.info("scheduled message inputDateTime: $inputDateTime")

        val baseDateTime = inputDateTime
            ?.let { LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) }
            ?: LocalDateTime.now()

        return listOf(
            baseDateTime.minusMinutes(1),
            baseDateTime
        )
    }

    private fun toScheduledMessageCreateOrderItemReadSort() = mapOf(
        "orderedAt" to Sort.Direction.ASC,
        "messageId" to Sort.Direction.ASC
    )

    @Component
    class ScheduledMessageCreateOrderItemProcessor : ItemProcessor<MessageCreateOrder, MessageCreateCommand> {
        override fun process(item: MessageCreateOrder) =
            MessageCreateCommand(
                item.messageId,
                item.title,
                item.contents,
                item.receiver
            )
    }

    @Component
    class ScheduledMessageCreateOrderItemWriter : ItemWriter<MessageCreateCommand> {

        private val log by logger()

        override fun write(items: MutableList<out MessageCreateCommand>) {
            items.forEach { log.info("create command: $it") }
            log.info("items written: $items")
        }
    }

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

}
