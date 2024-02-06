package io.github.helpermethod.wrekk

import com.github.ajalt.clikt.testing.test
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@WireMockTest
class WrekkrTests {
    @Test
    fun `should display usage instructions if no options are provided`() {
        val result = Wrekk().test()

        assertAll(
            { assertEquals(1, result.statusCode) },
            {
                assertTrue {
                    result.stderr.contains(
                        """
                        Usage: wrekk [<options>]

                        Error: missing option --gitlab-server-url
                        Error: missing option --personal-access-token
                        Error: missing option --project-id
                        Error: missing option --updated-before
                        """.trimIndent()
                    )
                }
            }
        )
    }

    @MethodSource
    @ParameterizedTest(name = "{1}")
    fun `should delete expired pipelines`(projectId: String, deletions: Int, wireMockRuntimeInfo: WireMockRuntimeInfo) {
        stubFor(
            delete(urlPathMatching("/api/v4/projects/$projectId/pipelines/([1-9][0-9]*)"))
                .willReturn(ok()),
        )

        val result =
            Wrekk().test(
                "--gitlab-server-url=${wireMockRuntimeInfo.httpBaseUrl}",
                "--personal-access-token=glpat-r7nNUMgXSY-4PFwsaEvN",
                "--project-id=$projectId",
                "--updated-before=2023-12-03T00:00:00Z",
            )

        verify(
            deletions,
            deleteRequestedFor(urlPathMatching("/api/v4/projects/$projectId/pipelines/[1-9][0-9]*"))
        )

        assertEquals(0, result.statusCode)
    }

    companion object {
        @JvmStatic
        fun `should delete expired pipelines`(): Stream<Arguments> =
            Stream.of(
                arguments("1", 0),
                arguments("2", 48),
                arguments("3", 144)
            )
    }
}
