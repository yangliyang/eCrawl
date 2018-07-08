package yly.crawl.springboot.pojo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
 
@Entity //表示是实体类
@Table(name = "image",uniqueConstraints = {@UniqueConstraint(columnNames={"gallery_id", "serial_num"})}) //对应的表名
public class Image {
 

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
    
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
    
    @Column(name = "next_inner_url")
    private String nextInnerUrl;
    
    

	@Column(name = "gmt_modified")
    private Date gmtModified;
    
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

	@Override
	public String toString() {
		return galleryId+"/"+serialNum;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}
	public String getNextInnerUrl() {
		return nextInnerUrl;
	}

	public void setNextInnerUrl(String nextInnerUrl) {
		this.nextInnerUrl = nextInnerUrl;
	}
    
    
    
     
}
