package com.xinxin.filebrowser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mMsgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMsgView = findViewById(R.id.msg);
    }

    public void browseFiles(View view) {
        Intent intent = new Intent(this, FilesActivity.class);
        startActivity(intent);
    }

    public void chooseFiles(View view) {
        Intent intent = new Intent(this, FilesActivity.class);
        intent.setAction(Shared.ACTION_PICK);
        intent.putExtra(Shared.EXTRA_FILE_MAX_COUNT, 5);
        intent.putExtra(Shared.EXTRA_FILE_MAX_SIZE, 1024 * 1024L * 20); //20M
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMsgView.setText("");
        if (resultCode == RESULT_OK) {
            String[] selections = data.getStringArrayExtra("data");
            int i = 0;
            for(String s : selections) {
                mMsgView.append((++i) + ". " + s + "\r\n");
            }
        }
    }
}
