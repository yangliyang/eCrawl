package yly.crawl.springboot.pojo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
 
@Entity //表示是实体类
@Table(name = "image") //对应的表名
public class Image {
 
//    @Id //主键标识
//    @GeneratedValue(strategy = GenerationType.IDENTITY) //自增长方式
//    @Column(name = "id") //数据库字段名（属性、列名）
//    private Integer id;
    @Id 
    @Column(name = "url")
    private String url;
    
    @Column(name = "serial_num")
    private Integer serialNum;
    
    @Column(name = "gallery_id")
    private String galleryId;
    
    @Column(name = "suffix")
    private String suffix;
    
    @Column(name = "gmt_create")
    private Date gmtCreate;

    @Column(name = "inner_url")
    private String innerUrl;

	public String getInnerUrl() {
		return innerUrl;
	}

	public void setInnerUrl(String innerUrl) {
		this.innerUrl = innerUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	

	

	public Integer getSerialNum() {
		return serialNum;
	}

	public void setSerialNum(Integer serialNum) {
		this.serialNum = serialNum;
	}

	public String getGalleryId() {
		return galleryId;
	}

	public void setGalleryId(String galleryId) {
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
