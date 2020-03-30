package com.codehumane.messageschedule.application

import com.codehumane.messageschedule.domain.MessageCreateOrder
import com.codehumane.messageschedule.domain.MessageCreateOrderRepository
import com.codehumane.messageschedule.logger
import com.codehumane.messageschedule.properties.ScheduledMessageCreateJobProperties
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@JobScope
@Component
class ScheduledMessageCreateOrderItemReaderBuilder(
    private val properties: ScheduledMessageCreateJobProperties,
    private val messageCreateOrderRepository: MessageCreateOrderRepository,
    @Value("#{jobParameters[inputDateTime]}") private val inputDateTime: String?
) {

    private val log by logger()
    private val inputDateTimePattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    fun build(): RepositoryItemReader<MessageCreateOrder> {
        return RepositoryItemReaderBuilder<MessageCreateOrder>()
            .name("scheduledMessageCreateOrderItemReader")
            .repository(messageCreateOrderRepository)
            .methodName("findByScheduledAtIsGreaterThanAndScheduledAtIsLessThanEqual")
            .arguments(toScheduledMessageReadParameters(inputDateTime))
            .sorts(toScheduledMessageCreateOrderItemReadSort())
            .maxItemCount(properties.readerMaxItemCount)
            .pageSize(properties.readerFetchSize)
            .build()
    }

    private fun toScheduledMessageReadParameters(inputDateTime: String?): List<LocalDateTime> {
        log.info("scheduled message inputDateTime: $inputDateTime")

        val baseDateTime = inputDateTime
            ?.let { LocalDateTime.parse(it, inputDateTimePattern) }
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

}
