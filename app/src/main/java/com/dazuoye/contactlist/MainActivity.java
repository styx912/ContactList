package com.dazuoye.contactlist;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ListView contactListView;
    private List<Contact> contactList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private ContactAdapter adapter;

    // 中文排序比较器
    private final Comparator<Contact> chineseComparator = (c1, c2) -> {
        Collator collator = Collator.getInstance(Locale.CHINA);
        return collator.compare(c1.getName(), c2.getName());
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactListView = findViewById(R.id.contactListView);
        dbHelper = new DatabaseHelper(this);

        // 确保数据库初始化完成
        initializeDatabase();

        // 设置适配器
        adapter = new ContactAdapter();
        contactListView.setAdapter(adapter);

        // 设置点击监听
        contactListView.setOnItemClickListener((parent, view, position, id) -> {
            Contact selectedContact = contactList.get(position);
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("NAME", selectedContact.getName());
            intent.putExtra("PHONE", selectedContact.getPhone());
            startActivity(intent);
        });
    }

    private void initializeDatabase() {
        // 如果数据库为空，添加测试数据
        if (dbHelper.getAllContacts().isEmpty()) {
            Log.d("Database", "Adding test contacts");
            addTestContacts();
        }
        loadContacts();
    }

    private void addTestContacts() {
        String[] names = {"安哲平", "白梓岚", "陈诚斌", "丁真", "方向成", "高玉波", "黄绍轩", "金成武","孔惟桢","李俊真","马玉琦","周振杰"};
        String[] phones = {
                "13800138000", "13900139000", "13700137000",
                "13600136000", "13500135000", "13400134000",
                "13300133000", "13200132000", "14521458965",
                "14785236987", "12365478541", "15995874521"
        };

        for (int i = 0; i < names.length; i++) {
            Contact contact = new Contact(0, names[i], phones[i]);
            dbHelper.addContact(contact);
        }
    }

    private void loadContacts() {
        contactList.clear();
        contactList.addAll(dbHelper.getAllContacts());

        // 在Java层进行中文排序
        Collections.sort(contactList, chineseComparator);

        Log.d("Database", "Loaded " + contactList.size() + " contacts");

        // 打印排序后的联系人
        for (Contact contact : contactList) {
            Log.d("SortedContacts", contact.getName() + " - " + contact.getPhone());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshContacts();
    }

    private void refreshContacts() {
        loadContacts();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // 适配器类
    class ContactAdapter extends BaseAdapter {
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
            return contactList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.contact_item, parent, false);
                holder = new ViewHolder();
                holder.avatar = convertView.findViewById(R.id.avatar);
                holder.name = convertView.findViewById(R.id.contactName);
                holder.phone = convertView.findViewById(R.id.contactPhone);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Contact contact = contactList.get(position);

            // 设置头像
            int avatarColor = Color.argb(255,
                    new Random().nextInt(256),
                    new Random().nextInt(256),
                    new Random().nextInt(256));
            holder.avatar.setColorFilter(avatarColor);

            // 设置文本内容
            holder.name.setText(contact.getName());
            holder.phone.setText(contact.getPhone());

            return convertView;
        }

        class ViewHolder {
            ImageView avatar;
            TextView name;
            TextView phone;
        }
    }
}