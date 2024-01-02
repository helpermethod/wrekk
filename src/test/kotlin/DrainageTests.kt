import com.github.ajalt.clikt.testing.test
import com.github.tomakehurst.wiremock.WireMockServer
import io.github.helpermethod.drainage.Drainage
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.wiremock.ListenerMode
import io.kotest.extensions.wiremock.WireMockListener
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

private const val RANDOM_PORT = 0

class DrainageTests : FunSpec({
    val gitlabServer = WireMockServer(RANDOM_PORT)
    listeners(WireMockListener(gitlabServer, ListenerMode.PER_SPEC))
    val testClock = Clock.fixed(Instant.parse("2024-01-02T00:00:00Z"), ZoneId.of("UTC"))

    context("drainage") {
        test("should display usage instructions if no options are provided") {
            assertSoftly(Drainage(testClock).test()) {
                statusCode shouldBe 1
                stderr shouldContain
                    """
                    Usage: drainage [<options>]
                    
                    Error: missing option --gitlab-server-url
                    Error: missing option --personal-access-token
                    Error: missing option --project-id
                    Error: missing option --retention-in-days
                    """.trimIndent()
            }
        }
        // TODO http://localhost:8080/__admin/recorder/
        // TODO start
        // TODO replace URL with http://localhost:8080
        // TODO stop
        // TODO curl -s -H "Private-Token: glpat-yRxXiAE7mgRzhABBxcQ2" 'http://localhost:8080/api/v4/projects/2634/pipelines?order_by=updated_at&updated_before=2023-10-04T00:00Z'
        // TODO https://graalvm.github.io/native-build-tools/latest/gradle-plugin-quickstart.html
        // TODO cat api_v4_projects_5454_pipelines-370f0af9-2383-4b75-967d-f45d780d57e6.json | jq '.response.body | fromjson'
        test("should delete expired pipelines") {
            val result =
                Drainage(testClock).test(
                    "--gitlab-server-url=${gitlabServer.baseUrl()}",
                    "--personal-access-token=glpat-r7nNUMgXSY-4PFwsaEvN",
                    "--project-id=5454",
                    "--retention-in-days=90",
                )

            assertSoftly(result) {
                statusCode shouldBe 0
            }
        }
    }
})
