package com.codehumane.messageschedule.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MessageCreateOrderRepository : PagingAndSortingRepository<MessageCreateOrder, MessageId> {

    fun findByScheduledAtIsGreaterThanAndScheduledAtIsLessThanEqual(
        from: LocalDateTime,
        to: LocalDateTime,
        pageable: Pageable
    ): Page<MessageCreateOrder>

}
