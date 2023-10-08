package io.billie.invoices.service

import io.billie.invoices.data.InvoiceRepository
import io.billie.invoices.viewmodel.Invoice
import org.springframework.stereotype.Service
import java.util.*

@Service
class InvoiceService(val invoiceRepository: InvoiceRepository) {

    fun findAll() = invoiceRepository.findAllInvoices()

    fun findInvoice(invoiceId: String) = invoiceRepository.findInvoiceById(UUID.fromString(invoiceId))

    fun createInvoice(invoice: Invoice) = invoiceRepository.createInvoice(invoice)

    fun deleteInvoice(invoiceId: String) = invoiceRepository.deleteInvoiceById(UUID.fromString(invoiceId))

}