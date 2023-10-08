package io.billie.invoices.resource

import io.billie.invoices.service.InvoiceService
import io.billie.invoices.viewmodel.Invoice
import io.billie.invoices.viewmodel.InvoiceResourceError
import io.billie.organisations.viewmodel.Entity
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("invoices")
class InvoiceResource(val service: InvoiceService) {

    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200", description = "Get all invoices", content = [(Content(
                mediaType = "application/json", array = (ArraySchema(schema = Schema(implementation = Invoice::class)))
            ))]
        )]
    )
    @GetMapping
    fun findAll(): Collection<Invoice> {
        return service.findAll()
    }

    @DeleteMapping("/{invoiceId}")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200", description = "Deleted the invoice", content = [(Content(
                mediaType = "application/json", array = (ArraySchema(schema = Schema(implementation = Entity::class)))
            ))]
        ), ApiResponse(
            responseCode = "400", description = "Bad request", content = [Content(
                mediaType = "application/json", array = (ArraySchema(schema = Schema(implementation = Error::class)))
            )]
        )]
    )
    fun delete(@PathVariable invoiceId: String): Entity {
        if (service.findInvoice(invoiceId = invoiceId) == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice does not exist")
        }
        service.deleteInvoice(invoiceId = invoiceId)
        return Entity(UUID.fromString(invoiceId))
    }

    @PostMapping
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200", description = "Accepted the new invoice", content = [(Content(
                mediaType = "application/json", array = (ArraySchema(schema = Schema(implementation = Entity::class)))
            ))]
        ), ApiResponse(
            responseCode = "400", description = "Bad request", content = [Content(
                mediaType = "application/json", array = (ArraySchema(schema = Schema(implementation = Error::class)))
            )]
        ), ApiResponse(
            responseCode = "400", description = "Bad payload", content = [Content(
                mediaType = "application/json",
                array = (ArraySchema(schema = Schema(implementation = InvoiceResourceError::class)))
            )]
        )]
    )
    fun post(@Valid @RequestBody invoice: Invoice): Entity {
        if (invoice.invoiceId != null) {
            //update not implemented
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice already exists, please delete it first")
        }
        return Entity(service.createInvoice(invoice))
    }

    @GetMapping("/{invoiceId}")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200", description = "Get invoice by id", content = [(Content(
                mediaType = "application/json", array = (ArraySchema(schema = Schema(implementation = Invoice::class)))
            ))]
        ), ApiResponse(
            responseCode = "400", description = "Bad request", content = [Content(
                mediaType = "application/json", array = (ArraySchema(schema = Schema(implementation = Error::class)))
            )]
        )]
    )
    fun get(@PathVariable invoiceId: String): Invoice? {
        return service.findInvoice(invoiceId = invoiceId)
    }
}