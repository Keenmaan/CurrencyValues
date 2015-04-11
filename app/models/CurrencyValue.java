package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Created by keen on 4/11/15.
 */
@Entity
public class CurrencyValue extends Model {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Long id;

    public BigDecimal value;

    @ManyToOne
    public Currency currency;

    @ManyToOne
    public Date dateModel;

}