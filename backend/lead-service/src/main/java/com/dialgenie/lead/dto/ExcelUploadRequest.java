package com.dialgenie.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelUploadRequest {
    private String campaignId;
    private List<ExcelLeadRow> rows;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ExcelLeadRow {
    private String name;
    private String phoneNumber;
    private String email;
    private String concern;
}
