package com.dazuoye.contactlist;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ListView contactListView;
    private List<Contact> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactListView = findViewById(R.id.contactListView);

        // 初始化测试数据
        contactList = new ArrayList<>();
        contactList.add(new Contact(1,"张三", "13800138000"));
        contactList.add(new Contact(2,"李四", "13900139000"));
        contactList.add(new Contact(3,"王五", "13700137000"));
        contactList.add(new Contact(4,"张三", "13800138000"));
        contactList.add(new Contact(5,"李四", "13900139000"));
        contactList.add(new Contact(6,"王五", "13700137000"));
        contactList.add(new Contact(7,"张三", "13800138000"));
        contactList.add(new Contact(8,"李四", "13900139000"));
        contactList.add(new Contact(9,"王五", "13700137000"));
        contactList.add(new Contact(10,"张三", "13800138000"));
        contactList.add(new Contact(11,"李四", "13900139000"));
        contactList.add(new Contact(12,"王五", "13700137000"));

        // 设置适配器
        // 替换原来的ArrayAdapter代码
        contactListView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return contactList.size();
            }

            @Override
            public Object getItem(int position) {
                return contactList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(MainActivity.this)
                            .inflate(R.layout.contact_item, parent, false);
                }

                Contact contact = contactList.get(position);

                // 设置头像（这里使用随机颜色，实际可加载真实头像）
                ImageView avatar = convertView.findViewById(R.id.avatar);
                int avatarColor = Color.argb(255,
                        new Random().nextInt(256),
                        new Random().nextInt(256),
                        new Random().nextInt(256));
                avatar.setColorFilter(avatarColor);

                // 设置文本内容
                ((TextView) convertView.findViewById(R.id.contactName)).setText(contact.getName());
                ((TextView) convertView.findViewById(R.id.contactPhone)).setText(contact.getPhone());

                return convertView;
            }
        });

        // 设置点击监听
        contactListView.setOnItemClickListener((parent, view, position, id) -> {
            Contact selectedContact = contactList.get(position);
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("NAME", selectedContact.getName());
            intent.putExtra("PHONE", selectedContact.getPhone());
            startActivity(intent);
        });
    }
}