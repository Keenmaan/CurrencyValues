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

    public BigDecimal lowestValue;

    public BigDecimal highestValue;

    public BigDecimal avgValue;

    @OneToMany(targetEntity=CurrencyValue.class,mappedBy="currency", cascade = CascadeType.ALL)
    public List<CurrencyValue> currencyValues;
}
