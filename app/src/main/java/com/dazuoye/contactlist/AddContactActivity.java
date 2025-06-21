package com.dazuoye.contactlist;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddContactActivity extends AppCompatActivity {

    private EditText editName;
    private EditText editPhone;
    private String avatarUri = "";
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        dbHelper = new DatabaseHelper(this);

        ImageView avatar = findViewById(R.id.avatar);
        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        Button saveButton = findViewById(R.id.saveButton);

        // 设置默认头像
        avatar.setImageResource(R.drawable.ic_person1);
        avatarUri = "android.resource://" + getPackageName() + "/" + R.drawable.ic_person1;

        // 保存按钮
        saveButton.setOnClickListener(v -> saveContact());
    }

    private void saveContact() {
        String name = editName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        // 验证输入
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "姓名和电话不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.length() < 2) {
            Toast.makeText(this, "姓名至少需要2个字符", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            Toast.makeText(this, "请输入有效的手机号码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建联系人对象
        Contact contact = new Contact(0, name, phone, false, avatarUri);

        // 添加到数据库
        dbHelper.addContact(contact);

        Toast.makeText(this, "联系人已添加", Toast.LENGTH_SHORT).show();

        // 返回结果并关闭
        setResult(RESULT_OK);
        finish();
    }
}