package io.billie.invoices.viewmodel

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.util.*

@Table("INVOICE_ITEMS")
data class InvoiceItem(
    @JsonProperty("invoice_item_id") val invoiceItemId: UUID?,
    @JsonProperty("invoice_id") val invoiceId: UUID,
    val quantity: Int,
    @JsonProperty("price_per_item") val pricePerItem: BigDecimal
)