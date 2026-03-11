package org.dromara.commission.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.dromara.commission.domain.CalcCommission;

@Data
@AllArgsConstructor
public class PolicyUnderwrittenEvent {
    private CalcCommission calcParam;
}
