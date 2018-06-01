package yly.crawl.springboot.pojo;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
 
@Entity //表示是实体类
@Table(name = "gallery") //对应的表名
public class Gallery {
 
    @Id //主键标识
    @GeneratedValue(strategy = GenerationType.IDENTITY) //自增长方式
    @Column(name = "id") //数据库字段名（属性、列名）
    private Integer id;
     
    @Column(name = "url")
    private String url;
    
    @Column(name = "serial_id")
    private String serialId;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "lenth")
    private String lenth;
    
    @Column(name = "gmt_create")
    private Date gmtCreate;

	public int getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSerialId() {
		return serialId;
	}

	public void setSerialId(String serialId) {
		this.serialId = serialId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLenth() {
		return lenth;
	}

	public void setLenth(String lenth) {
		this.lenth = lenth;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}
    
    
    
    
     
}
