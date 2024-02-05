package io.github.helpermethod.wipr

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.long
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.PipelineApi
import org.gitlab4j.api.models.Pipeline
import org.gitlab4j.api.models.PipelineFilter
import java.time.Instant
import java.util.Date
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Stream

class Wipr : CliktCommand() {
    private val gitlabServerUrl by option(help = "The GitLab server URL.").required()
    private val personalAccessToken by option(help = "The personal access token.").required()
    private val projectId by option(help = "The project ID.").long().required()
    private val updatedBefore by option(help = "Delete pipelines updated before the specified date.")
        .convert {
            Date.from(Instant.parse(it))
        }
        .required()

    override fun run() {
        GitLabApi(gitlabServerUrl, personalAccessToken).use { gitLabApi ->
            gitLabApi
                .pipelineApi
                .getPipelinesStream(projectId) {
                    withUpdatedBefore(updatedBefore)
                }
                .parallel()
                .map {
                    it.id
                }
                .forEach { pipelineId ->
                    gitLabApi
                        .pipelineApi
                        .deletePipeline(projectId, pipelineId)
                }
        }
    }
}

private fun PipelineApi.getPipelinesStream(
    projectId: Long,
    filter: PipelineFilter.() -> Unit,
): Stream<Pipeline> = getPipelinesStream(projectId, PipelineFilter().apply(filter))

fun main(args: Array<String>) {
    Wipr().main(args)
}
