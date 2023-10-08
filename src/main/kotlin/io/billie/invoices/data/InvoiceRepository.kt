package io.billie.invoices.data

import io.billie.invoices.viewmodel.Invoice
import io.billie.invoices.viewmodel.InvoiceItem
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.Date
import java.sql.ResultSet
import java.util.*


@Repository
class InvoiceRepository(val jdbcTemplate: JdbcTemplate) {


    @Transactional(readOnly = true)
    fun findAllInvoices(): Collection<Invoice> {
        return findAll()
    }

    @Transactional
    fun findInvoiceById(id: UUID): Invoice? {
        return findInvoicesById(id)
    }

    @Transactional
    fun deleteInvoiceById(id: UUID) {
        jdbcTemplate.update(
        "delete from organisations_schema.invoices where id = ?;",
        id
        )
    }

    @Transactional
    fun createInvoice(invoice: Invoice): UUID {
        return createSingleInvoice(invoice)
    }

    private fun createSingleInvoice(invoice: Invoice): UUID {
        val keyHolder: KeyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            { connection ->
                val ps = connection.prepareStatement(
                    "INSERT INTO organisations_schema.invoices(organisation_id, creation_date, due_date) VALUES (?, ?, ?);",
                    arrayOf("id")
                )
                ps.setObject(1, invoice.organisationId)
                ps.setDate(2, Date.valueOf(invoice.creationDate))
                ps.setDate(3, Date.valueOf(invoice.dueDate))
                ps
            }, keyHolder
        )

        val invoiceId = keyHolder.getKeyAs(UUID::class.java)!!

        createInvoiceItems(invoiceId, invoice.invoiceItems)

        return invoiceId
    }

    private fun createInvoiceItems(invoice_id: UUID, items: List<InvoiceItem>) {
        items.forEach {
            val keyHolder: KeyHolder = GeneratedKeyHolder()
            jdbcTemplate.update ({ connection ->
                val ps = connection.prepareStatement(
                    "INSERT INTO organisations_schema.invoice_items (invoice_id quantity, price_per_item) VALUES (?, ?, ?) ;".trim(),
                    arrayOf("id")
                )
                ps.setObject(1, invoice_id)
                ps.setInt(2, it.quantity)
                ps.setBigDecimal(3, it.pricePerItem)

                ps
            }, keyHolder)
        }
    }

    private fun findAll(): Collection<Invoice> = jdbcTemplate.query(
        "SELECT id, organisation_id, creation_date, due_date FROM organisations_schema.invoices;".trim(),
        invoiceMapper()
    ).filterNotNull().apply { fulfilInvoicesWithItem(this) }


    private fun fulfilInvoicesWithItem(invoices: Collection<Invoice>): Collection<Invoice> {
        return invoices.map { invoice ->
            invoice.copy(invoiceItems = findInvoiceItemsByInvoiceId(invoice.invoiceId!!))
        }
    }

    private fun findInvoicesById(id: UUID): Invoice = jdbcTemplate.query(
        "SELECT id, organisation_id, creation_date, due_date FROM organisations_schema.invoices WHERE id = ?".trim(),
        invoiceMapper(),
        id
    ).filterNotNull().map { invoice ->
        invoice.copy(invoiceItems = findInvoiceItemsByInvoiceId(invoice.invoiceId!!))
    }.first()


    private fun findInvoiceItemsByInvoiceId(invoiceId: UUID) = jdbcTemplate.query(
        "SELECT id, invoice_id, quantity, price_per_item FROM organisations_schema.invoice_items WHERE id = ?",
        invoiceItemMapper(),
        invoiceId
    ).filterNotNull()

    private fun invoiceItemMapper(): RowMapper<InvoiceItem> {
        return RowMapper<InvoiceItem> { it: ResultSet, _: Int ->
            val invoiceItemId = it.getString(1)
            val invoiceId = it.getString(2)
            val quantity = it.getInt(3)
            val price = it.getBigDecimal(4)

            return@RowMapper InvoiceItem(
                UUID.fromString(invoiceItemId),
                UUID.fromString(invoiceId),
                quantity,
                price
            )

        }
    }

    private fun invoiceMapper() = RowMapper<Invoice> { it: ResultSet, _: Int ->

        val id = it.getString(1)
        val organisationId = it.getString(2)
        val creationDate = it.getDate(3).toLocalDate()
        val dueDate = it.getDate(4).toLocalDate()

        // Even kotlin automatically returns the last statement, I like to be explicit
        return@RowMapper Invoice(
            UUID.fromString(id), UUID.fromString(organisationId), creationDate, dueDate, emptyList()
        )
    }

}