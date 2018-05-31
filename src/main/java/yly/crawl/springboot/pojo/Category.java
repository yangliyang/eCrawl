package yly.crawl.springboot.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
 
@Entity //表示是实体类
@Table(name = "category_") //对应的表名
public class Category {
 
    @Id //主键标识
    @GeneratedValue(strategy = GenerationType.IDENTITY) //自增长方式
    @Column(name = "id") //数据库字段名（属性、列名）
    private int id;
     
    @Column(name = "name")
    private String name;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
     
}
