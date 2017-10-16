package com.ag777.util.lang.string.model;

import java.util.List;

/**
 * 和百度翻译api交互时用的pojo
 * 
 * @author ag777
 * @version create on 2017年10月16日,last modify at 2017年10月16日
 */
public class ApiTranslatePojo {
	String from,to;
	List<Data> data;
    public class Data{
        String dst,src;

        public String getDst() {
            return dst;
        }

        public void setDst(String dst) {
            this.dst = dst;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }
        
    }
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }
    public List<Data> getData() {
        return data;
    }
    public void setData(List<Data> data) {
        this.data = data;
    }
}
