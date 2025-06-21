package com.dazuoye.contactlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
    private final List<Contact> contactList = new ArrayList<>();
    private final List<Contact> originalContactList = new ArrayList<>(); // 用于保存原始联系人列表
    private DatabaseHelper dbHelper;
    private ContactAdapter adapter;
    private EditText searchInput;
    private Button clearButton;

    // 中文排序比较器
    private final Comparator<Contact> chineseComparator = (c1, c2) -> {
        // 先按置顶状态排序（置顶的排前面）
        if (c1.isPinned() != c2.isPinned()) {
            return c1.isPinned() ? -1 : 1;
        }

        // 置顶状态相同的，按中文姓名排序
        Collator collator = Collator.getInstance(Locale.CHINA);
        return collator.compare(c1.getName(), c2.getName());
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView contactListView = findViewById(R.id.contactListView);
        dbHelper = new DatabaseHelper(this);

        // 初始化搜索组件
        searchInput = findViewById(R.id.searchInput);
        clearButton = findViewById(R.id.clearButton);

        // 确保数据库初始化完成
        initializeDatabase();

        // 设置适配器
        adapter = new ContactAdapter();
        contactListView.setAdapter(adapter);

        // 设置点击监听
        contactListView.setOnItemClickListener((parent, view, position, id) -> {
            Contact selectedContact = contactList.get(position);
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("CONTACT_ID", selectedContact.getId());
            intent.putExtra("NAME", selectedContact.getName());
            intent.putExtra("PHONE", selectedContact.getPhone());
            startActivity(intent);
        });

        // 设置搜索功能
        setupSearchFunctionality();
    }

    private void setupSearchFunctionality() {
        // 文本变化监听
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });

        // 清除按钮点击
        clearButton.setOnClickListener(v -> {
            searchInput.setText("");
            filterContacts("");
            // 隐藏键盘
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        });

        // 键盘搜索按钮监听
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // 隐藏键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    private void filterContacts(String query) {
        contactList.clear();

        if (TextUtils.isEmpty(query)) {
            // 如果搜索框为空，显示所有联系人
            contactList.addAll(originalContactList);
        } else {
            String lowerQuery = query.toLowerCase(Locale.getDefault());
            // 过滤联系人：姓名或电话包含搜索内容
            for (Contact contact : originalContactList) {
                if (contact.getName().toLowerCase(Locale.getDefault()).contains(lowerQuery) ||
                        contact.getPhone().contains(query)) {
                    contactList.add(contact);
                }
            }
        }

        // 重新排序（保留置顶功能）
        contactList.sort(chineseComparator);
        adapter.notifyDataSetChanged();
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
        String[] names = { "安哲平", "孙笑川", "侯国玉", "丁真", "方向成", "高玉波",
                           "Paul", "Alice","Samara","Bob","Lawrence","Carpenter"
                         };

        String[] phones = { "13800138000", "13900139000", "13700137000",
                            "13600136000", "13500135000", "13400134000",
                            "13300133000", "13200132000", "14521458965",
                            "14785236987", "12365478541", "15995874521"
                          };

        String avatar = "android.resource://" + getPackageName() + "/" + R.drawable.ic_person1;

        // 随机设置一些联系人置顶
        Random random = new Random();
        for (int i = 0; i < names.length; i++) {
            // 随机生成置顶状态
            boolean isPinned = random.nextBoolean();

            // 创建联系人对象并预设头像
            Contact contact = new Contact(0, names[i], phones[i], isPinned, avatar);
            dbHelper.addContact(contact);
        }
    }

    private void loadContacts() {
        // 清空列表
        contactList.clear();
        originalContactList.clear();

        // 从数据库加载联系人
        List<Contact> dbContacts = dbHelper.getAllContacts();
        contactList.addAll(dbContacts);
        originalContactList.addAll(dbContacts); // 保存原始列表

        // 在Java层进行中文排序
        contactList.sort(chineseComparator);

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
            String avatarUri = contact.getAvatarUri();
            if (avatarUri != null && !avatarUri.isEmpty()) {
                if (avatarUri.startsWith("android.resource")) {
                    // 预设头像
                    int resId = getResources().getIdentifier(
                            avatarUri.substring(avatarUri.lastIndexOf('/') + 1),
                            "drawable", getPackageName());
                    holder.avatar.setImageResource(resId);
                } else {
                    // 自定义头像
                    holder.avatar.setImageURI(Uri.parse(avatarUri));
                }
            } else {
                // 默认头像
                holder.avatar.setImageResource(R.drawable.ic_person1);
            }

            // 设置文本内容
            holder.name.setText(contact.getName());
            holder.phone.setText(contact.getPhone());

            // 设置置顶图标
            ImageView pinIcon = convertView.findViewById(R.id.pinIcon);
            if (contact.isPinned()) {
                pinIcon.setImageResource(R.drawable.ic_favorite);
            } else {
                pinIcon.setImageResource(R.drawable.ic_favorite_border);
            }

            // 设置置顶点击监听
            pinIcon.setOnClickListener(v -> {
                // 切换置顶状态
                boolean newPinnedState = !contact.isPinned();
                contact.setPinned(newPinnedState);

                // 更新数据库
                dbHelper.togglePinnedStatus(contact.getId(), newPinnedState);

                // 更新图标
                if (newPinnedState) {
                    pinIcon.setImageResource(R.drawable.ic_favorite);
                } else {
                    pinIcon.setImageResource(R.drawable.ic_favorite_border);
                }

                // 刷新列表（因为排序会改变）
                refreshContacts();
            });

            return convertView;
        }

        class ViewHolder {
            ImageView avatar;
            TextView name;
            TextView phone;
        }
    }
}