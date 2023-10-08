package io.billie.functional

import com.alibaba.fastjson.JSONObject
import io.billie.functional.data.Fixtures.createInvoiceRequest
import io.billie.functional.data.Fixtures.createUpdateInvoiceRequest
import io.billie.functional.matcher.IsUUID.isUuid
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CanStoreReadDeleteInvoices {

    companion object {
        @JvmStatic
        var postgres: PostgreSQLContainer<*> = PostgreSQLContainer<Nothing>(
            "postgres:13.2-alpine"
        ).apply {
            withDatabaseName("organisations")
            withReuse(true)
        }

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            postgres.start()
            postgres.waitingFor(Wait.forLogMessage(".*database system is ready to accept connections*", 2))
        }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun canStoreInvoice() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/invoices").contentType(MediaType.APPLICATION_JSON)
                .content(createInvoiceRequest())
        ).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(jsonPath("$.id").value(isUuid()))
    }

    @Test
    fun canDeleteInvoice() {
        val invoiceCreationResponse = mockMvc.perform(
            MockMvcRequestBuilders.post("/invoices").contentType(MediaType.APPLICATION_JSON)
                .content(createInvoiceRequest())
        ).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(jsonPath("$.id").value(isUuid()))
            .andReturn().response.contentAsString
        val invoiceId = JSONObject.parseObject(invoiceCreationResponse).getString("id")

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/invoices/$invoiceId").contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(jsonPath("$.id").value(isUuid()))
    }


    @Test
    fun getBadRequestOnExistingInvoice() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/invoices").contentType(MediaType.APPLICATION_JSON)
                .content(createUpdateInvoiceRequest())
        ).andExpect(MockMvcResultMatchers.status().isBadRequest())
    }

    @Test
    fun orgs() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/invoices").contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk())
    }
}