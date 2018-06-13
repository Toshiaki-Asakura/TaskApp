package jp.techacademy.toshiaki.asakura.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_TASK = "jp.techacademy.toshiaki.asakura.TASK";
    private Realm mRealm;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadListView();
        }
    };
    private ListView mListView;
    private TaskAdapter mTaskAdapter;
    private EditText mCheckCategory;
    private String mCheckCategory_Text;

//◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab); //◆◆◆fab定義
            fab.setOnClickListener(new View.OnClickListener() { //◆◆◆fab実装

            @Override
            public void onClick(View view) {  //◆◆◆fabを押したら・・・
                Intent intent = new Intent(MainActivity.this, InputActivity.class);  //◆◆◆InputActivityに遷移
                startActivity(intent); //◆◆◆行け！！！
                }
            } );

        // ◆◆◆Realmの設定
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        //◆◆◆ ListViewの設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);

        // ◆タップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  //◆◆◆実装して
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 入力・編集する画面に遷移させる
                Task task = (Task) parent.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task.getId());

                startActivity(intent);
            }
        });

        // ◆長押しでダイアログ
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final Task task = (Task) parent.getAdapter().getItem(position);  // ◆◆◆taskを定義

                //builderという名noダイアログを生成
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                //アラートダイタログ
                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");

                //◆◆◆OKボタン
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {  //◆◆◆ダイアログのボタンに実装
                                @Override
                                public void onClick(DialogInterface dialog, int which) {  //◆◆◆ダイアログ押下

                                    RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();

                                    mRealm.beginTransaction();
                                    results.deleteAllFromRealm();//◆◆◆◆◆消す
                                    mRealm.commitTransaction();

                                    Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                                    PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                                            MainActivity.this,
                                            task.getId(),
                                            resultIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    );

                                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                    alarmManager.cancel(resultPendingIntent);

                                    reloadListView();//◆◆◆◆◆再描画
                                }
                });
                //◆◆◆◆◆キャンセルボタン
                builder.setNegativeButton("CANCEL", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();

                return true;
            }
        });

        Button button = (Button) findViewById(R.id.cc_button); //button定義
        button.setOnClickListener(new View.OnClickListener() { //button実装

            @Override
            public void onClick(View view) {  //buttonを押したら・・・
                mCheckCategory = (EditText) findViewById(R.id.checkcategory_edit_text);
                mCheckCategory_Text = mCheckCategory.getText().toString();

                if (mCheckCategory.getText().toString().equals("")) {

                    Log.d("asat", "EditTextが空です");
                    reloadListView();//再描画
                }
                {
                    Log.d("asat", String.valueOf(mCheckCategory_Text));
                    RealmResults<Task> taskRealmResults = mRealm.where(Task.class)
                            .equalTo("category", String.valueOf(mCheckCategory_Text))
                            .findAll().sort("date", Sort.DESCENDING);
                    mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults)); // 上記の結果を、TaskList としてセットする
                    mListView.setAdapter(mTaskAdapter); // TaskのListView用のアダプタに渡す
                    mTaskAdapter.notifyDataSetChanged();// 表示を更新するために、アダプターにデータが変更されたことを知らせる

                }
            }

        } );
        reloadListView();//◆◆◆再描画
    }

//◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆

    private void reloadListView() {  //◆◆◆再描画
                                                                                                // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAll().sort("date", Sort.DESCENDING);
        mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults)); // 上記の結果を、TaskList としてセットする
        mListView.setAdapter(mTaskAdapter); // TaskのListView用のアダプタに渡す
        mTaskAdapter.notifyDataSetChanged();// 表示を更新するために、アダプターにデータが変更されたことを知らせる
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }
}