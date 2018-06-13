package jp.techacademy.toshiaki.asakura.taskapp;

import java.io.Serializable;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Task extends RealmObject implements Serializable {
    private  String title;   // タイトル
    private  String contents;  // 内容
    private  String category;  //カテゴリー
    private Date date;  // 日時
    @PrimaryKey
    private  int id;  // id をプライマリーキーとして設定

                                                                                 //タイトルの取得設定
    public String getTitle() {
        return title;
    }
    public void setTitle(String title)  { this.title = title; }
                                                                                //カテゴリーの取得設定
    public String getCategory() { return category; }
    public void setCategory(String category){ this.category = category; }
                                                                                   //内容の取得設定
    public String getContents(){return contents;}
    public void setContents(String contents){ this.contents = contents; }
                                                                                 //デイトの取得設定
    public Date getDate() {
        return date;
    }
    public void setDate(Date date)  {this.date = date;}
                                                                                //ＩＤの取得設定
    public int getId() {
        return id;
    }
    public void setId(int id) { this.id = id; }
}
