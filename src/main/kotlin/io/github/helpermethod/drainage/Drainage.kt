package io.github.helpermethod.drainage

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.long
import org.gitlab4j.api.Constants.PipelineOrderBy.UPDATED_AT
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.PipelineApi
import org.gitlab4j.api.models.Pipeline
import org.gitlab4j.api.models.PipelineFilter
import java.time.Clock
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS
import java.util.Date
import java.util.stream.Stream

class Drainage(private val clock: Clock) : CliktCommand() {
    private val gitlabServerUrl by option(help = "The GitLab server URL.").required()
    private val personalAccessToken by option(help = "The personal access token.").required()
    private val projectId by option(help = "The project ID.").long().required()
    private val retentionInDays by option(help = "The retention in days.").long().required()

    override fun run() {
        val gitLabApi = GitLabApi(gitlabServerUrl, personalAccessToken)
        val expiration =
            ZonedDateTime
                .now(clock)
                .truncatedTo(DAYS)
                .minusDays(retentionInDays)
                .toDate()

        gitLabApi.pipelineApi
            .getPipelinesStream(projectId) {
                withOrderBy(UPDATED_AT)
                withUpdatedBefore(expiration)
            }
            .parallel()
            .map { it.id }
            .forEach { pipelineId ->
                gitLabApi.pipelineApi.deletePipeline(projectId, pipelineId)
            }
    }
}

private fun ZonedDateTime.toDate(): Date = Date.from(toInstant())

private fun PipelineApi.getPipelinesStream(
    projectId: Long,
    filter: PipelineFilter.() -> Unit,
): Stream<Pipeline> = getPipelinesStream(projectId, PipelineFilter().apply(filter))

fun main(args: Array<String>) {
    Drainage(Clock.systemUTC()).main(args)
}
