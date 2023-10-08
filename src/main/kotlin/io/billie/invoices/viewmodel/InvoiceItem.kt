package io.billie.invoices.viewmodel

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("INVOICE_ITEMS")
data class InvoiceItem(
    val quantity: Int,
    @JsonProperty("price_per_item") val pricePerItem: BigDecimal
)