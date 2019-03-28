package com.revolut.assessment.services.Impl;

import static java.math.RoundingMode.DOWN;
import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolut.assessment.dao.RateDao;
import com.revolut.assessment.model.Rate;
import com.revolut.assessment.services.RateService;

public class RateServiceImpl implements RateService {

  private static Logger LOG = LoggerFactory.getLogger(RateServiceImpl.class);

  private static final String DELIMITER = "_";
  private final RateDao rateDao;
  private final Map<String, BigDecimal> currencyRate;

  public RateServiceImpl(final RateDao rateDao) {
    this.rateDao = rateDao;
    currencyRate = new HashMap<>();
    synchronize();
  }

  @Override
  public void create(final long id, final String currencyConversion, final String currencyRate) {
    rateDao.create(id, currencyConversion, currencyRate);
    synchronize();
  }

  @Override
  public void synchronize() {
    List<Rate> rates = rateDao.getAllCurrencyRates();
    rates.forEach(rate -> {
      BigDecimal roundedRate = new BigDecimal(rate.getCurrencyRate()).setScale(4, HALF_UP);
      currencyRate.put(rate.getCurrencyConversion(), roundedRate);
    });

  }

  @Override
  public BigDecimal converter(final String from, final String to, final Long amount) {
    if (from.equals(to)) {
      return new BigDecimal(amount);
    }
    BigDecimal curRate = currencyRate.get(from + DELIMITER + to);
    return new BigDecimal(amount).multiply(curRate).setScale(2, DOWN);
  }
}
