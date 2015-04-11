package controllers;

import models.Currency;
import models.CurrencyValue;
import models.Date;
import play.mvc.Controller;

import java.math.BigDecimal;

/**
 * Created by keen on 4/11/15.
 */
public class Currencies extends Controller{

    public static Currency createCurrency(String name, String code, String factor,String avgValue){
        //if there is no currency of the same code or name (!)
        Currency currency = Currency.find.where().eq("code", code).findUnique();

        if (currency==null){
            currency=new Currency();
            currency.code=code;
            currency.name=name;
            currency.factor=Integer.parseInt(factor);
            avgValue=avgValue.replaceAll(",",".");
            currency.avgValue= new BigDecimal(avgValue);
            currency.lowestValue=new BigDecimal(avgValue);
            currency.highestValue=new BigDecimal(avgValue);
            currency.save();
        }
        return currency;
    }

    public static Date createDate(String date){
        Date dateModel = Date.find.where().eq("date", date).findUnique();

        if (dateModel ==null){
            dateModel=new Date();
            dateModel.date=java.sql.Date.valueOf(date);
            dateModel.save();
        }

        return dateModel;
    }

    public static CurrencyValue createCurrencyValue(String code,Date date,String avgValue){

        Currency currency = Currency.find.where().eq("code", code).findUnique();

        if (currency!=null)  {
            CurrencyValue currencyValue = new CurrencyValue();
            currencyValue.currency=currency;
            avgValue=avgValue.replaceAll(",",".");
            currencyValue.value=new BigDecimal(avgValue);
            currencyValue.dateModel=date;
            currencyValue.save();
            return currencyValue;
        }
        else return null;
    }
}
