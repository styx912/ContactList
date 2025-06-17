package com.dazuoye.contactlist;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    private String originalName;
    private String originalPhone;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        TextView detailName = findViewById(R.id.detailName);
        TextView detailPhone = findViewById(R.id.detailPhone);
        dbHelper = new DatabaseHelper(this);

        // 获取传递过来的数据
        originalName = getIntent().getStringExtra("NAME");
        originalPhone = getIntent().getStringExtra("PHONE");

        // 设置界面内容
        detailName.setText(originalName);
        detailPhone.setText(originalPhone);

        // 拨打按钮
        Button callButton = findViewById(R.id.callButton);
        callButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + originalPhone));
            startActivity(intent);
        });

        // 修改按钮
        Button editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(v -> showEditDialog());

        // 删除按钮
        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改联系人");

        // 设置自定义布局
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_contact, null);
        final EditText nameInput = view.findViewById(R.id.editName);
        final EditText phoneInput = view.findViewById(R.id.editPhone);

        nameInput.setText(originalName);
        phoneInput.setText(originalPhone);
        phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);

        builder.setView(view);

        // 设置按钮
        builder.setPositiveButton("保存", (dialog, which) -> {
            String newName = nameInput.getText().toString().trim();
            String newPhone = phoneInput.getText().toString().trim();

            if (newName.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(DetailActivity.this, "姓名和电话不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 更新联系人
            boolean success = dbHelper.updateContact(originalName, originalPhone, newName, newPhone);

            if (success) {
                Toast.makeText(DetailActivity.this, "联系人已更新", Toast.LENGTH_SHORT).show();

                // 更新显示
                TextView detailName = findViewById(R.id.detailName);
                TextView detailPhone = findViewById(R.id.detailPhone);
                detailName.setText(newName);
                detailPhone.setText(newPhone);

                // 更新原始值
                originalName = newName;
                originalPhone = newPhone;
            } else {
                Toast.makeText(DetailActivity.this, "更新失败，请重试", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("删除联系人")
                .setMessage("确定要删除 " + originalName + " 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    // 执行删除操作
                    boolean deleted = dbHelper.deleteContact(originalName, originalPhone);

                    if (deleted) {
                        Toast.makeText(DetailActivity.this, "联系人已删除", Toast.LENGTH_SHORT).show();
                        finish(); // 关闭当前详情页
                    } else {
                        Toast.makeText(DetailActivity.this, "删除失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}