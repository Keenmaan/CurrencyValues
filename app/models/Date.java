package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by keen on 4/11/15.
 */
@Entity
public class Date extends Model{
    @Id
    public java.sql.Date date;

    @OneToMany(targetEntity=CurrencyValue.class,mappedBy="dateModel")
    public List<CurrencyValue> currencyValues;

    public static Model.Finder<String,Date> find = new Model.Finder<>(
            String.class, Date.class
    );
}
