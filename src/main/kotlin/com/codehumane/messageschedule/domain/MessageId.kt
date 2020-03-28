package com.codehumane.messageschedule.domain

import java.io.Serializable
import javax.persistence.Embeddable

@Embeddable
data class MessageId(
    var id: String
) : Serializable
