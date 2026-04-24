package com.businesshub.dashboard.web.response;

import java.util.List;

public record LeadImportResponse(
        int importedCount,
        int skippedCount,
        List<String> errors
) {
}
