package org.dromara.insurance.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 投保凭证 PDF 生成结果。
 */
@Data
@AllArgsConstructor
public class VoucherPdfResult {

    private String fileName;

    private byte[] content;
}
