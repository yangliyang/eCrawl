package yly.crawl.springboot.pojo;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
 
@Entity //表示是实体类
@Table(name = "image") //对应的表名
public class Image {
 
    @Id //主键标识
    @GeneratedValue(strategy = GenerationType.IDENTITY) //自增长方式
    @Column(name = "id") //数据库字段名（属性、列名）
    private Integer id;
     
    @Column(name = "url")
    private String url;
    
    @Column(name = "serial_num")
    private String serialNum;
    
    @Column(name = "gallery_id")
    private Integer galleryId;
    
    @Column(name = "suffix")
    private String suffix;
    
    @Column(name = "gmt_create")
    private Date gmtCreate;

	public Integer getId() {
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

	public String getSerialNum() {
		return serialNum;
	}

	public void setSerialNum(String serialNum) {
		this.serialNum = serialNum;
	}

	public Integer getGalleryId() {
		return galleryId;
	}

	public void setGalleryId(Integer galleryId) {
		this.galleryId = galleryId;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	
    
    
    
    
     
}
