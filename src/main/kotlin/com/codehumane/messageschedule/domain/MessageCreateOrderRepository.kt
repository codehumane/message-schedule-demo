package com.codehumane.messageschedule.domain

import org.springframework.data.repository.CrudRepository

interface MessageCreateOrderRepository : CrudRepository<MessageCreateOrder, MessageId>