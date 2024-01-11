package io.github.helpermethod.plumber

import com.github.ajalt.clikt.testing.test
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.extensions.wiremock.ListenerMode
import io.kotest.extensions.wiremock.WireMockListener
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PlumberTests : FunSpec({
    val gitlabServer = WireMockServer(options().dynamicPort())
    listeners(WireMockListener(gitlabServer, ListenerMode.PER_SPEC))

    context("plumber") {
        test("should display usage instructions if no options are provided") {
            assertSoftly(Plumber().test()) {
                statusCode shouldBe 1
                stderr shouldContain
                    """ 
                    Usage: plumber [<options>]

                    Error: missing option --gitlab-server-url
                    Error: missing option --personal-access-token
                    Error: missing option --project-id
                    Error: missing option --updated-before
                    """.trimIndent()
            }
        }
        context("should delete expired pipelines") {
            withData(
                nameFn = { (projectId, _) -> projectId },
                "1" to 0,
                "2" to 48,
                "3" to 144,
            ) { (projectId, deletions) ->
                gitlabServer.stubFor(
                    delete(urlPathMatching("/api/v4/projects/$projectId/pipelines/([1-9][0-9]*)"))
                        .willReturn(ok()),
                )

                val result =
                    Plumber().test(
                        "--gitlab-server-url=${gitlabServer.baseUrl()}",
                        "--personal-access-token=glpat-r7nNUMgXSY-4PFwsaEvN",
                        "--project-id=$projectId",
                        "--updated-before=2023-12-03T00:00:00Z",
                    )

                gitlabServer.verify(deletions, deleteRequestedFor(urlPathMatching("/api/v4/projects/$projectId/pipelines/[1-9][0-9]*")))

                assertSoftly(result) {
                    statusCode shouldBe 0
                }
            }
        }
    }
})
