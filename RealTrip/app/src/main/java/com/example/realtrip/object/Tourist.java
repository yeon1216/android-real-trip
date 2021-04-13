package com.example.realtrip.object;

/**
 * 여행지
 */
public class Tourist {

    public String addr1; // 주소
    public String addr2; // xx 동 (주소)
    public String areacode; // 지역 (서울, 인청 등)
    public String cat1; // 대분류
    public String cat2; // 중분류
    public String cat3; // 소분류
    public String contentid; // 컨텐트 아이디
    public String contenttypeid; // 컨텐츠 유형
    public String createdtime; // 만들어진 시간
    public String dist; // 기준 좌표로부터의 거리
    public String firstimage; // 대표이미지
    public String firstimage2; // 썸네일 이미지
    public String mapx; // 지도 x 좌표
    public String mapy; // 지도 y 좌표
    public String mlevel; // 지도 레벨
    public String modifiedtime; // 수정시간
    public String readcount; // 조회수
    public String sigungucode;
    public String tel; // 전화번호
    public String title; // 여행지 이름

    public Tourist() {
    }

    public Tourist(String contentid) {
        this.contentid = contentid;
    }

    public Tourist(String addr1, String addr2, String areacode, String cat1, String cat2, String cat3, String contentid, String contenttypeid, String createdtime, String dist, String mapx, String mapy, String mlevel, String modifiedtime, String readcount, String sigungucode, String tel, String title) {
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.areacode = areacode;
        this.cat1 = cat1;
        this.cat2 = cat2;
        this.cat3 = cat3;
        this.contentid = contentid;
        this.contenttypeid = contenttypeid;
        this.createdtime = createdtime;
        this.dist = dist;
        this.mapx = mapx;
        this.mapy = mapy;
        this.mlevel = mlevel;
        this.modifiedtime = modifiedtime;
        this.readcount = readcount;
        this.sigungucode = sigungucode;
        this.tel = tel;
        this.title = title;
    }

    public Tourist(String addr1, String addr2, String areacode, String cat1, String cat2, String cat3, String contentid, String contenttypeid, String createdtime, String dist, String firstimage, String mapx, String mapy, String mlevel, String modifiedtime, String readcount, String sigungucode, String tel, String title) {
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.areacode = areacode;
        this.cat1 = cat1;
        this.cat2 = cat2;
        this.cat3 = cat3;
        this.contentid = contentid;
        this.contenttypeid = contenttypeid;
        this.createdtime = createdtime;
        this.dist = dist;
        this.firstimage = firstimage;
        this.mapx = mapx;
        this.mapy = mapy;
        this.mlevel = mlevel;
        this.modifiedtime = modifiedtime;
        this.readcount = readcount;
        this.sigungucode = sigungucode;
        this.tel = tel;
        this.title = title;
    }

    public Tourist(String addr1, String addr2, String areacode, String cat1, String cat2, String cat3, String contentid, String contenttypeid, String createdtime, String dist, String firstimage, String firstimage2, String mapx, String mapy, String mlevel, String modifiedtime, String readcount, String sigungucode, String tel, String title) {
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.areacode = areacode;
        this.cat1 = cat1;
        this.cat2 = cat2;
        this.cat3 = cat3;
        this.contentid = contentid;
        this.contenttypeid = contenttypeid;
        this.createdtime = createdtime;
        this.dist = dist;
        this.firstimage = firstimage;
        this.firstimage2 = firstimage2;
        this.mapx = mapx;
        this.mapy = mapy;
        this.mlevel = mlevel;
        this.modifiedtime = modifiedtime;
        this.readcount = readcount;
        this.sigungucode = sigungucode;
        this.tel = tel;
        this.title = title;
    }

    public String getAddr1() {
        return addr1;
    }

    public String getAddr2() {
        return addr2;
    }

    public String getAreacode() {
        return areacode;
    }

    public String getCat1() {
        return cat1;
    }

    public String getCat2() {
        return cat2;
    }

    public String getCat3() {
        return cat3;
    }

    public String getContentid() {
        return contentid;
    }

    public String getContenttypeid() {
        return contenttypeid;
    }

    public String getCreatedtime() {
        return createdtime;
    }

    public String getDist() {
        return dist;
    }

    public String getFirstimage() {
        return firstimage;
    }

    public String getFirstimage2() {
        return firstimage2;
    }

    public String getMapx() {
        return mapx;
    }

    public String getMapy() {
        return mapy;
    }

    public String getMlevel() {
        return mlevel;
    }

    public String getModifiedtime() {
        return modifiedtime;
    }

    public String getReadcount() {
        return readcount;
    }

    public String getSigungucode() {
        return sigungucode;
    }

    public String getTel() {
        return tel;
    }

    public String getTitle() {
        return title;
    }

    public void setAddr1(String addr1) {
        this.addr1 = addr1;
    }

    public void setAddr2(String addr2) {
        this.addr2 = addr2;
    }

    public void setAreacode(String areacode) {
        this.areacode = areacode;
    }

    public void setCat1(String cat1) {
        this.cat1 = cat1;
    }

    public void setCat2(String cat2) {
        this.cat2 = cat2;
    }

    public void setCat3(String cat3) {
        this.cat3 = cat3;
    }

    public void setContentid(String contentid) {
        this.contentid = contentid;
    }

    public void setContenttypeid(String contenttypeid) {
        this.contenttypeid = contenttypeid;
    }

    public void setCreatedtime(String createdtime) {
        this.createdtime = createdtime;
    }

    public void setDist(String dist) {
        this.dist = dist;
    }

    public void setFirstimage(String firstimage) {
        this.firstimage = firstimage;
    }

    public void setFirstimage2(String firstimage2) {
        this.firstimage2 = firstimage2;
    }

    public void setMapx(String mapx) {
        this.mapx = mapx;
    }

    public void setMapy(String mapy) {
        this.mapy = mapy;
    }

    public void setMlevel(String mlevel) {
        this.mlevel = mlevel;
    }

    public void setModifiedtime(String modifiedtime) {
        this.modifiedtime = modifiedtime;
    }

    public void setReadcount(String readcount) {
        this.readcount = readcount;
    }

    public void setSigungucode(String sigungucode) {
        this.sigungucode = sigungucode;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setTitle(String title) {
        this.title = title;
    }
} // 클래스
