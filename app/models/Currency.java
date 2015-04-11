package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by keen on 4/11/15.
 */
@Entity
public class Currency extends Model{
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Long id;

    @Column(unique=true)
    public String code;

    public String name;

    public int factor;

    @Column(precision=20,scale=4)
    public BigDecimal lowestValue;

    @Column(precision=20,scale=4)
    public BigDecimal highestValue;

    @Column(precision=20,scale=4)
    public BigDecimal avgValue;

    @OneToMany(targetEntity=CurrencyValue.class,mappedBy="currency", cascade = CascadeType.ALL)
    public List<CurrencyValue> currencyValues;

    public static Model.Finder<String,Currency> find = new Model.Finder<>(
            String.class, Currency.class
    );
}
