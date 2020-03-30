package com.codehumane.messageschedule.config

import com.codehumane.messageschedule.logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
@EnableBatchProcessing
class JobConfiguration(
    @Value("\${spring.batch.job.names:NONE}") private val names: String?,
    private val jobs: Set<Job>
) {

    private val log by logger()

    @PostConstruct
    fun validateSpringBatchJobNames() {
        if (names.isNullOrBlank() || names == "NONE" || jobs.isEmpty()) {
            throw IllegalArgumentException("`spring.batch.job.names`에 지정된 값이 없거나 유효하지 않습니다($names).")
        }

        log.info("Job($jobs) configuration validated.")
    }

}
