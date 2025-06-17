package com.dazuoye.contactlist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        TextView detailName = findViewById(R.id.detailName);
        TextView detailPhone = findViewById(R.id.detailPhone);

        // 获取传递过来的数据
        String name = getIntent().getStringExtra("NAME");
        String phone = getIntent().getStringExtra("PHONE");

        // 设置界面内容
        detailName.setText(name);
        detailPhone.setText(phone);

        Button callButton = findViewById(R.id.callButton);
        callButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        });
    }
}
