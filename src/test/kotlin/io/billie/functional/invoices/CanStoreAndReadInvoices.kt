package io.billie.functional.invoices

import io.billie.invoices.data.InvoiceRepository
import io.billie.invoices.viewmodel.Invoice
import io.billie.invoices.viewmodel.InvoiceItem
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@SpringBootTest
class CanStoreAndReadInvoices {

    @Autowired
    private lateinit var invoiceRepository: InvoiceRepository

    companion object {
        @JvmStatic
        var postgres: PostgreSQLContainer<*> = PostgreSQLContainer<Nothing>(
            "postgres:13.2-alpine"
        ).apply {
            withDatabaseName("organisations")
            withReuse(true)
           // withInitScript("db/init.sql")
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


    @Test
    fun canCreateAnEmptyInvoice(){
        val invoice = Invoice(
            null,
            UUID.randomUUID(),
            LocalDate.now(),
            LocalDate.now().plusDays(1),
            emptyList()
        )
        invoiceRepository.createInvoice(invoice)
        val invoices = invoiceRepository.findAllInvoices()
        Assertions.assertEquals(1, invoices.size)
        invoiceRepository.deleteInvoiceById(invoices.first().invoiceId!!)
        Assertions.assertEquals(0, invoiceRepository.findAllInvoices().size)

    }


}