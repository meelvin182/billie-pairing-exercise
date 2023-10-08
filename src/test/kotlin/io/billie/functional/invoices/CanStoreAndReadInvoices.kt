package io.billie.functional.invoices

import io.billie.functional.data.Fixtures
import io.billie.invoices.data.InvoiceRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

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
    fun canCreateInvoice() {
        val cntBefore = invoiceRepository.findAllInvoices().size
        invoiceRepository.createInvoice(Fixtures.invoice)
        val invoices = invoiceRepository.findAllInvoices()
        Assertions.assertEquals(cntBefore + 1, invoices.size)
        Assertions.assertEquals(1, invoices.first().invoiceItems.size)
    }

    @Test
    fun canDeleteInvoice() {
        var allInvoices = invoiceRepository.findAllInvoices()
        if (allInvoices.isEmpty()) {
            invoiceRepository.createInvoice(Fixtures.invoice)
            allInvoices = invoiceRepository.findAllInvoices()
        }
        val cntBefore = allInvoices.size
        invoiceRepository.deleteInvoiceById(allInvoices.first().invoiceId!!)
        allInvoices = invoiceRepository.findAllInvoices()
        Assertions.assertEquals(cntBefore - 1, allInvoices.size)
    }


}