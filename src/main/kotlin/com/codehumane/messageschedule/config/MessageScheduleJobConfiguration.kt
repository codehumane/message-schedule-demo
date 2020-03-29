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
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.listener.JobExecutionListenerSupport
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
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
    fun scheduledMessagePrepareStep(
        scheduledMessagePrepareTasklet: ScheduledMessagePrepareTasklet
    ) = stepBuilderFactory
        .get("scheduledMessagePrepareStep")
        .tasklet(scheduledMessagePrepareTasklet)
        .build()

    @Bean
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
    fun scheduledMessageCreateOrderItemReader(
        entityManagerFactory: EntityManagerFactory,
        properties: MessageScheduleJobProperties
    ) = JpaPagingItemReaderBuilder<MessageCreateOrder>()
        .saveState(true)
        .name("MessageSchedule")
        .maxItemCount(properties.readerMaxItemCount)
        .queryString(
            "select o " +
                    "from MessageCreateOrder o " +
                    "where o.scheduledAt is not null " +
                    "order by o.orderedAt asc, o.messageId asc"
        )
        .pageSize(properties.readerFetchSize)
        .entityManagerFactory(entityManagerFactory)
        .build()

    @Bean
    fun scheduledMessageCreateOrderItemProcessor(
        messageCreateOrderConverter: MessageCreateOrderConverter
    ) = ItemProcessor(messageCreateOrderConverter::convert)

    @Bean
    fun scheduledMessageCreateOrderItemWriter(
        messageCreateService: MessageCreateService
    ) = ItemWriter<MessageCreateCommand> { items ->
        items.forEach { messageCreateService.create(it) }
        log.info("items writed: $items")
    }

    @Component
    class ScheduledMessagePrepareTasklet(
        private val messageCreateOrderPrepareService: MessageCreateOrderPrepareService
    ) : Tasklet {
        override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
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
