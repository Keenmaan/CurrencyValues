package controllers;

import models.Currency;
import play.mvc.Controller;
import java.math.BigDecimal;

/**
 * Created by keen on 4/11/15.
 */
public class Currencies extends Controller{

    public static Currency createCurrency(String name, String code, String factor,String avgValue){
        //if there is no currency of the same code or name (!)
        Currency currency=new Currency();
        currency.code=code;
        currency.name=name;
        currency.factor=Integer.parseInt(factor);
        avgValue=avgValue.replaceAll(",",".");
        currency.avgValue= new BigDecimal(avgValue);
        currency.lowestValue=new BigDecimal(avgValue);
        currency.highestValue=new BigDecimal(avgValue);
        currency.save();
        return currency;
    }
}
