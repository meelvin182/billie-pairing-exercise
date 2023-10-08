package io.billie.invoices.viewmodel

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.util.*
import javax.validation.constraints.NotBlank

@Table("INVOICES")
data class Invoice(
    @JsonProperty("invoice_id") val invoiceId: UUID?,
    @field:NotBlank @JsonProperty("organisation_id") val organisationId: String,
    @JsonFormat(pattern = "dd/MM/yyyy") @JsonProperty("creation_date") val creationDate: LocalDate,
    @JsonFormat(pattern = "dd/MM/yyyy") @JsonProperty("due_date") val dueDate: LocalDate,
    @JsonProperty("items") val invoiceItems: List<InvoiceItem>
)