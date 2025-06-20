package com.dazuoye.contactlist;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class DetailActivity extends AppCompatActivity {
    private String originalName;
    private String originalPhone;
    private DatabaseHelper dbHelper;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TAKE_PHOTO_REQUEST = 2;
    private long contactId;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "需要相机权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        TextView detailName = findViewById(R.id.detailName);
        TextView detailPhone = findViewById(R.id.detailPhone);
        ImageView detailAvatar = findViewById(R.id.detailAvatar);
        dbHelper = new DatabaseHelper(this);

        // 获取传递过来的数据
        originalName = getIntent().getStringExtra("NAME");
        originalPhone = getIntent().getStringExtra("PHONE");
        contactId = getIntent().getLongExtra("CONTACT_ID", -1);
        Contact contact = dbHelper.getContactById(contactId);

        if (contact != null) {
            // 设置界面内容
            originalName = contact.getName();
            originalPhone = contact.getPhone();
            detailName.setText(originalName);
            detailPhone.setText(originalPhone);

            // 设置头像
            String avatarUri = contact.getAvatarUri();
            if (avatarUri != null && !avatarUri.isEmpty()) {
                if (avatarUri.startsWith("android.resource")) {
                    int resId = getResources().getIdentifier(
                            avatarUri.substring(avatarUri.lastIndexOf('/') + 1),
                            "drawable", getPackageName());
                    detailAvatar.setImageResource(resId);
                } else {
                    detailAvatar.setImageURI(Uri.parse(avatarUri));
                }
            } else {
                detailAvatar.setImageResource(R.drawable.ic_person1);
            }
        } else {
            // 回退方案：使用Intent传递的数据
            originalName = getIntent().getStringExtra("NAME");
            originalPhone = getIntent().getStringExtra("PHONE");
            detailName.setText(originalName);
            detailPhone.setText(originalPhone);
            detailAvatar.setImageResource(R.drawable.ic_person1);
        }

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

        //修改联系人头像
        ImageView avatar = findViewById(R.id.detailAvatar);
        avatar.setOnClickListener(v -> showAvatarOptions());
    }


    //修改头像选项
    private void showAvatarOptions() {
        CharSequence[] options = {"从预设中选择", "拍照", "从相册选择", "取消"};

        new AlertDialog.Builder(this)
                .setTitle("更换头像")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showPresetAvatars();
                    else if (which == 1) takePhoto();
                    else if (which == 2) pickImage();
                })
                .show();
    }

    private void showPresetAvatars() {
        // 获取所有预设头像
        final int[] presetAvatars = {
                R.drawable.ic_person2,
                R.drawable.ic_person3,
                R.drawable.ic_person4,
                R.drawable.ic_person5,
                R.drawable.ic_person6,
                R.drawable.ic_person7
        };

        // 使用GridView展示头像
        GridView gridView = new GridView(this);
        gridView.setNumColumns(3); // 每行显示3个头像

        // 设置适配器
        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return presetAvatars.length;
            }

            @Override
            public Object getItem(int position) {
                return presetAvatars[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ImageView imageView;
                if (convertView == null) {
                    imageView = new ImageView(DetailActivity.this);
                    imageView.setLayoutParams(new GridView.LayoutParams(200, 200)); // 设置大小
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setPadding(8, 8, 8, 8);

                } else {
                    imageView = (ImageView) convertView;
                }

                // 设置头像资源
                imageView.setImageResource(presetAvatars[position]);
                return imageView;
            }
        };

        gridView.setAdapter(adapter);

        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择预设头像")
                .setView(gridView)
                .setNegativeButton("取消", null);

        final AlertDialog dialog = builder.create();

        // 设置头像点击事件
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String uri = "android.resource://" + getPackageName() + "/" + presetAvatars[position];
            updateAvatar(uri);
            dialog.dismiss(); // 关闭对话框
        });

        dialog.show();
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION
            );
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST);
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                Uri selectedImage = data.getData();
                processSelectedImage(selectedImage);
            } else if (requestCode == TAKE_PHOTO_REQUEST && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                processCapturedImage(imageBitmap);
            }
        }
    }

    private void processSelectedImage(Uri imageUri) {
        Bitmap bitmap = ImageUtils.getBitmapFromUri(this, imageUri);
        if (bitmap != null) {
            Bitmap circleBitmap = ImageUtils.getCircleBitmap(bitmap);
            String savedUri = ImageUtils.saveImage(this, circleBitmap, String.valueOf(contactId));
            if (savedUri != null) {
                updateAvatar(savedUri);
            }
        }
    }

    private void processCapturedImage(Bitmap bitmap) {
        if (bitmap != null) {
            Bitmap circleBitmap = ImageUtils.getCircleBitmap(bitmap);
            String savedUri = ImageUtils.saveImage(this, circleBitmap, String.valueOf(contactId));
            if (savedUri != null) {
                updateAvatar(savedUri);
            }
        }
    }

    private void updateAvatar(String avatarUri) {
        // 更新数据库
        boolean success = dbHelper.updateAvatar(contactId, avatarUri);

        if (success) {
            // 更新UI
            ImageView avatar = findViewById(R.id.detailAvatar);
            if (avatarUri.startsWith("android.resource")) {
                int resId = getResources().getIdentifier(
                        avatarUri.substring(avatarUri.lastIndexOf('/') + 1),
                        "drawable", getPackageName());
                avatar.setImageResource(resId);
            } else {
                avatar.setImageURI(Uri.parse(avatarUri));
            }
            Toast.makeText(this, "头像已更新", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
        }
    }

    //修改联系人方法
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

            //输入验证
            if (newName.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(DetailActivity.this, "姓名和电话不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newName.length() < 2) {
                Toast.makeText(this, "姓名至少需要2个字符", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPhone.matches("^1[3-9]\\d{9}$")) { // 简单手机号验证
                Toast.makeText(this, "请输入有效的手机号码", Toast.LENGTH_SHORT).show();
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


    //确认删除联系人提示信息
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