package org.dromara.insurance.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.insurance.domain.bo.InsuranceCardOrderBo;
import org.dromara.insurance.domain.dto.OrderInsureInfoDTO;
import org.dromara.insurance.domain.dto.PayWithBalanceReqDTO;
import org.dromara.insurance.domain.vo.InsuranceCardOrderVo;
import org.dromara.insurance.domain.vo.SaveInsureResultVO;

import java.util.Collection;
import java.util.List;

/**
 * 卡密订单Service接口
 *
 * @author li.xiang
 * @date 2026-05-12
 */
public interface IInsuranceCardOrderService {

    InsuranceCardOrderVo queryById(Long id);

    InsuranceCardOrderVo queryAdminById(Long id);

    InsuranceCardOrderVo queryByOrderNoForCurrentUser(String orderNo);

    TableDataInfo<InsuranceCardOrderVo> queryPageList(InsuranceCardOrderBo bo, PageQuery pageQuery);

    List<InsuranceCardOrderVo> queryList(InsuranceCardOrderBo bo);

    TableDataInfo<InsuranceCardOrderVo> queryAdminPageList(InsuranceCardOrderBo bo, PageQuery pageQuery);

    List<InsuranceCardOrderVo> queryAdminList(InsuranceCardOrderBo bo);

    Boolean insertByBo(InsuranceCardOrderBo bo);

    Boolean updateByBo(InsuranceCardOrderBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    SaveInsureResultVO saveOrderInfo(String orderNo, OrderInsureInfoDTO infoDTO);

    Boolean payWithBalance(PayWithBalanceReqDTO reqDTO);

    Boolean updateLogistics(InsuranceCardOrderBo bo);
}

