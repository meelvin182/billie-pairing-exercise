package io.billie.invoices.viewmodel


import java.util.*

sealed interface InvoiceResourceResponse
data class Entity(val id: UUID): InvoiceResourceResponse
data class InvoiceResourceError(val errors: List<String>): InvoiceResourceResponse