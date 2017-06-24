package com.xuejinwei.swipmenulayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_content) TextView mTvContent;
    @BindView(R.id.tv_delete)  TextView mTvDelete;
    @BindView(R.id.tv_top)     TextView mTvTop;
    @BindView(R.id.tv_top)     TextView mTvTopfdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    @OnClick({R.id.tv_content, R.id.tv_delete, R.id.tv_top})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_content:
                Toast.makeText(MainActivity.this, "content", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_delete:
                Toast.makeText(MainActivity.this, "delete", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_top:
                Toast.makeText(MainActivity.this, "top", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
