package com.businesshub.dashboard.web.api;

import com.businesshub.dashboard.service.InvoiceService;
import com.businesshub.dashboard.web.request.CreateInvoiceRequest;
import com.businesshub.dashboard.web.request.UpdateInvoiceStatusRequest;
import com.businesshub.dashboard.web.response.ApiResponseMapper;
import com.businesshub.dashboard.web.response.InvoiceResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceApiController {

    private final InvoiceService invoiceService;
    private final ApiResponseMapper apiResponseMapper;

    public InvoiceApiController(InvoiceService invoiceService, ApiResponseMapper apiResponseMapper) {
        this.invoiceService = invoiceService;
        this.apiResponseMapper = apiResponseMapper;
    }

    @GetMapping
    public List<InvoiceResponse> getInvoices() {
        return apiResponseMapper.toInvoiceResponses(invoiceService.getAllInvoices());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        return apiResponseMapper.toInvoiceResponse(invoiceService.createInvoice(request));
    }

    @PatchMapping("/{invoiceId}/status")
    public InvoiceResponse updateInvoiceStatus(@PathVariable Long invoiceId,
                                               @Valid @RequestBody UpdateInvoiceStatusRequest request) {
        return apiResponseMapper.toInvoiceResponse(invoiceService.updateStatus(invoiceId, request.getStatus()));
    }
}
