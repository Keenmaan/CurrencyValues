package controllers;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.YearMonth;

public class CurrencyValueJson {
    String date;
    String value;
    public CurrencyValueJson(Date date, BigDecimal value) {
        YearMonth yearMonth=YearMonth.of(date.toLocalDate().getYear(),date.toLocalDate().getMonth());
        this.date=yearMonth.toString();
        this.value=value.toString();
    }
}
