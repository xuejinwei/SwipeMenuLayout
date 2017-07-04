package com.xuejinwei.swipemenulayout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.xuejinwei.libs.SwipeMenuLayout;
import com.xuejinwei.swipemenulayout.adapter.CommonRVAdapter;
import com.xuejinwei.swipemenulayout.adapter.CommonViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xuejinwei on 2017/7/4.
 * Email:xuejinwei@outlook.com
 */

public class RecyclerViewActivity extends AppCompatActivity {

    @BindView(R.id.rv_list) RecyclerView mRvList;

    private List<String>            mStringList;
    private CommonRVAdapter<String> mCommonRVAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);
        ButterKnife.bind(this);

        mStringList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            mStringList.add("item:  " + i);
        }

        mCommonRVAdapter = new CommonRVAdapter<String>(this, R.layout.item_rv_list, mStringList) {
            @Override
            public void convert(CommonViewHolder gViewHolder, final String s) {
                gViewHolder.setText(R.id.tv_item, s);

                gViewHolder.getView(R.id.tv_item).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(RecyclerViewActivity.this, s, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void convert(final CommonViewHolder gViewHolder, String s, final int position) {
                super.convert(gViewHolder, s, position);
                gViewHolder.getView(R.id.tv_delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((SwipeMenuLayout) gViewHolder.getView(R.id.sml_content)).smoothClose();
                        gViewHolder.getView(R.id.sml_content).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mCommonRVAdapter.removeItem(position);
                            }
                        }, 300);
                    }
                });
            }
        };

        mRvList.setLayoutManager(new LinearLayoutManager(this));
        mRvList.setAdapter(mCommonRVAdapter);

    }

}
