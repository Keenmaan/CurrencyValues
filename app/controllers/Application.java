package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.Currency;
import models.CurrencyValue;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.currency;
import views.html.index;

import java.util.ArrayList;
import java.util.List;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render(models.Currency.find.all()));
    }

    public static Result currencyView(String code){
        Currency currencyInstance=Currency.find.where().eq("code",code).findUnique();
        return ok(currency.render(JsonEncode(code), currencyInstance));
    }

    public static String JsonEncode(String code){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        List<CurrencyValue> currencyValueList=Currency.find.where().eq("code",code).findUnique().currencyValues;
        List<CurrencyValueJson> cvJsonList = new ArrayList<>();

        for (CurrencyValue currencyValue : currencyValueList){
            cvJsonList.add(new CurrencyValueJson(
                    currencyValue.dateModel.date,
                    currencyValue.value));

        }
        return gson.toJson(cvJsonList);
    }
}

